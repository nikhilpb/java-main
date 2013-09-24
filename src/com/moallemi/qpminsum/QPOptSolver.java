package com.moallemi.qpminsum;

import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.*;

import com.moallemi.minsum.PairwiseFactorGraph;

public class QPOptSolver {
    private QuadraticPairwiseProblem problem;
    private double[] solution;

    public void setProblem(QuadraticPairwiseProblem problem) {
        this.problem = problem;
        solution = null;
    }
    
    public boolean solve() throws IterativeSolverNotConvergedException {
        int nodeCount = problem.getNodeCount();

        // construct the matrix A and vector b so that
        // f(x) = (1/2) x^T A x - b^T x
        FlexCompRowMatrix A = new FlexCompRowMatrix(nodeCount, nodeCount);
        DenseVector b = new DenseVector(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            double diag = problem.getSingleK(i);
            double v = problem.getSingleH(i);
            int degree = problem.getNodeDegree(i);
            for (int idx = 0; idx < degree; idx++) {
                diag += problem.getPairwiseK(i, idx);
                v += problem.getPairwiseH(i, idx);
                int j = problem.getNodeNeighbor(i, idx);
                A.set(i, j, problem.getPairwiseGamma(i, idx));
            }
            A.set(i, i, diag);
            b.set(i, v);
        }
        A.compact();

        // solve A x = b
        DenseVector x = new DenseVector(nodeCount);
        IterativeSolver solver = new CG(x);
        Preconditioner M = new ICC(new CompRowMatrix(A));
        M.setMatrix(A);
        solver.setPreconditioner(M);
        solver.solve(A, b, x);
        solution = x.getData();

        return true;
    }

    public double[] getSolution() {
        return solution;
    }
}
    

                                        
