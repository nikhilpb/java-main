package com.moallemi.math.stats;

import java.util.Arrays;

/**
 * Class for maintaining statistics on a set of samples.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2009-02-13 04:54:21 $
 */
public class SampleStatistics {
    private double[] samples;
    private double sum;
    private double min, max;
    private int count;
    private boolean sorted;


    /**
     * Constructor.
     */
    public SampleStatistics() {
	this(128);
    }

    /**
     * Constructor.
     *
     * @param size number of sample to preallocate
     */
    public SampleStatistics(int size) {
	samples = new double [size];
    }


    /**
     * Constructor.
     *
     * Creates a new SampleStatistics
     * from another SampleStatistics.
     * @param s The sample statistics to copy
     */
    public SampleStatistics(SampleStatistics s) {
	this(s.getCount());
	for(int i = 0; i < s.samples.length; i++) {
	    addSample(s.samples[i]);
	}
    }

    /**
     * Clear any samples collected thus far.
     */
    public void clear() {
	count = 0;
	sum =  0.0;
	sorted = false;
    }


    /**
     * Add a sample.
     *
     * @param x sample value to add
     */
    public void addSample(double x) {
	if (count >= samples.length) {
	    double[] tmp = new double [2 * count];
	    System.arraycopy(samples, 0, tmp, 0, count);
	    samples = tmp;
	}
	samples[count] = x;
	sum += x;

	if (count == 0) 
	    min = max = x;
	else if (x < min)
	    min = x;
	else if (x > max)
	    max = x;

	count++;
	sorted = false;
    }
    
    /**
     * Fetch the number of samples.
     *
     * @return the number of samples
     */
    public int getCount() { return count; }

    /**
     * Get a sample. Note that samples may not necessarily be in the order
     * they were inserted in.
     *
     * @param index the sample index
     */
    public double getSample(int index) { 
	if (index < count)
	    return samples[index];
	throw new ArrayIndexOutOfBoundsException();
    }


    /**
     * Fetch the mean of the samples.
     * 
     * @return the mean
     * @throws ArithmeticException if there are no samples
     */
    public double getMean() throws ArithmeticException {
	if (count <= 0)
	    throw new ArithmeticException("no samples");
	return sum / count;
    }

    /**
     * Fetch the sum of the samples
     * 
     * @return the sum of samples
     * @throws ArithmeticException if there are no samples
     */
    public double getSum() throws ArithmeticException {
	if (count <= 0)
	    throw new ArithmeticException("no samples");
	return sum;
    }
    
    
    // utility function, compute sum_i (x_i - mean)^2
    // robustly
    private double getSumSquaredErrors() {
        // use a two-pass variance calculation not vulnerable to rounding
	// error to compute sum of squared deviations, see Numerical
	// Recipes
	
	double mean = sum / count;
	double sse = 0.0;
	double ep = 0.0;
	for (int i = 0; i < count; i++) {
	    double s = samples[i] - mean;
	    sse += s*s;
	    ep += s;
	}
	// if there was no rounding error, ep would be 0.0, but in practice
	// ep has roughly the same error as the variance, so subtraction
	// reduces the rounding error
	sse -= ep*ep/count;

        if (sse < 0.0)
            throw new ArithmeticException("gross rounding error: "
                                          + "negative variance");
        return sse;
    }


    /**
     * Fetch the standard deviation of the samples.
     * This is the sample standard deviation and not the 
     * population standard deviation.
     * 
     * @return the standard deviation
     * @throws ArithmeticException if there are not at least 2 samples
     */
    public double getStandardDeviation() 
	throws ArithmeticException
    {
	if (count < 2)
	    throw new ArithmeticException("need 2 samples for "
					  + "standard deviation");

	return Math.sqrt(getSumSquaredErrors() / (count - 1));  
    }

    /**
     * Fetch the population standard deviation.
     * 
     * @return the standard deviation
     * @throws ArithmeticException if there are not at least 2 samples
     * or in the case of gross rounding error (negative variance)
     */
    public double getStandardDeviationPopulation()
	throws ArithmeticException
    {
	if (count < 1)
	    throw new ArithmeticException("need 1 samples for "
					  + "standard deviation");

	return Math.sqrt(getSumSquaredErrors() / count);	    
    }

    /**
     * Fetch the sum of squares of the samples.
     * 
     * @return the sum of squares
     * @throws ArithmeticException if there is not at least 1 sample
     */
    public double getSumSquares()
	throws ArithmeticException
    {
	if (count < 1)
	    throw new ArithmeticException("need at least 1 samples");

	double ss = 0.0;
	for (int i = 0; i < count; i++) {
	    double s = samples[i];
	    ss += s*s;
	}

        return ss;
    }

    /**
     * Get the root mean squared error versus mean. (This is the same as the
     * population standard deviation.)
     * 
     * @return the RMS error
     * @throws ArithmeticException if there are not at least 2 samples
     */
    public double getRMSError() 
	throws ArithmeticException
    {
	return getStandardDeviationPopulation();
    }

    /**
     * Get the root mean squared error versus a specified mean.
     * 
     * @param mean the mean to use
     * @return the RMS error
     * @throws ArithmeticException if there are not at least 2 samples
     */
    public double getRMSError(double mean) 
	throws ArithmeticException
    {
	if (count < 1)
	    throw new ArithmeticException("need 1 sample");

	double mse = 0.0;
	for (int i = 0; i < count; i++) {
	    double s = samples[i] - mean;
	    mse += s*s;
	}
	mse /= count;
	return Math.sqrt(mse);  
    }

    /**
     * Fetch the standard error, assuming that the samples are IID
     * observations.
     * 
     * @return the standard deviation
     * @throws ArithmeticException if there are not at least 2 samples
     * or in the case of gross rounding error (negative variance)
     */
    public double getStandardError()
	throws ArithmeticException
    {
	return getStandardDeviation() / Math.sqrt(count);	    
    }

    /**
     * Fetch the minimum of the samples.
     * 
     * @return the minimum
     * @throws ArithmeticException if there are no samples
     */
    public double getMinimum() {
	if (count <= 0)
	    throw new ArithmeticException("no samples");
	return min;
    }

    /**
     * Fetch the maximum of the samples.
     * 
     * @return the maximum
     * @throws ArithmeticException if there are no samples
     */
    public double getMaximum() {
	if (count <= 0)
	    throw new ArithmeticException("no samples");
	return max;
    }

    /**
     * Fetch the median of the samples.
     * 
     * @return the median
     * @throws ArithmeticException if there are no samples
     */
    public double getMedian() {
	return getOrderStatistic(count / 2);
    }


    /**
     * Fetch a quantile. Averages between neighboring points.
     *
     * @param p what probability threshold
     * @return the quantile value
     * @throws ArithmeticException if there are not enough samples
     */
    public double getQuantile(final double p) {
	if (p < 0.0 || p > 1.0)
	    throw new ArithmeticException("bad probability");
	if (p == 0.0)
	    return min;
	if (p == 1.0)
	    return max;
        int i1 = (int) Math.floor((count - 1) * p);
        double f = (count - 1)*p - i1;
        return (1.0 - f) * getOrderStatistic(i1)
            + f * getOrderStatistic(i1+1);
    }

    /**
     * Fetch the ith order statistic, that is the ith largest element.
     *
     * @param i which order statistic
     * @return the order statistic value
     * @throws ArithmeticException if there are not enough samples
     */
    public double getOrderStatistic(int i) {
	if (i >= count || i < 0)
	    throw new ArithmeticException("bad order statistic");
	if (i == 0)
	    return min;
	if (i == count - 1)
	    return max;
	if (sorted)
	    return samples[i];
	return select(samples, 0, count, i);
    }


    /**
     * Sort the sample array. Provides for quick order statistics.
     */
    public void sort() { 
	if (!sorted) {
	    Arrays.sort(samples, 0, count);
	    sorted = true;
	}
    }
	

    /**
     * Are the sample statistics in sorted order?
     *
     * @return <code>true</code> if the are sorted, <code>false</code>
     * otherwise
     */
    public boolean isSorted() { return sorted; }

    /**
     * Count the number of samples below a given value.
     *
     * @param min minimum value (exclusive)
     * @return the number of samples < min
     */
    public int getBelowCount(double min) {
	if (sorted) {
	    int start = binarySearch(samples,
				     min,
				     0,
				     count - 1);
	    if (start < 0) 
		start = -start - 1;
	    else {
		// samples[start] == min
		while (start > 0 && samples[start-1] >= min)
		    start--;
		// start == 0 || samples[start-1] < min
	    }
	    return start;
	}
	else {
	    int cnt = 0;
	    for (int i = 0; i < count; i++) {
		if (samples[i] < min)
		    cnt++;
	    }
	    return cnt;
	}
    }

    /**
     * Count the number of samples below or equal to given value.
     *
     * @param min minimum value (inclusive)
     * @return the number of samples <= min
     */
    public int getBelowEqualCount(double min) {
	if (sorted) {
	    int start = binarySearch(samples,
				     min,
				     0,
				     count - 1);
	    if (start < 0) 
		start = -start - 1;
	    else {
		// samples[start] == min
		do {
		    start++;
		}
		while (start < count && samples[start] <= min);
		// samples[start] == count || samples[start] > min
	    }
	    return start;
	}
	else {
	    int cnt = 0;
	    for (int i = 0; i < count; i++) {
		if (samples[i] < min)
		    cnt++;
	    }
	    return cnt;
	}
    }

    /**
     * Count the number of samples above a given value.
     *
     * @param min minimum value (exclusive)
     * @return the number of samples > min
     */
    public int getAboveCount(double min) {
	return count - getBelowEqualCount(min);
    }

    /**
     * Count the number of samples above or equal to a given value.
     *
     * @param min minimum value (inclusive)
     * @return the number of samples >= min
     */
    public int getAboveEqualCount(double min) {
	return count - getBelowCount(min);
    }


    // partition-based order statistic calculation
    private static double select(double[] x, 
				 int off,
				 int len,
				 int index)
    {
	// for small arrays, just use an insertion sort
	if (len < 7) {
	    for (int i=off; i<len+off; i++)
		for (int j=i; j>off && x[j-1]>x[j]; j--)
		    swap(x, j, j-1);
	    return x[index];
	}

	// Choose a partition element, v
	int m = off + len/2;       // Small arrays, middle element
	if (len > 7) {
	    int l = off;
	    int n = off + len - 1;
	    if (len > 40) {        // Big arrays, pseudomedian of 9
		int s = len/8;
		l = med3(x, l,     l+s, l+2*s);
		m = med3(x, m-s,   m,   m+s);
		n = med3(x, n-2*s, n-s, n);
	    }
	    m = med3(x, l, m, n); // Mid-size, med of 3
	}
	double v = x[m];
	
	
	// Establish Invariant: v* (<v)* (>v)* v*
	int a = off, b = a, c = off + len - 1, d = c;
	while(true) {
	    while (b <= c && x[b] <= v) {
		if (x[b] == v)
		    swap(x, a++, b);
		b++;
	    }
	    while (c >= b && x[c] >= v) {
		if (x[c] == v)
		    swap(x, c, d--);
		c--;
	    }
	    if (b > c)
		break;
	    swap(x, b++, c--);
	}

	// Swap partition elements back to middle
	int s, n = off + len;
	s = Math.min(a-off, b-a  );  vecswap(x, off, b-s, s);
	s = Math.min(d-c,   n-d-1);  vecswap(x, b,   n-s, s);

	// recursively select from proper partition
	// first partition
	if (index < off + (s = b - a))
	    return select(x, 
			  off,
			  s,
			  index);
	// last partition
	if (index >= n - (s = d - c))
	    return select(x,
			  n - s,
			  s,
			  index);
	// it must be the middle partition
	return v;
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(double x[], int a, int b) {
	double t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(double x[], int a, int b, int n) {
	for (int i=0; i<n; i++, a++, b++)
	    swap(x, a, b);
    }

    /**
     * Returns the index of the median of the three indexed doubles.
     */
    private static int med3(double x[], int a, int b, int c) {
	return (x[a] < x[b] ?
		(x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
		(x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    // utility, binary search
    private static int binarySearch(double[] a, double key, int low,int high) {
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    double midVal = a[mid];

            int cmp;
            if (midVal < key) {
                cmp = -1;   // Neither val is NaN, thisVal is smaller
            } else if (midVal > key) {
                cmp = 1;    // Neither val is NaN, thisVal is larger
            } else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                cmp = (midBits == keyBits ?  0 : // Values are equal
                       (midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
                        1));                     // (0.0, -0.0) or (NaN, !NaN)
            }

	    if (cmp < 0)
		low = mid + 1;
	    else if (cmp > 0)
		high = mid - 1;
	    else
		return mid; // key found
	}
	return -(low + 1);  // key not found.
    }


}	
