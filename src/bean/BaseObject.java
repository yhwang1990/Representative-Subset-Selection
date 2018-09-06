package bean;

import java.util.Comparator;
import org.apache.commons.math3.linear.RealVector;

public abstract class BaseObject implements Comparator<BaseObject> {
	public int id;
	public double maximum_gain;
	public RealVector budget_vector;
	public double maximum_cost;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseObject other = (BaseObject) obj;
		return (id == other.id);
	}

	@Override
	public int compare(BaseObject o1, BaseObject o2) {
		return (o1.id - o2.id);
	}
}
