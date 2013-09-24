package com.moallemi.math.stats;

/**
 * Class for maintaining statistics on a set of samples, but just mean
 * and variance. Uses the Knuth/Welford algorithm, see: 
 * Algorithms_for_calculating_variance.
 * 
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2009-02-13 04:53:55 $
 */
public class MVSampleStatistics {
    private double mean, M2;
    private double min, max;
    private int count;
    

    /**
     * Constructor
     */
    public MVSampleStatistics() {
    }

    /**
     * Constructor
     */
    public MVSampleStatistics(MVSampleStatistics other) {
	this.count = other.count;
	this.mean = other.mean;
	this.M2 = other.M2;
	this.min = other.min;
	this.max = other.max;

    }

    /**
     * Clear any samples collected thus far.
     */
    public void clear() {
	count = 0;
        mean = M2 = 0.0;
    }


    /**
     * Add a sample.
     *
     * @param x sample value to add
     */
    public void addSample(double x) {
	if (count == 0)
            min = max = x;
	else if (x < min) 
            min = x;
        else if (x > max) 
            max = x;

        count++;
        double delta = x - mean;
        mean += delta / count;
        M2 += delta*(x - mean); // note: uses new expression for mean!
    }

    /**
     * Fetch the number of samples.
     *
     * @return the number of samples
     */
    public int getCount() { return count; }

    /**
     * Fetch the mean of the samples.
     * 
     * @return the mean
     * @throws ArithmeticException if there are no samples
     */
    public double getMean() throws ArithmeticException {
	if (count <= 0)
	    throw new ArithmeticException("no samples");
	return mean;
    }

    /**
     * Fetch the minimum of the samples.
     * 
     * @return the minimum
     * @throws ArithmeticException if there are no samples
     */
    public double getMinimum() throws ArithmeticException {
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
    public double getMaximum() throws ArithmeticException {
	if (count <= 0)
	    throw new ArithmeticException("no samples");
	return max;
    }

    /**
     * Fetch the sample standard deviation.
     * 
     * @return the standard deviation
     * @throws ArithmeticException if there are not at least 2 samples
     * or in the case of gross rounding error (negative variance)
     */
    public double getStandardDeviation()
	throws ArithmeticException
    {
	if (count < 2)
	    throw new ArithmeticException("need 2 samples for "
					  + "standard deviation");

	double variance = M2 / (count - 1);
	if (variance < 0.0)
	    throw new ArithmeticException("rounding error: negative variance");

	return Math.sqrt(variance);	    
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
	if (count < 2)
	    throw new ArithmeticException("need 2 samples for "
					  + "standard deviation");

	double variance = M2 / count;
	if (variance < 0.0)
	    throw new ArithmeticException("rounding error: negative variance");

	return Math.sqrt(variance);	    
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
}	

