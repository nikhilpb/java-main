package com.moallemi.math;

import java.util.*;
import java.io.*;

/**
 * An engine for finding the maximum-weight matching in a complete
 * bipartite graph.  Suppose we have two sets <i>S</i> and <i>T</i>,
 * both of size <i>n</i>.  For each <i>i</i> in <i>S</i> and <i>j</i>
 * in <i>T</i>, we have a weight <i>w<sub>ij</sub></i>.  A perfect
 * matching <i>X</i> is a subset of <i>S</i> x <i>T</i> such that each
 * <i>i</i> in <i>S</i> occurs in exactly one element of <i>X</i>, and
 * each <i>j</i> in <i>T</i> occurs in exactly one element of
 * <i>X</i>.  Thus, <i>X<i/> can be thought of as a one-to-one
 * function from <i>S</i> to <i>T</i>.  The weight of <i>X</i> is the
 * sum, over (<i>i</i>, <i>j<i>) in <i>X</i>, of
 * <i>w<sub>ij</sub></i>.  A BipartiteMatcher takes the number
 * <i>n</i> and the weights <i>w<sub>ij</sub></i>, and finds a perfect
 * matching of maximum weight.
 *
 * If a weight is set to Double.NEGATIVE_INFINITY, then the algorithm will 
 * behave as if that edge were not in the graph.  If all the edges incident on 
 * a given node have weight Double.NEGATIVE_INFINITY, then the final result 
 * will not be a perfect matching, and an exception will be thrown.  
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.15 $, $Date: 2006-10-29 17:39:48 $
 */
public interface BipartiteMatcher {
    /**
     * Sets the weight matrix to the given value w, and computes the
     * maximum weight matching.
     *
     * @param w the weight matrix
     */
    public void computeMax(double[][] w);


    /**
     * Sets the weight matrix to the given value w, and computes the
     * minimum weight matching.
     *
     * @param w the weight matrix
     */
    public void computeMin(double[][] w);

    /**
     * Return the computed maximum-weight perfect matching relative to
     * the weights specified with setWeight.  The matching is
     * represented as an array arr of length n, where arr[i] = j if
     * (i,j) is in the matching.
     *
     * @returns the computed matching, returns the same array at every
     * invocation
     */
    public int[] getMatchingSource();

    /**
     * Return the computed maximum-weight perfect matching relative to
     * the weights specified with setWeight.  The matching is
     * represented as an array arr of length n, where arr[j] = i if
     * (i,j) is in the matching.
     *
     * @returns the computed matching, returns the same array at every
     * invocation
     */
    public int[] getMatchingDest();

    /**
     * Return the weight of the optimal matching.
     *
     * @return the weight
     */
    public double getMatchingWeight();
}