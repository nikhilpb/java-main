package com.moallemi.math;

import Jama.Matrix;

/**
 * An n-dimensional real vector.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class VectorNd {
    // underlying data
    protected double[] v;

    /**
     * Construct a zero vector.
     *
     * @param dim the dimension of the vector
     */
    public VectorNd(int dim) {
	v = new double [dim];
    }

    /**
     * Construct a vector with a specific initial value.
     *
     * @param x the initial value
     */
    public VectorNd(double[] x) {
	v = new double [x.length];
	System.arraycopy(x, 0, v, 0, x.length);
    }

    /**
     * Construct from the row of a matrix.
     *
     * @param m the matrix
     * @param index the row
     */
    public void extractMatrixRow(Matrix m, int index) {
	if (m.getColumnDimension() != v.length)
	    throw new IllegalArgumentException("incorrect matrix dimension");
	for (int i = 0; i < v.length; i++) 
	    v[i] = m.get(index, i);
    }

    /**
     * Construct from the column of a matrix.
     *
     * @param m the matrix
     * @param index the column
     */
    public void extractMatrixColumn(Matrix m, int index) {
	if (m.getRowDimension() != v.length)
	    throw new IllegalArgumentException("incorrect matrix dimension");
	for (int i = 0; i < v.length; i++) 
	    v[i] = m.get(i, index);
    }

    /**
     * Set a matrix row to the value of this vector.
     *
     * @param m the matrix
     * @param index the row
     */
    public void toMatrixRow(Matrix m, int index) {
	if (m.getColumnDimension() != v.length)
	    throw new IllegalArgumentException("incorrect matrix dimension");
	for (int i = 0; i < v.length; i++)
	    m.set(index, i, v[i]);
    }

    /**
     * Set a matrix column to the value of this vector.
     *
     * @param m the matrix
     * @param index the column
     */
    public void toMatrixColumn(Matrix m, int index) {
	if (m.getRowDimension() != v.length)
	    throw new IllegalArgumentException("incorrect matrix dimension");
	for (int i = 0; i < v.length; i++)
	    m.set(i, index, v[i]);
    }


    /**
     * Get the dimension.
     *
     * @return the dimension
     */
    public int getDimension() { return v.length; }

    /**
     * Get the value at a particular index.
     *
     * @param index the index
     * @return value of the vector at the index
     */
    public double get(int index) { return v[index]; }

    /**
     * Set the value at a particular index.
     *
     * @param index the index
     * @param value the value
     */
    public void set(int index, double value) { v[index] = value; }


    /**
     * Get the value of the vector as an array.
     *
     * @return the value of the vector
     */
    public double[] toArray() {
	double[] x = new double [v.length];
	System.arraycopy(v, 0, x, 0, v.length);
	return x;
    }

    // utility to check dimension compatibility
    private void checkDimension(VectorNd other) 
	throws IllegalArgumentException 
    {
	if (this.v.length != other.v.length)
	    throw new IllegalArgumentException("vector dimensions do "
					       + "not match");
    }
    

    /**
     * Set each component of this vector to it's absolute value.
     */
    public void absolute() {
	for (int i = 0; i < v.length; i++)
	    v[i] = Math.abs(v[i]);
    }

    /**
     * Compute the L1 distance to another vector.
     *
     * @param other the other vector
     * @return the L1 distance
     */
    public double distanceL1(VectorNd other) {
	checkDimension(other);
	double distance = 0.0;
	for (int i = 0; i < v.length; i++) {
	    double diff = Math.abs(v[i] - other.v[i]);
	    if (diff > distance)
		distance = diff;
	}
	return distance;
    }

    /**
     * Compute the L2 distance squared to another vector.
     *
     * @param other the other vector
     * @return the L2 distance squared
     */
    public double distanceL2Squared(VectorNd other) {
	checkDimension(other);
	double distance = 0.0;
	for (int i = 0; i < v.length; i++) {
	    double x = v[i] - other.v[i];
	    distance += x*x;
	}
	return distance;
    }

   /**
     * Compute the L2 distance to another vector.
     *
     * @param other the other vector
     * @return the L2 distance
     */
    public double distanceL2(VectorNd other) {
	return Math.sqrt(distanceL2Squared(other));
    }


    /**
     * Compute the L-infinity distance to another vector.
     *
     * @param other the other vector
     * @return the L-infinity distance
     */
    public double distanceLinf(VectorNd other) {
	checkDimension(other);
	double distance = 0.0;
	for (int i = 0; i < v.length; i++) {
	    double x = Math.abs(v[i] - other.v[i]);
	    if (x > distance)
		distance = x;
	}
	return distance;
    }

    /**
     * Return a string representation of this vector.
     *
     * @return a string representation
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < v.length; i++) {
	    if (i > 0)
		sb.append(",");
	    sb.append(v[i]);
	}
	return sb.toString();
    }

}
