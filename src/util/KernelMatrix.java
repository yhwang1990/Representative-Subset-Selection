package util;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class KernelMatrix {
    public int size;
    private RealMatrix matrix;

    // initialize a new matrix of size n
    public KernelMatrix(int n) {
        this.size = n;
        this.matrix = new Array2DRowRealMatrix(n, n);
    }

    public KernelMatrix clone() {
        KernelMatrix cpy = new KernelMatrix(this.size);
        cpy.matrix = this.matrix.copy();
        return cpy;
    }

    // increase the size of matrix from n to (n+1), copy all existing values
    public void resize(int n) {
        if (this.size == n)
        	return;
        else if (this.size < n) {
        	RealMatrix tmp_mat = new Array2DRowRealMatrix(n, n);
            tmp_mat.setSubMatrix(this.matrix.getData(), 0, 0);
            this.matrix = tmp_mat;
        }
        this.size = n;
    }

    // set new entries in the last column and row
    public void setEntry(int n, double[] temp_vector) {
    	if (temp_vector.length < n - 1)
    		System.err.println("Set Entry: error in dimension");
    	
        for (int i = 0; i < n - 1; i++) {
            this.matrix.setEntry(i, n - 1, temp_vector[i]);
            this.matrix.setEntry(n - 1, i, temp_vector[i]);
        }
        this.matrix.setEntry(n - 1, n - 1, 2.0);
    }
    
    // set new entries in the last column and row
    public void setEntry(int n, int value) {
        for (int i = 0; i < n - 1; i++) {
        	this.matrix.setEntry(i, n - 1, value);
        	this.matrix.setEntry(n - 1, i, value);
        }
        this.matrix.setEntry(n - 1, n - 1, 2.0);
    }

    // get Log Determinant of matrix
    public double getLogDet() {
    	CholeskyDecomposition chol = new CholeskyDecomposition(this.matrix);
        return 0.5 * Math.log(chol.getDeterminant()) / Math.log(2.0);
    }

}
