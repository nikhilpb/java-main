package com.moallemi.qpminsum;

import com.moallemi.math.graph.Graph;
import com.moallemi.minsum.PairwiseFactorGraph;


public class  QuadraticPairwiseProblem extends PairwiseFactorGraph {
    // single-node potentials
    // f_i(x_i) = 1/2 K[i] x_i^2 - H[i] x_i
    protected double[] singleK;
    protected double[] singleH;

    // pairwise-potentials [node][neighborIndex]
    // f_{ij}(x_i,x_j) = 1/2 ( K[i][j_idx] x_i^2
    //                         + 2 Gamma[i][j_idx] x_i x_j
    //                         + K[j][i_idx] x_j^2)
    //                    - H[i][j_idx] x_i - H[j][i_idx] x_j
    // Also,  Gamma[i][j_idx] == Gamma[j][i_idx]
    double[][] pairwiseK;
    double[][] pairwiseGamma;
    double[][] pairwiseH;


    public QuadraticPairwiseProblem(Graph graph) {
        super(graph);
        singleK = new double [nodeCount];
        singleH = new double [nodeCount];
        pairwiseK = new double [nodeCount][];
        pairwiseGamma = new double [nodeCount][];
        pairwiseH = new double [nodeCount][];
        for (int i = 0; i < nodeCount; i++) {
            int degree = getNodeDegree(i);
            pairwiseK[i] = new double [degree];
            pairwiseGamma[i] = new double [degree];
            pairwiseH[i] = new double [degree];
        }
    }

    public double getSingleK(int i) { return singleK[i]; }
    public double getSingleH(int i) { return singleH[i]; }
    public double getPairwiseK(int i, int idx) { return pairwiseK[i][idx]; }
    public double getPairwiseGamma(int i, int idx) {
        return pairwiseGamma[i][idx];
    }
    public double getPairwiseH(int i, int idx) { return pairwiseH[i][idx]; }

    public void setSingleK(int i, double v) { singleK[i] = v; }
    public void setSingleH(int i, double v) { singleH[i] = v; }
    public void setPairwiseK(int i, int idx, double v) { 
        pairwiseK[i][idx] = v;
    }
    public void setPairwiseGamma(int i, int idx, double v) {
        pairwiseGamma[i][idx] = v;
        int j = nodeNeighborMap[i][idx];
        int offset = nodeNeighborOffsetMap[i][idx];
        pairwiseGamma[j][offset] = v;
    }
    public void setPairwiseH(int i, int idx, double v) { 
        pairwiseH[i][idx] = v;
    }

    public double evaluate(double[] x) {
        double v = 0.0;
        for (int i = 0; i < nodeCount; i++) {
            double xi = x[i];
            v += (0.5 * singleK[i] * xi - singleH[i]) * xi;
            int degree = nodeDegree[i];
            for (int idx = 0; idx < degree; idx++) {
                v += (0.5 * pairwiseK[i][idx] * xi - pairwiseH[i][idx]) * xi;
                int j = nodeNeighborMap[i][idx];
                if (i < j)
                    v += pairwiseGamma[i][idx] * xi * x[j];
            }
        }
        return v;
    }

    public double evaluateGradientNorm(double[] x) {
        double g = 0.0;
        for (int i = 0; i < nodeCount; i++) {
            double xi = x[i];
            double dg = singleK[i] * xi - singleH[i];
            int degree = nodeDegree[i];
            for (int idx = 0; idx < degree; idx++) {
                int j = nodeNeighborMap[i][idx];
                dg += pairwiseK[i][idx] * xi 
                    + pairwiseGamma[i][idx] * x[j]
                    - pairwiseH[i][idx];
            }
            g += dg * dg;
        }
        return Math.sqrt(g);
    }
            

}
        