package activeset.kwplus;

import java.util.ArrayList;
import java.util.LinkedList;

import bean.BufferedUserVisit;
import bean.UserVisit;
import it.unimi.dsi.fastutil.ints.Int2ReferenceRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import util.Parameter;

public class ActiveSetHistogramBuffered {

	public LinkedList<Integer> index;
	public Int2ReferenceRBTreeMap<ActiveSetKnapsackBuffered> checkpoints;
	public int start_id;

	ActiveSetHistogramBuffered() {
		this.start_id = -1;
		this.index = new LinkedList<>();
		this.checkpoints = new Int2ReferenceRBTreeMap<>();
	}

	public void batchedInsert(ArrayList<UserVisit> list_visit) {

		this.start_id = list_visit.get(0).id;
		
		while (this.index.size() > 2 && this.index.get(1).intValue() < this.start_id + Parameter.T - Parameter.W) {
			int deleted = this.index.removeFirst();
			this.checkpoints.remove(deleted);
		}

		if (this.checkpoints.size() > 0) {
			for (ActiveSetKnapsackBuffered streaming : this.checkpoints.values()) {
				for (UserVisit visit : list_visit) {
					streaming.insert(visit);
				}
			}
		}

		ActiveSetKnapsackBuffered new_streaming = new ActiveSetKnapsackBuffered(this.start_id);
		for (UserVisit visit : list_visit) {
			new_streaming.insert(visit);
		}
		this.index.addLast(new_streaming.start_pos);
		this.checkpoints.put(new_streaming.start_pos, new_streaming);

		IntArrayList delete_list = new IntArrayList();
		int cur = 0, pre;
		while (cur < this.index.size() - 2) {
			pre = cur;
			cur = this.findNext(cur, this.checkpoints.get(this.index.get(cur).intValue()).getFunctionValue());
			if (cur - pre > 1) {
				for (int i = pre + 1; i < cur; i++) {
					delete_list.add(this.index.get(i).intValue());
				}
			}
		}

		if (delete_list.size() > 0) {
			IntListIterator iter = delete_list.iterator();
			while (iter.hasNext()) {
				int deleted = iter.nextInt();
				this.index.remove(Integer.valueOf(deleted));
				this.checkpoints.remove(deleted);
			}
		}
	}

	private int findNext(int cur, double cur_value) {
		int next;
		double next_value = this.checkpoints.get(this.index.get(cur + 1).intValue()).getFunctionValue();
		if (next_value < (1.0d - Parameter.BETA) * cur_value)
			next = cur + 1;
		else {
			int i = cur + 2;
			while (i < this.index.size() - 1 && this.checkpoints.get(this.index.get(i).intValue()).getFunctionValue() >= (1.0d - Parameter.BETA) * cur_value) {
				i++;
			}
			if (i == this.index.size() - 1 && this.checkpoints.get(this.index.get(i).intValue()).getFunctionValue() >= (1.0d - Parameter.BETA) * cur_value)
				next = i;
			else
				next = i - 1;
		}
		return next;
	}

	public ActiveSetInstanceBuffered post_processing() {
		ActiveSetInstanceBuffered result;
		ArrayList<BufferedUserVisit> candidates = new ArrayList<>();
		if (this.index.getFirst() < this.start_id + Parameter.T - Parameter.W) {
			ActiveSetKnapsackBuffered checkpoint = this.checkpoints.get(this.index.get(1));
			result = checkpoint.getCandidateInstance();
			for (int instance_id = checkpoint.result_key; instance_id <= checkpoint.max; instance_id++) {
				ActiveSetInstanceBuffered cur_inst = new ActiveSetInstanceBuffered(checkpoint.instances.get(instance_id));
				for (BufferedUserVisit buffered_visit : checkpoint.instances.get(instance_id).buffer) {
					if (cur_inst.checkBudget(buffered_visit.visit)) {
						candidates.add(buffered_visit);
					}
				}
				ActiveSetInstanceBuffered prev_inst = this.checkpoints.get(this.index.get(0)).instances.get(instance_id);
				if (prev_inst != null) {
					for (UserVisit visit : prev_inst.results) {
						if (visit.id >= this.start_id + Parameter.T - Parameter.W && ! cur_inst.results.contains(visit)) {
							candidates.add(new BufferedUserVisit(visit, visit.maximum_gain / visit.maximum_cost));
						}
					}
					
					for (BufferedUserVisit buffered_visit : prev_inst.buffer) {
						if (buffered_visit.visit.id >= this.start_id + Parameter.T - Parameter.W && !cur_inst.results.contains(buffered_visit.visit)) {
							candidates.add(new BufferedUserVisit(buffered_visit.visit, buffered_visit.visit.maximum_gain / buffered_visit.visit.maximum_cost));
						}
					}
				}
				
				if (!candidates.isEmpty()){
					this.greedy(cur_inst, candidates);
				}
				
				if (cur_inst.function_value > result.function_value) {
					result = cur_inst;
				} else if (instance_id > checkpoint.result_key + 1) {
					return result;
				}
			}
			return result;
		} else {
			ActiveSetKnapsackBuffered checkpoint = this.checkpoints.get(this.index.get(0));
			result = checkpoint.getCandidateInstance();
			for (int instance_id = checkpoint.result_key; instance_id <= checkpoint.max; instance_id++) {
				ActiveSetInstanceBuffered cur_inst = new ActiveSetInstanceBuffered(checkpoint.instances.get(instance_id));
				for (BufferedUserVisit buffered_visit : checkpoint.instances.get(instance_id).buffer) {
					if (cur_inst.checkBudget(buffered_visit.visit)) {
						candidates.add(buffered_visit);
					}
				}
				if (!candidates.isEmpty()){
					this.greedy(cur_inst, candidates);
				}
				
				if (cur_inst.function_value > result.function_value) {
					result = cur_inst;
				} else if (instance_id > checkpoint.result_key + 1) {
					return result;
				}
			}
			return result;
		}
	}
	
	private void greedy(ActiveSetInstanceBuffered instance, ArrayList<BufferedUserVisit> candidates) {
		double max_cost_effectiveness = 0.0d;
		ArrayList<BufferedUserVisit> deleted = new ArrayList<>();
		BufferedUserVisit visit_max = null;
		while(!candidates.isEmpty()) {
			for (BufferedUserVisit buffered_visit : candidates) {
				if (instance.checkBudget(buffered_visit.visit) && !instance.results.contains(buffered_visit.visit)) {
					if (buffered_visit.cost_effectiveness >= max_cost_effectiveness) {
						double cost_effectiveness = instance.getMarginalGain(buffered_visit.visit) / buffered_visit.visit.maximum_cost;
						buffered_visit.cost_effectiveness = cost_effectiveness;
						if (cost_effectiveness > max_cost_effectiveness) {
							max_cost_effectiveness = cost_effectiveness;
							visit_max = buffered_visit;
						}
					}
				} else {
					deleted.add(buffered_visit);
				}
			}
			if (visit_max != null) {
				instance.updateResults(visit_max.visit);
				deleted.add(visit_max);
			} else {
				return;
			}
			candidates.removeAll(deleted);
			deleted.clear();
			max_cost_effectiveness = 0.0d;
			visit_max = null;
		}
	}
	
	public int statNumElements(){
		int count = 0;
		for (ActiveSetKnapsackBuffered inst : this.checkpoints.values()) {
			count += inst.statNumElements();
		}
		return count;
	}
	
	public double getResultValue() {
		if (this.index.getFirst() < this.start_id + Parameter.T - Parameter.W)
			return this.checkpoints.get(this.index.get(1)).getFunctionValue();
		else
			return this.checkpoints.get(this.index.get(0)).getFunctionValue();
	}
	
	public ActiveSetInstanceBuffered getResultInstance() {
		if (this.index.getFirst() < this.start_id + Parameter.T - Parameter.W)
			return this.checkpoints.get(this.index.get(1)).getCandidateInstance();
		else
			return this.checkpoints.get(this.index.get(0)).getCandidateInstance();
	}
}
