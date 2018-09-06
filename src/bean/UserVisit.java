package bean;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import util.Parameter;

public class UserVisit extends BaseObject {
	public RealVector feature_vector;

	public UserVisit(String str) {
		String[] split = str.split(":");
		this.id = Integer.parseInt(split[0]);
		
		this.maximum_gain = 0.5d;
		this.feature_vector = new ArrayRealVector(Parameter.DIM_FEATURE);
		String[] features = split[1].split(",");
		for (int i = 0; i < Parameter.DIM_FEATURE; i++) {
			this.feature_vector.setEntry(i, Double.parseDouble(features[i]));
		}
		
		this.budget_vector = new ArrayRealVector(Parameter.d);
		this.maximum_cost = 0.0d;
		String[] costs = split[2].split(",");
		for (int i = 0; i < Parameter.d; i++) {
			double cost = Double.parseDouble(costs[i]) * Parameter.c;
			this.budget_vector.setEntry(i, cost);
			this.maximum_cost = Math.max(this.maximum_cost, cost);
		}
	}

	@Override
	public String toString() {
		return String.valueOf(this.id) + ":" +
				Parameter.VECTOR_FORMAT.format(this.feature_vector) + ":" +
				Parameter.VECTOR_FORMAT.format(this.budget_vector) + "\n";
	}

}
