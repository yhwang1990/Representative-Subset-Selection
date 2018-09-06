package bean;

import org.apache.commons.math3.linear.ArrayRealVector;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;
import util.Parameter;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class WeightedSet extends BaseObject {
	public int id;
	public Int2DoubleOpenHashMap word_weight;

	public WeightedSet(String str) {
		String[] split = str.split(":");
		this.id = Integer.parseInt(split[0]);
		
		this.maximum_gain = 0.0;
		this.word_weight = new Int2DoubleOpenHashMap();
		String[] token = split[1].split(" ");
		for (String s : token) {
			String[] pair = s.split(",");
			int word_id = Integer.parseInt(pair[0]);
			double weight = Double.parseDouble(pair[1]);
			this.maximum_gain += weight;
			this.word_weight.put(word_id, weight);
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
		StringBuilder builder = new StringBuilder();
		builder.append(this.id).append(":");
		for (Entry entry : this.word_weight.int2DoubleEntrySet()) {
			builder.append(entry.getIntKey()).append(",").append(entry.getDoubleValue() + " ");
		}
		builder.deleteCharAt(builder.length() - 1).append(":");
		builder.append(Parameter.VECTOR_FORMAT.format(this.budget_vector)).append("\n");

		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeightedSet other = (WeightedSet) obj;
		return (this.id == other.id);
	}
}
