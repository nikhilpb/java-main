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
 * It uses the Hungarian algorithm of Kuhn (1955), as improved and
 * presented by Lawler (1976).  The running time is
 * O(<i>n</i><sup>3</sup>).  The weights can be any finite real
 * numbers; Lawler's algorithm assumes positive weights, so if
 * necessary we add a constant <i>c</i> to all the weights before
 * running the algorithm.  This increases the weight of every perfect
 * matching by <i>nc</i>, which doesn't change which perfect matchings have 
 * maximum weight.  
 *
 * If a weight is set to Double.NEGATIVE_INFINITY, then the algorithm will 
 * behave as if that edge were not in the graph.  If all the edges incident on 
 * a given node have weight Double.NEGATIVE_INFINITY, then the final result 
 * will not be a perfect matching, and an exception will be thrown.  
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2006-10-29 17:39:48 $
 */
public class LawlerBipartiteMatcher implements BipartiteMatcher {
    private int n;
    private double[][] weights;
    private double weightOffset;
    private double weightSign;
    // If (i, j) is in the mapping, then sMatches[i] = j and tMatches[j] = i.  
    // If i is unmatched, then sMatches[i] = -1 (and likewise for tMatches). 
    private int[] sMatches;
    private int[] tMatches;

    static final int NO_LABEL = -1;
    static final int EMPTY_LABEL = -2;

    private int[] sLabels;
    private int[] tLabels;

    private double[] u;
    private double[] v;
	
    private double[] pi;


    private int[] eligibleS;
    private int eligibleSCnt;
    private int[] eligibleT;
    private int eligibleTCnt;

    /**
     * Creates a BipartiteMatcher and prepares it to run on an n x n graph.  
     * All the weights are initially set to 1.  
     */
    public LawlerBipartiteMatcher(int n) {
        this.n = n;
        weights = new double [n][n];
	sMatches = new int [n];
	tMatches = new int [n];
	sLabels = new int [n];
	tLabels = new int [n];
	u = new double [n];
	v = new double [n];
	pi = new double [n];    
        eligibleS = new int [n];
        eligibleT = new int [n];
    }

    /**
     * Sets the weight matrix to the given value w, and computes the
     * maximum weight matching.
     *
     * @param w the weight matrix
     */
    public void computeMax(double[][] w) {
        if (weights.length != n)
            throw new IllegalArgumentException("badly sized weight array");

        if (n == 0)
            return;

        double minWeight = Double.POSITIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if ((w[i][j] > Double.NEGATIVE_INFINITY) 
                    && (w[i][j] < minWeight)) 
                    minWeight = w[i][j];
            }
	}

        if (minWeight >= Double.POSITIVE_INFINITY)
            throw new IllegalArgumentException("no minimum weight");

        // copy the weights and adjust them to ensure positivity
        weightOffset = minWeight - 1.0;
        weightSign = 1.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) { 
                if (w[i][j] > Double.NEGATIVE_INFINITY) 
                    weights[i][j] = w[i][j] - weightOffset;
                else
                    weights[i][j] = Double.NEGATIVE_INFINITY;
            }
        }

        computeMatching();
    }

    /**
     * Sets the weight matrix to the given value w, and computes the
     * minimum weight matching.
     *
     * @param w the weight matrix
     */
    public void computeMin(double[][] w) {
        if (weights.length != n)
            throw new IllegalArgumentException("badly sized weight array");

        if (n == 0)
            return;

        double minWeight = Double.POSITIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double v = -w[i][j];
                if ((v > Double.NEGATIVE_INFINITY) 
                    && (v < minWeight)) 
                    minWeight = v;
            }
	}

        if (minWeight >= Double.POSITIVE_INFINITY)
            throw new IllegalArgumentException("no minimum weight");

        // copy the weights and adjust them to ensure positivity
        weightOffset = minWeight - 1.0;
        weightSign = -1.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) { 
                double v = -w[i][j];
                if (v > Double.NEGATIVE_INFINITY) 
                    weights[i][j] = v - weightOffset;
                else
                    weights[i][j] = Double.NEGATIVE_INFINITY;
            }
        }

        computeMatching();
    }
     
    // do the matching
    private void computeMatching() {
	// Step 0: Initialization
        eligibleSCnt = 0;
        eligibleTCnt = 0;
	for (int i = 0; i < n; i++) {
	    sMatches[i] = -1;
	    tMatches[i] = -1;

	    // u[i] = max_j weights[i][j]
	    u[i] = 0;
	    for (int j = 0; j < n; j++) {
		if (weights[i][j] > u[i]) {
		    u[i] = weights[i][j];
		}
	    }

	    v[i] = 0.0;
	    pi[i] = Double.POSITIVE_INFINITY;

	    // this is really first run of Step 1.0
	    sLabels[i] = EMPTY_LABEL;
            eligibleS[eligibleSCnt++] = i;

	    tLabels[i] = NO_LABEL;
	}

        outer:
	while (true) {
	    // Augment the matching until we can't augment any more given the 
	    // current settings of the dual variables.  
	    while (true) {
		// Steps 1.1-1.4: Find an augmenting path
		int lastNode = findAugmentingPath();
		if (lastNode == -1) {
		    break; // no augmenting path
		}
				
		// Step 2: Augmentation
		flipPath(lastNode);
		for (int i = 0; i < n; i++) {
		    pi[i] = Double.POSITIVE_INFINITY;
		    sLabels[i] = NO_LABEL;
		    tLabels[i] = NO_LABEL;
		}

		// This is Step 1.0
		eligibleSCnt = 0;
		for (int i = 0; i < n; i++) {
		    if (sMatches[i] == -1) {
			sLabels[i] = EMPTY_LABEL;
                        eligibleS[eligibleSCnt++] = i;
		    }
		}

                if (eligibleSCnt == 0)
                    break outer; // no more unassigned nodes
		eligibleTCnt = 0;
	    }

	    // Step 3: Change the dual variables

	    // delta1 = min_i u[i]
	    double delta1 = Double.POSITIVE_INFINITY;
	    for (int i = 0; i < n; i++) {
		if (sLabels[i] != NO_LABEL && u[i] < delta1) {
		    delta1 = u[i];
		}
	    }

	    // delta2 = min_{j : pi[j] > 0} pi[j]
	    double delta2 = Double.POSITIVE_INFINITY;
	    for (int j = 0; j < n; j++) {
		if (pi[j] > 0.0 && pi[j] < delta2) {
		    delta2 = pi[j];
		}
	    }

	    if (delta1 < delta2) {
		// In order to make another pi[j] equal 0, we'd need to 
		// make some u[i] negative.  
		break; // we have a maximum-weight matching
	    }
			
	    changeDualVars(delta2);
	}

        // test optimality
        if (false) {
            double dualVal = 0.0;
            for (int i = 0; i < n; i++) {
                dualVal += u[i] + v[i];
                if (u[i] < 0.0)
                    throw new IllegalStateException("dual not feasible");
                if (v[i] < 0.0)
                    throw new IllegalStateException("dual not feasible");
                for (int j = 0; j < n; j++) {
                    if (u[i] + v[j] < weights[i][j])
                        throw new IllegalStateException("dual not feasible");
                }
            }
            
            double primalVal = 0.0;
            for (int i = 0; i < n; i++)
                primalVal += weights[i][sMatches[i]];
            
            if (Math.abs(primalVal - dualVal) > 1e-10)
                throw new IllegalStateException("primal dual mismatch");
        }
    }

    /**
     * Return the computed maximum-weight perfect matching relative to
     * the weights specified with setWeight.  The matching is
     * represented as an array arr of length n, where arr[i] = j if
     * (i,j) is in the matching.
     *
     * @returns the computed matching, returns the same array at every
     * invocation
     */
    public int[] getMatchingSource() {
	if (n == 0) {
	    return new int [0];
	}
        return sMatches;
    }

    public int[] getMatchingDest() {
	if (n == 0) {
	    return new int [0];
	}
        return tMatches;
    }

    /**
     * Return the weight of the maximum weight matching.
     *
     * @return the weight
     */
    public double getMatchingWeight() {
        double sum = 0.0;
        for (int i = 0; i < n; i++)
            sum += weightSign * (weights[i][sMatches[i]] + weightOffset);
        return sum;
    }

    /**
     * Tries to find an augmenting path containing only edges (i,j) for which 
     * u[i] + v[j] = weights[i][j].  If it succeeds, returns the index of the 
     * last node in the path.  Otherwise, returns -1.  In any case, updates 
     * the labels and pi values.
     */
    private int findAugmentingPath() {
	while ((eligibleSCnt > 0) || (eligibleTCnt > 0)) {
	    if (eligibleSCnt > 0) {
                int i = eligibleS[--eligibleSCnt];
		for (int j = 0; j < n; j++) {
		    if (sMatches[i] == j) continue;

                    double x = u[i] + v[j] - weights[i][j];
		    // We need the pi[j] > 0 check here to avoid problems 
		    // when u[i] + v[j] - weights[i][j] is really 0, but is 
		    // computed to be a bit less than 0 due to limited 
		    // precision. 
                    // Really need this!!!
		    if (pi[j] > 0.0 &&
                        x < pi[j]) {
			tLabels[j] = i;
			pi[j] = x;
			if (pi[j] <= 0.0) 
			    eligibleT[eligibleTCnt++] = j;
		    }
		}
	    } else {
                int j = eligibleT[--eligibleTCnt];
		if (tMatches[j] == -1) {
		    return j; // we've found an augmenting path
		} 
		int i = tMatches[j];
		if (sLabels[i] == NO_LABEL) {
		    sLabels[i] = j;
                    eligibleS[eligibleSCnt++] = i;
		}
	    }
	}

	return -1;
    }

    /**
     * Given an augmenting path ending at lastNode, "flips" the path.  This 
     * means that an edge on the path is in the matching after the flip if 
     * and only if it was not in the matching before the flip.  An augmenting 
     * path connects two unmatched nodes, so the result is still a matching. 
     */ 
    private void flipPath(int lastNode) {
	while (lastNode != EMPTY_LABEL) {
	    int parent = tLabels[lastNode];

	    // Add (parent, lastNode) to matching.  We don't need to 
	    // remove any edges from the matching because: 
	    //  * We know at this point that there is no i such that 
	    //    sMatches[i] = lastNode.  
	    //  * Although there might be some j such that tMatches[j] =
	    //    parent, that j must be sLabels[parent], and will change 
	    //    tMatches[j] in the next time through this loop.  
	    sMatches[parent] = lastNode;
	    tMatches[lastNode] = parent;
					
	    lastNode = sLabels[parent];
	}		
    }

    private void changeDualVars(double delta) {
	for (int i = 0; i < n; i++) {
	    if (sLabels[i] != NO_LABEL) 
		u[i] -= delta;
	}
		
	for (int j = 0; j < n; j++) {
	    if (pi[j] <= 0.0) 
		v[j] += delta;
	    else if (tLabels[j] != NO_LABEL) {
		pi[j] -= delta;
		if (pi[j] <= 0.0) 
                    eligibleT[eligibleTCnt++] = j;
	    }
	}
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		sb.append(weights[i][j] + " ");
	    }
	    sb.append("; ");
	}
        return sb.toString();
    }

    public void dumpWeights(PrintStream out) {
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		out.print(weights[i][j] + " ");
	    }
	    out.println("");
	}
    }

    // test stub
    public static void main(String[] argv) throws Exception {
        String fileName = argv[0];

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
        int n = Integer.parseInt(tokenizer.nextToken());
        double[][] w = new double [n][n];

        for (int i = 0; i < n; i++) {
            String weightStr = reader.readLine();
            tokenizer = new StringTokenizer(weightStr);
            for (int j = 0; j < n; j++) 
                w[i][j] = Double.parseDouble(tokenizer.nextToken());
        }
        
        LawlerBipartiteMatcher bm = new LawlerBipartiteMatcher(n);
        bm.computeMax(w);
        bm.dumpWeights(System.out);
        int[] match = bm.getMatchingSource();
        for (int i = 0; i < n; i++)
            System.out.println((i+1) + " " + (match[i]+1));
    }
}
