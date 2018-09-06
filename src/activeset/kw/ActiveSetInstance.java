package activeset.kw;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;

import bean.BaseInstance;
import bean.UserVisit;
import util.Parameter;
import util.KernelMatrix;

public class ActiveSetInstance extends BaseInstance {

	private KernelMatrix kernel_matrix;
	
	public ArrayList<UserVisit> results;

	ActiveSetInstance(int instance_id, double optimal_value) {
		this.instance_id = instance_id;
		this.optimal_value = optimal_value;

		this.function_value = 0.0d;
		this.gain_threshold = this.optimal_value / Parameter.BASE;

		this.budget_vector = new ArrayRealVector(Parameter.d);
		this.budget_vector.set(0);

		this.results = new ArrayList<>();
		this.kernel_matrix = new KernelMatrix(1);
	}
	
	ActiveSetInstance(ActiveSetInstance inst) {
		this.instance_id = inst.instance_id;
		this.optimal_value = inst.optimal_value;

		this.function_value = inst.function_value;
		this.gain_threshold = inst.gain_threshold;

		this.budget_vector = new ArrayRealVector(Parameter.d);
		for (int i = 0; i < Parameter.d; i++) {
			this.budget_vector.setEntry(i, inst.budget_vector.getEntry(i));
		}

		this.results = new ArrayList<>();
		this.results.addAll(inst.results);
		
		this.kernel_matrix = inst.kernel_matrix.clone();
	}

	public double getMarginalGain(UserVisit visit) {
		double temp_gain;

		if (this.results.isEmpty()) {
			temp_gain = 0.5d;
			return temp_gain;
		}

		double[] temp_vector = new double[this.results.size()];
		int count = 0;
		for (UserVisit cur : this.results) {
			temp_vector[count++] = Parameter.squaredExponentialKernel(cur.feature_vector, visit.feature_vector);
		}
		
		int size = this.results.size();
		this.kernel_matrix.setEntry(size + 1, temp_vector);

		double new_value = this.kernel_matrix.getLogDet();
		temp_gain = new_value - this.function_value;

		return temp_gain;
	}

	public boolean checkUpdate(UserVisit visit) {
		if (!this.checkBudget(visit))
			return false;
		double max_budget = 0.0d;
		for (int i = 0; i < Parameter.d; i++)
			max_budget = (visit.budget_vector.getEntry(i) > max_budget) ? visit.budget_vector.getEntry(i) : max_budget;
		double temp_threshold = this.gain_threshold * max_budget;
		if (visit.maximum_gain < temp_threshold)
			return false;
		
		double marginal_gain = this.getMarginalGain(visit);
		return (marginal_gain >= temp_threshold);
	}

	public void updateResults(UserVisit visit) {
		double[] temp_vector = new double[this.results.size()];

		int count = 0;
		for (UserVisit cur : this.results) {
			temp_vector[count++] = Parameter.squaredExponentialKernel(cur.feature_vector, visit.feature_vector);
		}

		int size = this.results.size();
		this.kernel_matrix.resize(size + 1);
		this.kernel_matrix.setEntry(size + 1, temp_vector);
		this.function_value = this.kernel_matrix.getLogDet();
		
		this.kernel_matrix.resize(size + 2);
		this.kernel_matrix.setEntry(size + 2, 0);
		this.results.add(visit);

		this.updateBudget(visit);
	}
}
