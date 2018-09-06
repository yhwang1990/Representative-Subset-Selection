package activeset.kw;

import java.util.ArrayList;

import bean.UserVisit;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap.Entry;
import util.Parameter;
import it.unimi.dsi.fastutil.ints.Int2ReferenceRBTreeMap;

public class ActiveSetKnapsack {

	private int start_pos;

	private int result_key;
	private double result_value;

	private int min, max;
	private double min_weight, max_weight;

	public Int2ReferenceRBTreeMap<ActiveSetInstance> instances;

	ActiveSetKnapsack(int start_pos) {
		this.start_pos = start_pos;

		this.result_key = Integer.MAX_VALUE;
		this.result_value = 0.0d;

		this.min = 0;
		this.max = 0;
		this.min_weight = 0.0d;
		this.max_weight = 0.0d;

		this.instances = new Int2ReferenceRBTreeMap<>();
	}
	
	ActiveSetKnapsack(ActiveSetKnapsack streaming) {
		this.start_pos = streaming.start_pos;

		this.result_key = streaming.result_key;
		this.result_value = streaming.result_value;

		this.min = streaming.min;
		this.max = streaming.max;
		this.min_weight = streaming.min_weight;
		this.max_weight = streaming.max_weight;

		this.instances = new Int2ReferenceRBTreeMap<>();
		for (Entry<ActiveSetInstance> entry : streaming.instances.int2ReferenceEntrySet()) {
			this.instances.put(entry.getIntKey(), new ActiveSetInstance(entry.getValue()));
		}
	}

	private void maintainInstances(UserVisit visit) {
		double max_weight = 0.0d, min_weight;

		min_weight = visit.maximum_gain;
		
		for (int i = 0; i < Parameter.d; i++) {
			if (visit.maximum_gain / visit.budget_vector.getEntry(i) > max_weight) {
				max_weight = visit.maximum_gain / visit.budget_vector.getEntry(i);
			}
		}

		if (max_weight > this.max_weight || min_weight > this.min_weight) {
			this.max_weight = Math.max(max_weight, this.max_weight);
			this.min_weight = Math.max(min_weight, this.min_weight);
			int old_min = this.min, old_max = this.max;
			this.min = (int) (Math.log(this.min_weight) / Math.log(Parameter.LOG_BASE)) + 1;
			this.max = (int) (Math.log(this.max_weight * Parameter.BASE) / Math.log(Parameter.LOG_BASE));

			if (this.instances.size() > 0) {
				if (old_max >= this.min) {
					for (int i = old_min; i < this.min; i++) {
						this.instances.remove(i);
					}
					for (int i = old_max + 1; i <= this.max; i++) {
						double new_value = Math.pow(Parameter.LOG_BASE, i);
						ActiveSetInstance new_instance = new ActiveSetInstance(i, new_value);
						this.instances.put(i, new_instance);
					}
				} else {
					this.instances.clear();
					for (int i = this.min; i <= this.max; i++) {
						double new_value = Math.pow(Parameter.LOG_BASE, i);
						ActiveSetInstance new_instance = new ActiveSetInstance(i, new_value);
						this.instances.put(i, new_instance);
					}
				}
			} else {
				for (int i = this.min; i <= this.max; i++) {
					double new_value = Math.pow(Parameter.LOG_BASE, i);
					ActiveSetInstance new_instance = new ActiveSetInstance(i, new_value);
					this.instances.put(i, new_instance);
				}
			}
		}
	}

	public void insert(UserVisit visit) {
		this.maintainInstances(visit);

		if (!this.instances.containsKey(this.result_key)) {
			this.result_key = Integer.MAX_VALUE;
			this.result_value = 0.0d;
		}

		for (ActiveSetInstance instance : this.instances.values()) {
			boolean update;
			update = instance.checkUpdate(visit);

			if (update) {
				instance.updateResults(visit);
				if (instance.function_value > this.result_value) {
					this.result_key = instance.instance_id;
					this.result_value = instance.function_value;
				}
			}
		}
	}

	public int getStartPos() {
		return this.start_pos;
	}

	public ActiveSetInstance getCandidateInstance() {
		return this.instances.get(this.result_key);
	}

	public double getFunctionValue() {
		return this.result_value;
	}

	public int getResultKey() {
		return this.result_key;
	}

	public ArrayList<UserVisit> getResult() {
		return this.instances.get(this.result_key).results;
	}
}
