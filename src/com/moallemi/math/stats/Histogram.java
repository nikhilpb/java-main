package com.moallemi.math.stats;

import java.util.Arrays;

/**
 * A basic histogram class, counting points that fall into various bins.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public class Histogram {
    // bin edges
    private double[] binEdges;
    // number of points in each bin
    private int[] binCount;
    // total number of points
    private int totalCount;

    /**
     * Constructor.
     *
     * @param iBinEdges the bin edges, for example if { 0.0, 1.0, 2.0 }
     * is passed in, the bins become [0.0, 1.0), 
     * [1.0, 2.0). Should be sorted.
     */
    public Histogram(double[] iBinEdges) {
	binEdges = iBinEdges;
	for (int i = 1; i < binEdges.length; i++) {
	    if (binEdges[i-1] >= binEdges[i])
		throw new IllegalArgumentException("bin edges must be sorted");
	}
	binCount = new int [binEdges.length - 1];
    }

    /**
     * Constructor.
     *
     * @param min minimum value (inclusive)
     * @param max maximum value (exclusive)
     * @param nbins number of bins
     */
    public Histogram(double min, double max, int nbins) {
	this(min, max, nbins, false, false);
    }


    /**
     * Constructor.
     *
     * @param min minimum value (inclusive)
     * @param max maximum value (exclusive)
     * @param nbins number of bins
     * @param addInfNegative whether to add -infinity buckets
     * @param addInfPositive whether to add +infinity buckets
     */
    public Histogram(double min, 
		     double max, 
		     int nbins,
		     boolean addInfNegative,
		     boolean addInfPositive) 
    {
	int binArraySize = nbins + 1;
	if (addInfNegative) binArraySize++;
	if (addInfPositive) binArraySize++;
	binEdges = new double [binArraySize];
	int idx = 0;

	if (addInfNegative) {
	    binEdges[0] = Double.NEGATIVE_INFINITY;
	    idx++;
	}

	double dx = (max - min) / nbins;
	for (int i = 0; i <= nbins; i++)
	    binEdges[idx + i] = min + i*dx;

	if (addInfPositive) {
	    binEdges[binEdges.length - 1] = Double.POSITIVE_INFINITY;
	}

	binCount = new int [binEdges.length - 1];
    }

    /**
     * Clear the histogram.
     */
    public void clear() {
	Arrays.fill(binCount, 0);
	totalCount = 0;
    }


    /**
     * Is a point within the range of this histogram?
     *
     * @param x point
     * @return <code>true</code> if the point is within range,
     * <code>false</code> otherwise
     */
    public boolean isInRange(double x) {
	return !(x < binEdges[0] || x >= binEdges[binEdges.length-1]);
    }

    /** Add a point to the histogram.
     *
     * @param x point to add
     * @throws IllegalArgumentException if the point is outside the
     * histogram range
     */
    public void add(double x) throws IllegalArgumentException {
	if (x < binEdges[0] || x >= binEdges[binEdges.length-1])
	    throw new IllegalArgumentException("outside of histogram range");
	int idx = Arrays.binarySearch(binEdges, x);
	if (idx < 0) 
	    idx = -idx - 2;
	internalAdd(x, idx);
    }

    // paranoia
    private void internalAdd(double x, int idx) {
	if (binEdges[idx] <= x && binEdges[idx+1] > x) {
	    binCount[idx]++;
	    totalCount++;
	}
	else
	    throw 
		new IllegalStateException("failed to add point to histogram");
    }

    /**
     * Add a series of samples to a histogram.
     *
     * @param samples the samples
     * @throws IllegalArgumentException if a point is outside the
     * histogram range
     */
    public void add(SampleStatistics samples) throws IllegalArgumentException {
	int n = samples.getCount();
	for (int i = 0; i < n; i++)
	    add(samples.getSample(i));
    }


    /**
     * Fetch the number of points in a particular bin.
     *
     * @param idx the bin
     * @return the number of points
     */
    public int getBinCount(int idx) { return binCount[idx]; }

    /**
     * Fetch the total number of points.
     *
     * @return the total number of points
     */
    public int getTotalCount() { return totalCount; }

    /**
     * Fetch the number of bins.
     *
     * @return the number of bins
     */
    public int getNumBins() { return binCount.length; }

    /**
     * Fetch the start of the ith bin.
     *
     * @param i which bin
     * @return the bin start (inclusive)
     */
    public double getBinStart(int i) { return binEdges[i]; }

    /**
     * Fetch the end of the ith bin.
     *
     * @param i which bin
     * @return the bin end (exclusive)
     */
    public double getBinEnd(int i) { return binEdges[i+1]; }

    /**
     * Return a string representation.
     *
     * @return a string representation
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < binCount.length; i++) {
	    double binStart = getBinStart(i);
	    double binEnd = getBinEnd(i);
	    sb.append(binStart == Double.NEGATIVE_INFINITY
		      ? "(" : "[")
		.append(binStart)
		.append(",")
		.append(binEnd)
		.append("): ")
		.append(getBinCount(i))
		.append("\n");
	}
	return sb.toString();
    }

    
}
