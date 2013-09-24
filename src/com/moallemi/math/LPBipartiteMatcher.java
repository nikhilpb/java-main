package com.moallemi.math;

import java.util.*;
import java.io.*;

import ilog.concert.*;
import ilog.cplex.*;

/**
 * Computes bipartite matching by solving an LP.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.10 $, $Date: 2006-10-31 21:16:50 $
 */
public class LPBipartiteMatcher implements BipartiteMatcher {
    private int n;
    private double[][] w;
    private double weightSign = 1.0;
    private IloCplex cplex;
    private IloNumVar[][] xVar;
    private IloNumVar[] xVarRow;
    private double[] weights;
    private IloObjective obj;
    private Map<IloNumVar,Index> varMap;
    private static class Index {
        public int i, j;
        public Index(int i, int j) { this.i = i; this.j = j; }
    }
    private int[] sMatches;
    private int[] tMatches;
    private double value;

    public LPBipartiteMatcher(int n) {
        this.n = n;
        sMatches = new int [n];
        tMatches = new int [n];
        try {
            // build LP
            cplex = new IloCplex();
            cplex.setParam(IloCplex.IntParam.RootAlg,
                           IloCplex.Algorithm.Network);
            cplex.setOut(null);

            // create variables
            int n2 = n*n;
            weights = new double [n2];
            double[] zeros = new double [n2];
            Arrays.fill(zeros, 0.0);
            double[] ones = new double [n2];
            Arrays.fill(ones, 1.0);        
            xVarRow = cplex.numVarArray(n2, zeros, ones);
            xVar = new IloNumVar [n][n];
            varMap = new HashMap<IloNumVar,Index> (2*n2 + 1);
            int idx = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    xVar[i][j] = xVarRow[idx++];
                    varMap.put(xVar[i][j], new Index(i,j));
                }
            }
            
            // add objective
            obj = 
		cplex.addMinimize(cplex.scalProd(ones, xVarRow));
            
            // add constraints
            double[] ones2 = new double [n];
            IloNumVar[] row = new IloNumVar [n];
            Arrays.fill(ones2, 1.0);
            for (int i = 0; i < n; i++) {
                cplex.addEq(cplex.scalProd(ones2, xVar[i]), 1.0);
                for (int j = 0; j < n; j++) 
                    row[j] = xVar[j][i];
                cplex.addEq(cplex.scalProd(ones2, row), 1.0);
            }
        }
        catch (IloException e) {
            throw new IllegalStateException("cplex exception", e);
        }

    }

    /**
     * Sets the weight matrix to the given value w, and computes the
     * maximum weight matching.
     *
     * @param w the weight matrix
     */
    public void computeMax(double[][] w) {
        if (w.length != n)
            throw new IllegalArgumentException("badly sized weight array");

        if (n == 0)
            return;

        weightSign = -1.0;
        int idx = 0;
        for (int i = 0; i < n; i++) 
            for (int j = 0; j < n; j++) 
                weights[idx++] = -w[i][j];

        try {
            ((CpxObjective) obj).setLinearCoefs(weights, xVarRow);
            
            computeMatching();
        }
        catch (IloException e) {
            throw new IllegalStateException("cplex exception", e);
        }
    }

    /**
     * Sets the weight matrix to the given value w, and computes the
     * minimum weight matching.
     *
     * @param w the weight matrix
     */
    public void computeMin(double[][] w) {
        if (w.length != n)
            throw new IllegalArgumentException("badly sized weight array");
        this.w = w;

        if (n == 0)
            return;

        weightSign = 1.0;
        int idx = 0;
        for (int i = 0; i < n; i++) 
            for (int j = 0; j < n; j++) 
                weights[idx++] = w[i][j];

        try {
            ((CpxObjective) obj).setLinearCoefs(weights, xVarRow);

            computeMatching();
        }
        catch (IloException e) {
            throw new IllegalStateException("cplex exception", e);
        }
    }
     
    // do the matching
    private void computeMatching() throws IloException {
        if (!cplex.solve()) 
            throw new IllegalStateException("cplex failed");

         Arrays.fill(tMatches, -1);
        outer:
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n; j++) {
                 if (cplex.getValue(xVar[i][j]) > 0.5) {
                     sMatches[i] = j;
                     if (tMatches[j] >= 0)
                         throw new IllegalStateException("could not "
                                                         + "construct matching");
                     tMatches[j] = i;
                     continue outer;
                 }
                 sMatches[i] = -1;
             }
         }

         value = weightSign * cplex.getObjValue();
	 //cplex.exportModel("debug.lp");
    }

    public int[] getMatchingSource() {
	return n == 0 
            ? new int [0]
            : sMatches;
    }

    public int[] getMatchingDest() {
	return n == 0 
            ? new int [0]
            : tMatches;
    }

    public double getMatchingWeight() { return value; }

}
