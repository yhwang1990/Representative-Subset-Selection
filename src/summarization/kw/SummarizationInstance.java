package summarization.kw;

import org.apache.commons.math3.linear.ArrayRealVector;

import bean.BaseInstance;
import bean.WeightedSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import util.Parameter;

public class SummarizationInstance extends BaseInstance {

	public Int2DoubleOpenHashMap coverage;
	public ObjectOpenHashSet<WeightedSet> results;

	public SummarizationInstance(int instance_id, double optimal_value) {
		this.instance_id = instance_id;
		this.optimal_value = optimal_value;

		this.function_value = 0.0d;
		this.gain_threshold = this.optimal_value / Parameter.BASE;

		this.budget_vector = new ArrayRealVector(Parameter.d);
		this.budget_vector.set(0);

		this.results = new ObjectOpenHashSet<>();
		this.coverage = new Int2DoubleOpenHashMap();
	}
	
	public SummarizationInstance(SummarizationInstance inst) {
		this.instance_id = inst.instance_id;
		this.optimal_value = inst.optimal_value;

		this.function_value = inst.function_value;
		this.gain_threshold = inst.gain_threshold;

		this.budget_vector = new ArrayRealVector(Parameter.d);
		for (int i = 0; i < Parameter.d; i++) {
			this.budget_vector.setEntry(i, inst.budget_vector.getEntry(i));
		}

		this.results = new ObjectOpenHashSet<>();
		this.results.addAll(inst.results);
		
		this.coverage = new Int2DoubleOpenHashMap();
		this.coverage.putAll(inst.coverage);
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
		if (set.maximum_gain < temp_threshold)
			return false;
		
		double marginal_gain = this.getMarginalGain(set);

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
