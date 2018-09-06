package summarization.kw;

import java.util.ArrayList;
import java.util.LinkedList;

import bean.WeightedSet;
import it.unimi.dsi.fastutil.ints.Int2ReferenceRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import util.Parameter;

public class SummarizationHistogram {

	public LinkedList<Integer> index;
	public Int2ReferenceRBTreeMap<SummarizationKnapsack> checkpoints;
	public int start_id;
	
	SummarizationKnapsack result_instance;

	public SummarizationHistogram() {
		this.start_id = -1;
		this.index = new LinkedList<>();
		this.checkpoints = new Int2ReferenceRBTreeMap<>();
	}

	public void batchedInsert(ArrayList<WeightedSet> list_set) {

		this.start_id = list_set.get(Parameter.T - 1).id;
		if (this.checkpoints.size() > 0) {
			while (this.index.get(0).intValue() <= this.start_id - Parameter.W) {
				int deleted = this.index.removeFirst();
				this.checkpoints.remove(deleted);
			}
			
			for (SummarizationKnapsack streaming : this.checkpoints.values()) {
				for (WeightedSet set : list_set) {
					streaming.insert(set);
				}
			}
		}
		if (this.start_id % Parameter.L == 0) {
			SummarizationKnapsack new_streaming = new SummarizationKnapsack(this.start_id);
			for (WeightedSet set : list_set) {
				new_streaming.insert(set);
			}
			this.index.addLast(new_streaming.start_pos);
			this.checkpoints.put(new_streaming.start_pos, new_streaming);
		}
	}

	public long postprocessing() {
		this.result_instance = null;
		this.result_instance = new SummarizationKnapsack(this.checkpoints.get(this.index.get(0)));
		long t1 = System.nanoTime();
		for (WeightedSet set : SummarizationKW.WINDOW) {
			if (set.id >= this.result_instance.start_pos - Parameter.T + 1) {
				break;
			}
			this.result_instance.insert(set);
		}
		long t2 = System.nanoTime();
		return (t2 - t1);
	}

	public double getResultValue() {
		return this.result_instance.getFunctionValue();
	}

	public ObjectOpenHashSet<WeightedSet> getResultObject() {
		return this.result_instance.getResult();
	}
	
	public int statNumElements(){
		int count = 0;
		for (SummarizationKnapsack inst : this.checkpoints.values()) {
			count += inst.statNumElements();
		}
		return count;
	}
}
