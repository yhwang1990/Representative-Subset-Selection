package activeset.kwplus;

import java.util.ArrayList;
import java.util.PriorityQueue;

import org.apache.commons.math3.linear.ArrayRealVector;

import bean.BaseInstance;
import bean.BufferedUserVisit;
import bean.BufferedUserVisitAscending;
import bean.UserVisit;
import util.Parameter;
import util.KernelMatrix;

public class ActiveSetInstanceBuffered extends BaseInstance {

	private KernelMatrix kernel_matrix;
	public ArrayList<UserVisit> results;
	public PriorityQueue<BufferedUserVisit> buffer;

	ActiveSetInstanceBuffered(int instance_id, double optimal_value) {
		this.instance_id = instance_id;
		this.optimal_value = optimal_value;

		this.function_value = 0.0d;
		this.gain_threshold = this.optimal_value / Parameter.BASE;

		this.budget_vector = new ArrayRealVector(Parameter.d);
		this.budget_vector.set(0);

		this.results = new ArrayList<>();
		this.kernel_matrix = new KernelMatrix(1);
		
		this.buffer = new PriorityQueue<>(Parameter.ETA + 1, new BufferedUserVisitAscending());
	}

	ActiveSetInstanceBuffered(ActiveSetInstanceBuffered instance) {
		this.instance_id = instance.instance_id;
		this.optimal_value = instance.optimal_value;

		this.function_value = instance.function_value;
		this.gain_threshold = instance.gain_threshold;

		this.budget_vector = instance.budget_vector.copy();

		this.results = new ArrayList<>();
		this.results.addAll(instance.results);
		this.kernel_matrix = instance.kernel_matrix.clone();
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
		double temp_threshold = this.gain_threshold * visit.maximum_cost;
		if (visit.maximum_gain < Parameter.ALPHA * temp_threshold)
			return false;
		
		double marginal_gain = this.getMarginalGain(visit);

		if (marginal_gain >= Parameter.ALPHA * temp_threshold && marginal_gain < temp_threshold) {
			BufferedUserVisit new_buffered_visit = new BufferedUserVisit(visit, marginal_gain / visit.maximum_cost);
			this.buffer.offer(new_buffered_visit);
			if (this.buffer.size() > Parameter.ETA) {
				ArrayList<BufferedUserVisit> deleted = new ArrayList<>();
				for (BufferedUserVisit buffered_visit : this.buffer) {
					if (!this.checkBudget(buffered_visit.visit)) {
						deleted.add(buffered_visit);
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
