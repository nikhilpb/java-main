package com.moallemi.qpminsum;

import com.moallemi.minsum.PairwiseFactorGraph;

public class QPMinSumSolver implements QPIterativeSolver {
    protected QuadraticPairwiseProblem problem;
    protected double damp;

    // indexed by [node][neighborIndex]
    private double[][] incomingMsgK;
    private double[][] incomingMsgH;
    private double[][] nextIncomingMsgK;
    private double[][] nextIncomingMsgH;
    private double[] solution;
    private double bellmanErrorK;
    private double bellmanErrorH;

    public QPMinSumSolver(double damp) {
        this.damp = damp;
    }

    public double getBellmanErrorK() { return bellmanErrorK; }
    public double getBellmanErrorH() { return bellmanErrorH; }

    public void setProblem(QuadraticPairwiseProblem problem) {
        this.problem = problem;
        int nodeCount = problem.getNodeCount();

        incomingMsgK = new double [nodeCount][];
        incomingMsgH = new double [nodeCount][];
        nextIncomingMsgK = new double [nodeCount][];
        nextIncomingMsgH = new double [nodeCount][];
        for (int i = 0; i < nodeCount; i++) {
            int degree = problem.getNodeDegree(i);
            incomingMsgK[i] = new double [degree];
            incomingMsgH[i] = new double [degree];
            nextIncomingMsgK[i] = new double [degree];
            nextIncomingMsgH[i] = new double [degree];
        }
        solution = new double [nodeCount];
        getSolution();
    }

    public double[] getSolution() {
        int nodeCount = problem.getNodeCount();
        for (int i = 0; i < nodeCount; i++) {
            double sumK = problem.getSingleK(i);
            double sumH = problem.getSingleH(i);
            int degree = problem.getNodeDegree(i);
            for (int idx = 0; idx < degree; idx++) {
                sumK += incomingMsgK[i][idx];
                sumH += incomingMsgH[i][idx];
            }
            if (sumK <= 0.0)
                throw new IllegalStateException("ill-posed minimization");
            solution[i] = sumH / sumK;
        }
        return solution;
    }

    public void iterate() {
        bellmanErrorK = 0.0;
        bellmanErrorH = 0.0;
        int nodeCount = problem.getNodeCount();

        // compute new outgoing messages
        for (int i = 0; i < nodeCount; i++) {
            int degree = problem.getNodeDegree(i);
            double sumIncomingMsgK = problem.getSingleK(i);
            double sumIncomingMsgH = problem.getSingleH(i);
            for (int idx = 0; idx < degree; idx++) {
                sumIncomingMsgK += incomingMsgK[i][idx];
                sumIncomingMsgH += incomingMsgH[i][idx];
            }
            for (int idx = 0; idx < degree; idx++) {
                int j = problem.getNodeNeighbor(i, idx);
                double sumK = sumIncomingMsgK - incomingMsgK[i][idx]
                    + problem.getPairwiseK(i, idx);
                if (sumK <= 0.0)
                    throw new IllegalStateException("ill-posed minimization");

                double Gamma_ij = problem.getPairwiseGamma(i, idx);
                double sumH = sumIncomingMsgH - incomingMsgH[i][idx]
                    + problem.getPairwiseH(i, idx);
                int offset = problem.getNodeNeighborOffset(i, idx);
                double newK = problem.getPairwiseK(j, offset)
                    - Gamma_ij * Gamma_ij / sumK;

                nextIncomingMsgK[j][offset] = 
                    damp * newK + (1.0 - damp) * incomingMsgK[j][offset];
                double diffK = newK - incomingMsgK[j][offset];
                bellmanErrorK += diffK * diffK;

                double newH = problem.getPairwiseH(j, offset)
                    - sumH * Gamma_ij / sumK;
                nextIncomingMsgH[j][offset] = damp * newH
                    + (1.0 - damp) * incomingMsgH[j][offset];
                double diffH = newH - incomingMsgH[j][offset];
                bellmanErrorH += diffH * diffH;
            }
        }

        // swap in new messages
        double[][] tmp;
        tmp = incomingMsgH;
        incomingMsgH = nextIncomingMsgH;
        nextIncomingMsgH = tmp;
        tmp = incomingMsgK;
        incomingMsgK = nextIncomingMsgK;
        nextIncomingMsgK = tmp;
    }

}
