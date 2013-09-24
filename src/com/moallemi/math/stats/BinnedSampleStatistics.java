package com.moallemi.math.stats;

import java.util.Arrays;

/**
 * A binned binned sample statistics class, counting statistics across bins.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public class BinnedSampleStatistics {
    // bin edges
    private double[] binEdges;
    // statistics for each bin
    private MVSampleStatistics[] binStats;

    /**
     * Constructor.
     *
     * @param binEdges the bin edges, for example if { 0.0, 1.0, 2.0 }
     * is passed in, the bins become [0.0, 1.0), 
     * [1.0, 2.0). Should be sorted. This is copied by reference.
     */
    public BinnedSampleStatistics(final double[] binEdges) {
	this.binEdges = binEdges;
	for (int i = 1; i < binEdges.length; i++) {
	    if (binEdges[i-1] >= binEdges[i])
		throw new IllegalArgumentException("bin edges must be sorted");
	}

        binStats = new MVSampleStatistics [binEdges.length - 1];
        for (int i = 0; i < binStats.length; i++)
            binStats[i] = new MVSampleStatistics();
    }

    /**
     * Constructor.
     *
     * @param min minimum value (inclusive)
     * @param max maximum value (exclusive)
     * @param nbins number of bins
     */
    public BinnedSampleStatistics(final double min, 
                                  final double max, 
                                  final int nbins) 
    {
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
    public BinnedSampleStatistics(final double min, 
                                  final double max, 
                                  final int nbins,
                                  final boolean addInfNegative,
                                  final boolean addInfPositive) 
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

        if (nbins <= 0)
            throw new IllegalArgumentException("nbins must be positive");
	double dx = (max - min) / nbins;
	for (int i = 0; i <= nbins; i++)
	    binEdges[idx + i] = min + i*dx;

	if (addInfPositive) {
	    binEdges[binEdges.length - 1] = Double.POSITIVE_INFINITY;
	}

        binStats = new MVSampleStatistics [binEdges.length - 1];
        for (int i = 0; i < binStats.length; i++)
            binStats[i] = new MVSampleStatistics();
    }

    /**
     * Clear the statistics.
     */
    public void clear() {
        for (int i = 0; i < binStats.length; i++)
            binStats[i].clear();
    }


    /**
     * Is a point within the range of the bins?
     *
     * @param x point
     * @return <code>true</code> if the point is within range,
     * <code>false</code> otherwise
     */
    public boolean isInRange(final double x) {
	return x >= binEdges[0] && x < binEdges[binEdges.length-1];
    }

    /** 
     * Get the bin index of a point.
     *
     * @param x the point
     * @return the bin index, or -1 if it falls outside of the range of the bins
     */
    public int getBinIndex(final double x) {
	if (x < binEdges[0] || x >= binEdges[binEdges.length-1])
            return -1;
	int idx = Arrays.binarySearch(binEdges, x);
	if (idx < 0) 
	    idx = -idx - 2;
        return idx;
    }
    /** 
     * Add a point to the statistics.
     *
     * @param x point to add
     * @param v the value of the point
     * @throws IllegalArgumentException if the point is outside the
     * range of the bins
     */
    public void add(final double x, final double v) 
        throws IllegalArgumentException 
    {
        int idx = getBinIndex(x);
	if (idx < 0)
	    throw new IllegalArgumentException("outside of bin range");
	fastAdd(x, idx, v);
    }

    /** 
     * Add a point to the statistics, assuming you know the bin it belongs to.
     * This skips the binary search and is much faster.
     *
     * @param x point to add
     * @param idx the index of the bin
     * @param v the value of the point
     * @throws IllegalArgumentException if the point does not belong to the bin
     * in question
     */
    public void fastAdd(final double x, final int idx, final double v) 
        throws IllegalArgumentException
    {
	if (binEdges[idx] <= x && binEdges[idx+1] > x)
	    binStats[idx].addSample(v);
	else
	    throw 
		new IllegalStateException("failed to add point to histogram");
    }

    /** 
     * Add a point to the statistics, assuming you know the bin it belongs to.
     * This skips the binary search and is much faster.
     *
     * @param idx the index of the bin
     * @param v the value of the point
     * @throws IllegalArgumentException if the point does not belong to the bin
     * in question
     */
    public void fastAdd(final int idx, final double v) 
    {
        binStats[idx].addSample(v);
    }

    /**
     * Fetch the statistics of a bin.
     *
     * @param idx the bin
     * @return the statistics
     */
    public MVSampleStatistics getStatistics(final int idx) {
        return binStats[idx];
    }

    /**
     * Fetch the number of bins.
     *
     * @return the number of bins
     */
    public int getNumBins() { return binStats.length; }

    /**
     * Fetch the start of the ith bin.
     *
     * @param i which bin
     * @return the bin start (inclusive)
     */
    public double getBinStart(final int i) { return binEdges[i]; }

    /**
     * Fetch the end of the ith bin.
     *
     * @param i which bin
     * @return the bin end (exclusive)
     */
    public double getBinEnd(final int i) { return binEdges[i+1]; }

    /**
     * Return a string representation.
     *
     * @return a string representation
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < binStats.length; i++) {
            MVSampleStatistics stats = binStats[i];
            int count = stats.getCount();
	    
            double binStart = getBinStart(i);
            if (binStart == Double.NEGATIVE_INFINITY) 
                sb.append("( negInfinity ,");
            else
                sb.append(String.format("[ %f ,", binStart));
            
            double binEnd = getBinEnd(i);
            if (binEnd == Double.POSITIVE_INFINITY)
                sb.append(String.format(" posInfinity ): %d", count));
            else
                sb.append(String.format(" %f ): %d", binEnd, count));

            if (count >= 1) 
                sb.append(String.format(" %f", stats.getMean()));
            if (count >= 2)
                sb.append(String.format(" +- %f", 
                                        stats.getStandardDeviation()));
            sb.append("\n");
	}
	return sb.toString();
    }

    
}
