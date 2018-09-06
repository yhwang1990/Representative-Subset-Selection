package activeset.kw;

import java.util.ArrayList;
import java.util.LinkedList;

import bean.UserVisit;
import it.unimi.dsi.fastutil.ints.Int2ReferenceRBTreeMap;
import util.Parameter;

public class ActiveSetHistogram {

	public int start_id;

	private LinkedList<Integer> index;
	private Int2ReferenceRBTreeMap<ActiveSetKnapsack> checkpoints;
	private ActiveSetKnapsack result_instance;

	ActiveSetHistogram() {
		this.start_id = -1;
		this.index = new LinkedList<>();
		this.checkpoints = new Int2ReferenceRBTreeMap<>();
	}

	public void batchedInsert(ArrayList<UserVisit> list_visit) {

		this.start_id = list_visit.get(Parameter.T - 1).id;

		if (this.checkpoints.size() > 0) {
			while (this.index.get(0) <= this.start_id - Parameter.W) {
				int deleted = this.index.removeFirst();
				this.checkpoints.remove(deleted);
			}
			
			for (ActiveSetKnapsack streaming : this.checkpoints.values()) {
				for (UserVisit visit : list_visit) {
					streaming.insert(visit);
				}
			}
		}

		if (this.start_id % Parameter.L == 0) {
			ActiveSetKnapsack new_streaming = new ActiveSetKnapsack(this.start_id);
			for (UserVisit visit : list_visit) {
				new_streaming.insert(visit);
			}
			this.index.addLast(new_streaming.getStartPos());
			this.checkpoints.put(new_streaming.getStartPos(), new_streaming);
		}
	}
	
	public long postprocessing() {
		this.result_instance = null;
		this.result_instance = new ActiveSetKnapsack(this.checkpoints.get(this.index.get(0)));
		long t1 = System.nanoTime();
		for (UserVisit visit : ActiveSetKW.WINDOW) {
			if (visit.id >= this.result_instance.getStartPos() - Parameter.T + 1) {
				break;
			}
			this.result_instance.insert(visit);
		}
		long t2 = System.nanoTime();
		return (t2 - t1);
	}
	
	public int getResultPos() {
		return this.result_instance.getStartPos();
	}

	public double getResultValue() {
		return this.result_instance.getFunctionValue();
	}

	public ArrayList<UserVisit> getResult() {
		return this.result_instance.getResult();
	}
}
