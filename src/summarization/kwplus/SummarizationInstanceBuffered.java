package summarization.kwplus;

import java.util.ArrayList;
import java.util.PriorityQueue;

import org.apache.commons.math3.linear.ArrayRealVector;

import bean.BaseInstance;
import bean.BufferedSet;
import bean.BufferedSetAscending;
import bean.WeightedSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import util.Parameter;

public class SummarizationInstanceBuffered extends BaseInstance {

	public Int2DoubleOpenHashMap coverage;
	public ObjectOpenHashSet<WeightedSet> results;
	public PriorityQueue<BufferedSet> buffer;

	public SummarizationInstanceBuffered(int instance_id, double optimal_value) {
		this.instance_id = instance_id;
		this.optimal_value = optimal_value;

		this.function_value = 0.0d;
		this.gain_threshold = this.optimal_value / Parameter.BASE;

		this.budget_vector = new ArrayRealVector(Parameter.d);
		this.budget_vector.set(0);

		this.results = new ObjectOpenHashSet<>();
		this.coverage = new Int2DoubleOpenHashMap();

		this.buffer = new PriorityQueue<>(Parameter.ETA + 1, new BufferedSetAscending());
	}

	public SummarizationInstanceBuffered(SummarizationInstanceBuffered instance) {
		this.instance_id = instance.instance_id;
		this.optimal_value = instance.optimal_value;

		this.function_value = instance.function_value;
		this.gain_threshold = instance.gain_threshold;

		this.budget_vector = instance.budget_vector.copy();

		this.results = instance.results.clone();
		this.coverage = instance.coverage.clone();
	}

	public double getMarginalGain(WeightedSet set) {
		double temp_gain = 0;

		if (this.results.isEmpty()) {
			return set.maximum_gain;
		}

		for (int word_id : set.word_weight.keySet()) {
			if (!this.coverage.containsKey(word_id)) {
				temp_gain += set.word_weight.get(word_id);
			}
		}

		return temp_gain;
	}

	public boolean checkUpdate(WeightedSet set) {
		if (!this.checkBudget(set))
			return false;

		double temp_threshold = this.gain_threshold * set.maximum_cost;
		if (set.maximum_gain < Parameter.ALPHA * temp_threshold)
			return false;

		double marginal_gain = this.getMarginalGain(set);
		if (marginal_gain >= Parameter.ALPHA * temp_threshold && marginal_gain < temp_threshold) {
			BufferedSet new_buffered_set = new BufferedSet(set, marginal_gain / set.maximum_cost);
			this.buffer.offer(new_buffered_set);
			if (this.buffer.size() > Parameter.ETA) {
				ArrayList<BufferedSet> deleted = new ArrayList<>();
				for (BufferedSet buffered_set : this.buffer) {
					if (!this.checkBudget(buffered_set.set)) {
						deleted.add(buffered_set);
					}
				}
				if (!deleted.isEmpty()) {
					this.buffer.removeAll(deleted);
					deleted.clear();
				}
				while (this.buffer.size() > Parameter.ETA) {
					this.buffer.poll();
				}
			}
		}

		return (marginal_gain >= temp_threshold);
	}

	public void updateResults(WeightedSet set) {
		this.results.add(set);
		for (int word_id : set.word_weight.keySet()) {
			if (! this.coverage.containsKey(word_id)) {
				this.coverage.put(word_id, set.word_weight.get(word_id));
				this.function_value += set.word_weight.get(word_id);
			}
		}
		this.updateBudget(set);
	}
}
