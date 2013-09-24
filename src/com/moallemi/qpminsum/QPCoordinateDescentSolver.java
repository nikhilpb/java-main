package com.moallemi.qpminsum;

import com.moallemi.minsum.PairwiseFactorGraph;

public class QPCoordinateDescentSolver implements QPIterativeSolver {
    protected QuadraticPairwiseProblem problem;
    protected double damp;

    private double[] solution;
    private double[] nextSolution;

    public QPCoordinateDescentSolver(double damp) {
        this.damp = damp;
    }

    public void setProblem(QuadraticPairwiseProblem problem) {
        this.problem = problem;
        int nodeCount = problem.getNodeCount();
        solution = new double [nodeCount];
        nextSolution = new double [nodeCount];
    }

    public double[] getSolution() {
        return solution;
    }

    public void iterate() {
        int nodeCount = problem.getNodeCount();
        for (int i = 0; i < nodeCount; i++) {
            double sumK = problem.getSingleK(i);
            double sumH = problem.getSingleH(i);
            int degree = problem.getNodeDegree(i);
            for (int idx = 0; idx < degree; idx++) {
                int j = problem.getNodeNeighbor(i, idx);
                sumK += problem.getPairwiseK(i, idx);
                sumH += - problem.getPairwiseGamma(i, idx) * solution[j]
                    + problem.getPairwiseH(i, idx);
            }
            if (sumK <= 0.0)
                throw new IllegalStateException("ill-posed minimization");
            double newX = sumH / sumK;
            nextSolution[i] = damp * newX + (1.0 - damp)* solution[i];
        }
        double[] tmp = solution;
        solution = nextSolution;
        nextSolution = tmp;
    }
}
        
