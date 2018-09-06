package summarization.kwplus;

import java.util.ArrayList;
import java.util.LinkedList;

import bean.BufferedSet;
import bean.WeightedSet;
import it.unimi.dsi.fastutil.ints.Int2ReferenceRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import util.Parameter;

public class SummarizationHistogramBuffered {

	public LinkedList<Integer> index;
	public Int2ReferenceRBTreeMap<SummarizationKnapsackBuffered> checkpoints;
	
	public int start_id;

	public SummarizationHistogramBuffered() {
		this.start_id = -1;
		this.index = new LinkedList<>();
		this.checkpoints = new Int2ReferenceRBTreeMap<>();
	}

	public void batchedInsert(ArrayList<WeightedSet> list_set) {

		this.start_id = list_set.get(0).id;
		
		while (this.index.size() > 2 && this.index.get(1).intValue() < this.start_id + Parameter.T - Parameter.W) {
			int deleted = this.index.removeFirst();
			this.checkpoints.remove(deleted);
		}

		if (this.checkpoints.size() > 0) {
			for (SummarizationKnapsackBuffered streaming : this.checkpoints.values()) {
				for (WeightedSet vertex : list_set) {
					streaming.insert(vertex);
				}
			}
		}

		SummarizationKnapsackBuffered new_streaming = new SummarizationKnapsackBuffered(this.start_id);
		for (WeightedSet set : list_set) {
			new_streaming.insert(set);
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

	int findNext(int cur, double cur_value) {
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
	
	public SummarizationInstanceBuffered post_processing() {
		SummarizationInstanceBuffered result;
		ArrayList<BufferedSet> candidates = new ArrayList<>();
		if (this.index.getFirst() < this.start_id + Parameter.T - Parameter.W) {
			SummarizationKnapsackBuffered checkpoint = this.checkpoints.get(this.index.get(1));
			result = checkpoint.getCandidateInstance();
			for (int instance_id = checkpoint.result_key; instance_id <= checkpoint.max; instance_id++) {
				SummarizationInstanceBuffered cur_inst = new SummarizationInstanceBuffered(checkpoint.instances.get(instance_id));
				for (BufferedSet buffered_set : checkpoint.instances.get(instance_id).buffer) {
					if (cur_inst.checkBudget(buffered_set.set)) {
						candidates.add(buffered_set);
					}
				}
				SummarizationInstanceBuffered prev_inst = this.checkpoints.get(this.index.get(0)).instances.get(instance_id);
				if (prev_inst != null) {
					for (WeightedSet set : prev_inst.results) {
						if (set.id >= this.start_id + Parameter.T - Parameter.W && !cur_inst.results.contains(set)) {
							candidates.add(new BufferedSet((WeightedSet) set, set.maximum_gain / set.maximum_cost));
						}
					}
					
					for (BufferedSet buffered_set : prev_inst.buffer) {
						if (buffered_set.set.id >= this.start_id + Parameter.T - Parameter.W && !cur_inst.results.contains(buffered_set.set)) {
							candidates.add(new BufferedSet(buffered_set.set, buffered_set.set.maximum_gain / buffered_set.set.maximum_cost));
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
			SummarizationKnapsackBuffered checkpoint = this.checkpoints.get(this.index.get(0));
			result = checkpoint.getCandidateInstance();
			for (int instance_id = checkpoint.result_key; instance_id <= checkpoint.max; instance_id++) {
				SummarizationInstanceBuffered cur_inst = new SummarizationInstanceBuffered(checkpoint.instances.get(instance_id));
				for (BufferedSet buffered_set : checkpoint.instances.get(instance_id).buffer) {
					if (cur_inst.checkBudget(buffered_set.set)) {
						candidates.add(buffered_set);
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
	
	private void greedy(SummarizationInstanceBuffered instance, ArrayList<BufferedSet> candidates) {
		double max_cost_effectiveness = 0.0d;
		ArrayList<BufferedSet> deleted = new ArrayList<>();
		BufferedSet set_max = null;
		while(!candidates.isEmpty()) {
			for (BufferedSet buffered_set : candidates) {
				if (instance.checkBudget(buffered_set.set) && ! instance.results.contains(buffered_set.set)) {
					if (buffered_set.cost_effectiveness >= max_cost_effectiveness) {
						double cost_effectiveness = instance.getMarginalGain(buffered_set.set) / buffered_set.set.maximum_cost;
						buffered_set.cost_effectiveness = cost_effectiveness;
						if (cost_effectiveness > max_cost_effectiveness) {
							max_cost_effectiveness = cost_effectiveness;
							set_max = buffered_set;
						}
					}
				} else {
					deleted.add(buffered_set);
				}
			}
			if (set_max != null) {
				instance.updateResults(set_max.set);
				deleted.add(set_max);
			} else {
				return;
			}
			candidates.removeAll(deleted);
			deleted.clear();
			max_cost_effectiveness = 0.0d;
			set_max = null;
		}
	}
	
	public int statNumElements(){
		int count = 0;
		for (SummarizationKnapsackBuffered inst : this.checkpoints.values()) {
			count += inst.statNumElements();
		}
		return count;
	}
}
