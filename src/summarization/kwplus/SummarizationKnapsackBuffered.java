package summarization.kwplus;

import bean.WeightedSet;
import it.unimi.dsi.fastutil.ints.Int2ReferenceRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import util.Parameter;

public class SummarizationKnapsackBuffered {

	public int start_pos;

	public int result_key;
	double result_value;

	public int min, max;
	double min_weight, max_weight;

	public Int2ReferenceRBTreeMap<SummarizationInstanceBuffered> instances;

	public SummarizationKnapsackBuffered(int start_pos) {
		this.start_pos = start_pos;

		this.result_key = Integer.MAX_VALUE;
		this.result_value = 0.0d;

		this.min = 0;
		this.max = 0;
		this.min_weight = 0.0d;
		this.max_weight = 0.0d;

		this.instances = new Int2ReferenceRBTreeMap<>();
	}

	public void maintainInstances(WeightedSet set) {
		double max_weight = 0.0d;
		double min_weight = Math.max(set.maximum_gain, this.result_value);

		for (int i = 0; i < Parameter.d; i++) {
			if (set.maximum_gain / set.budget_vector.getEntry(i) > max_weight) {
				max_weight = set.maximum_gain / set.budget_vector.getEntry(i);
			}
		}

		if (max_weight > this.max_weight || min_weight > this.min_weight) {
			this.max_weight = Math.max(max_weight, this.max_weight);
			this.min_weight = Math.max(min_weight, this.min_weight);
			int old_min = this.min, old_max = this.max;
			this.min = (int) (Math.log(this.min_weight) / Math.log(Parameter.LOG_BASE)) + 1;
			this.max = (int) (Math.log(Parameter.BASE * this.max_weight) / Math.log(Parameter.LOG_BASE));

			if (this.instances.size() > 0) {
				if (old_max >= this.min) {
					for (int i = old_min; i < this.min; i++) {
						this.instances.remove(i);
					}
					for (int i = old_max + 1; i <= this.max; i++) {
						double new_value = Math.pow(Parameter.LOG_BASE, i);
						SummarizationInstanceBuffered new_instance = new SummarizationInstanceBuffered(i, new_value);
						this.instances.put(i, new_instance);
					}
				} else {
					this.instances.clear();
					for (int i = this.min; i <= this.max; i++) {
						double new_value = Math.pow(Parameter.LOG_BASE, i);
						SummarizationInstanceBuffered new_instance = new SummarizationInstanceBuffered(i, new_value);
						this.instances.put(i, new_instance);
					}
				}
			} else {
				for (int i = this.min; i <= this.max; i++) {
					double new_value = Math.pow(Parameter.LOG_BASE, i);
					SummarizationInstanceBuffered new_instance = new SummarizationInstanceBuffered(i, new_value);
					this.instances.put(i, new_instance);
				}
			}
		}
	}

	public void insert(WeightedSet set) {
		this.maintainInstances(set);

		if (!this.instances.containsKey(this.result_key)) {
			this.result_key = Integer.MAX_VALUE;
			this.result_value = 0.0d;
			
			for(SummarizationInstanceBuffered inst : this.instances.values()) {
				if (inst.function_value > this.result_value) {
					this.result_key = inst.instance_id;
					this.result_value = inst.function_value;
				}
			}
		}

		for (SummarizationInstanceBuffered instance : this.instances.values()) {
			if (instance.checkUpdate(set)) {
				instance.updateResults(set);
				if (instance.function_value > this.result_value) {
					this.result_key = instance.instance_id;
					this.result_value = instance.function_value;
				}
			}
		}
	}

	public SummarizationInstanceBuffered getCandidateInstance() {
		return this.instances.get(this.result_key);
	}

	public double getFunctionValue() {
		return this.result_value;
	}

	public int getResultKey() {
		return this.result_key;
	}
	
	public int statNumElements(){
		int count = 0;
		for (SummarizationInstanceBuffered inst : instances.values()) {
			count += inst.results.size();
			count += inst.buffer.size();
		}
		return count;
	}

	public ObjectOpenHashSet<WeightedSet> getResult() {
		return this.instances.get(this.result_key).results;
	}
}
