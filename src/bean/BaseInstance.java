package bean;

import org.apache.commons.math3.linear.RealVector;

import util.Parameter;

public abstract class BaseInstance {

	public int instance_id;
	public double optimal_value, function_value, gain_threshold;
	public RealVector budget_vector;

	public boolean checkBudget(BaseObject obj) {
		for (int i = 0; i < Parameter.d; i++) {
			if (this.budget_vector.getEntry(i) + obj.budget_vector.getEntry(i) > 1.0d + 1.0e-9)
				return false;
		}
		return true;
	}

	public void updateBudget(BaseObject obj) {
		for (int i = 0; i < Parameter.d; i++)
			this.budget_vector.addToEntry(i, obj.budget_vector.getEntry(i));
	}
	
}
