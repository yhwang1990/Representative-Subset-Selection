package util;

import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealVectorFormat;


import bean.UserVisit;
import bean.WeightedSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class Parameter {

	public static int W;
	public static int L;
	public static int T;

	// For Yahoo! webscope dataset, each feature vector is 5-dimensional
	public static final int DIM_FEATURE = 5;
	
	public static int d;
	
	public static final int ETA = 20;
	public static final double ALPHA = 0.5d;

	public static double EPSILON;
	public static double BETA;

	public static double c;

	public static double BASE;
	public static double LOG_BASE;

	public static RealVectorFormat VECTOR_FORMAT = new RealVectorFormat("", "", ",");

	public static double squaredExponentialKernel(RealVector v1, RealVector v2) {
		return Math.exp(-(Math.pow(v1.getDistance(v2), 2) / Math.pow(0.75d, 2)));
	}
	
	public static double evaluateLogDeterminant(ArrayList<UserVisit> list_visit) {
		if (list_visit.isEmpty())
			return 0.0;
		RealMatrix kernel_matrix = new Array2DRowRealMatrix(list_visit.size(), list_visit.size());
		for (int i = 0; i < list_visit.size(); i++) {
			for (int j = 0; j < list_visit.size(); j++) {
				kernel_matrix.setEntry(i, j, squaredExponentialKernel(list_visit.get(i).feature_vector, list_visit.get(j).feature_vector));
			}
		}
		for (int i = 0; i < list_visit.size(); i++) {
			kernel_matrix.setEntry(i, i, 2.0);
		}
		
		CholeskyDecomposition chol = new CholeskyDecomposition(kernel_matrix);
        return 0.5d * Math.log(chol.getDeterminant()) / Math.log(2.0d);
	}
	
	public static double evaluateWeight(ArrayList<WeightedSet> list_set) {
		if (list_set.isEmpty())
			return 0.0d;
		Int2DoubleOpenHashMap coverage = new Int2DoubleOpenHashMap();
		for (WeightedSet s : list_set) {
			coverage.putAll(s.word_weight);
		}
		double weight = 0.0;
		for (double value : coverage.values()) {
			weight += value;
		}
		return weight;
	}
}
