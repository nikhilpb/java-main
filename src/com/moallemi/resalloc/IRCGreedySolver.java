package com.moallemi.resalloc;

import java.util.Random;

import com.moallemi.math.Shuffle;

public class IRCGreedySolver implements InelasticRateControlSolver {
    private InelasticRateControlProblem problem;
    private InelasticRateControlSolution solution;
    private Random r;

    public IRCGreedySolver(InelasticRateControlProblem problem, Random r)
    {
        this.problem = problem;
        this.r = r;
        solution = new InelasticRateControlSolution(problem);
    }

    public boolean solve() {
        int userCount = problem.getVariableCount();
        int linkCount = problem.getFactorCount();


        double[] bias = new double [userCount];
        for (int u = 0; u < userCount; u++) {
            double p = 0.0;
            int degree = problem.getVariableDegree(u);
            for (int lIndex = 0; lIndex < degree; lIndex++) {
                int l = problem.getVariableNeighbor(u, lIndex);
                p += 1.0 / problem.getLinkCapacity(l);
            }
            p *= problem.getUserMinBandwidth(u);
            bias[u] = p <= 0.0 
                ? Double.POSITIVE_INFINITY
                : problem.getUserUtility(u) / p;
        }
        solution.setGreedyShuffle(bias, r);

        return true;
    }

    public InelasticRateControlSolution getSolution() { return solution; }

    public boolean isOptimal() { return false; }
}

        
        
    