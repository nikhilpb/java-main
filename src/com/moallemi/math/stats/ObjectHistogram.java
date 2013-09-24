package com.moallemi.math.stats;

import java.util.*;

import com.moallemi.util.data.MutableInt;

/**
 * A histogram for counting objects.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.4 $, $Date: 2006-06-16 19:59:07 $
 */
public class ObjectHistogram<T> {
    // set of object counts
    private Map<T,MutableInt> countMap;
    // total number of counts
    private int totalCount = 0;

    /**
     * Constructor.
     */
    public ObjectHistogram() {
        countMap = new HashMap<T,MutableInt> ();
    }

    /**
     * Constructor.
     *
     * @param size size of initial map to build
     */
    public ObjectHistogram(int size) {
        countMap = new HashMap<T,MutableInt> (size);
    }

    /**
     * Create an (empty) bin for an object.
     *
     * @param object object to create the bin for
     * @return <code>true</code> if a bin did not already exist,
     * <code>false</code> otherwise (bin is zeroed if it existed).
     */
    public boolean createBin(T object) {
	MutableInt count = 
	    countMap.put(object, new MutableInt(0));
	if (count != null) {
	    totalCount -= count.value;
	    return false;
	}
	return true;
    }


    /**
     * Add an object. Increments the counter of the object by one,
     * creating a bin if necessary.
     *
     * @param object the object to add
     */
    public void add(T object) { add(object, 1); }

    /**
     * Add an object. Increments the counter of the object by
     * <code>increment</code>, creating a bin if necessary.
     *
     * @param object the object to add
     * @param increment the increment
     */
    public void add(T object, int increment) {
	MutableInt count = countMap.get(object);
	if (count == null) 
	    countMap.put(object, new MutableInt(increment));
	else
	    count.value += increment;
	totalCount += increment;
    }

    /**
     * Set the count of an object, creating a bin if necessary.
     *
     * @param object the object to set
     * @param value the count
     */
    public void setBinCount(T object, int value) {
	MutableInt count = countMap.get(object);
	if (count == null) 
	    countMap.put(object, new MutableInt(value));
	else {
	    totalCount -= count.value;
	    count.value = value;
	}
	totalCount += value;
    }


    /**
     * Get the number of bins.
     *
     * @return the number of bins
     */
    public int getNumBins() { return countMap.size(); }

    /**
     * Get the total count.
     *
     * @return the total count
     */
    public int getTotalCount() { return totalCount; }

    /**
     * Get the count of a particular object.
     *
     * @param object the object
     * @return the count of the object
     * @throws IllegalArgumentException if there is no bin for that object
     */
    public int getBinCount(T object) 
	throws IllegalArgumentException
    {
	MutableInt count = countMap.get(object);
	if (count == null)
	    throw new IllegalArgumentException("no bin");
	return count.value;
    }

    /**
     * Get the frequency of a particular object.
     *
     * @param object the object
     * @return the frequency of the object
     * @throws IllegalArgumentException if there is no bin for that object
     */
    public double getBinFrequency(T object) 
	throws IllegalArgumentException
    {
	MutableInt count = countMap.get(object);
	if (count == null)
	    throw new IllegalArgumentException("no bin");
	return ((double) count.value) / ((double) totalCount);
    }


    /**
     * Does a bin exist for a particular object?
     *
     * @param object the object
     * @return <code>true</code> if a bin exists, <code>false</code>
     * otherwise
     */
    public boolean containsBin(T object) {
	return countMap.containsKey(object);
    }

    /**
     * Returns a bin iterator sorted
     * by a comparator.
     *
     * @param c The comparator to sort 
     * the bins with
     * @return an sorted Iterator of bin keys 
     */
    public Iterator<T> binIterator(Comparator<T> c) {
	TreeSet<T> s =new TreeSet<T> (c);
	s.addAll(Collections.unmodifiableSet(countMap.keySet()));
	return s.iterator();
    }


    /**
     * Get an iterator over all bins.
     *
     * @return an iterator over all bins.
     */
    public Iterator<T> binIterator() {
	return Collections.unmodifiableSet(countMap.keySet()).iterator();
    }
	
    /**
     * Get the mean number of counts.
     *
     * @return the mean
     * @throws ArithmeticException if there are no bins
     */
    public double getMean() throws ArithmeticException {
	int size = countMap.size();
	if (size == 0)
	    throw new ArithmeticException("no bins");
	return ((double) totalCount) / ((double) size);
    }

    /**
     * Generate sample statistics for the counts in this histogram.
     *
     * @return sample statistics
     */
    public SampleStatistics getSampleStatistics() {
	SampleStatistics statistics = new SampleStatistics(countMap.size());
	for (Iterator<MutableInt> i = countMap.values().iterator(); 
             i.hasNext(); ) {
	    MutableInt count = i.next();
	    statistics.addSample(count.value);
	}
	return statistics;
    }

    /**
     * Clear the histogram (but retain the bins).
     */
    public void clear() {
	for (Iterator<MutableInt> i = countMap.values().iterator(); 
             i.hasNext(); ) {
	    MutableInt count = i.next();
	    count.value = 0;
	}
	totalCount = 0;
    }

    /**
     * Clear the histogram and remove all bins.
     */
    public void clearAll() {
	countMap.clear();
	totalCount = 0;
    }

    /**
     * Add another histogram to this one, adding bins as necessary.
     *
     * @param other histogram to add
     */
    public void addHistogram(ObjectHistogram<T> other) {
	for (Iterator<Map.Entry<T,MutableInt>> i 
                 = other.countMap.entrySet().iterator(); 
	     i.hasNext(); ) {
	    Map.Entry<T,MutableInt> e = i.next();
	    add(e.getKey(), e.getValue().value);
	}
    }
}
