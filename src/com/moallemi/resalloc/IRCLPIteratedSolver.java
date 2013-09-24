package com.moallemi.resalloc;

import java.util.Arrays;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.CplexFactory;

public class IRCLPIteratedSolver implements InelasticRateControlSolver {
    private InelasticRateControlProblem problem;
    private InelasticRateControlSolution solution;
    private CplexFactory factory;
    private boolean isOptimal = false;
    private int totalIterations;
    private double epsilon;

    public IRCLPIteratedSolver(InelasticRateControlProblem problem,
                               int totalIterations,
                               double epsilon,
                               CplexFactory factory) throws IloException
    {
        this.problem = problem;
        this.totalIterations = totalIterations;
        this.epsilon = epsilon;
        this.factory = factory;
        solution = new InelasticRateControlSolution(problem);
    }

    public boolean solve() throws IloException {
        int userCount = problem.getVariableCount();
        int linkCount = problem.getFactorCount();

        double[] objWeight = new double [userCount];
        for (int u = 0; u < userCount; u++)
            objWeight[u] = problem.getUserUtility(u);

        for (int iter = 0; iter < totalIterations; iter++) {
            IloCplex cplex = factory.getCplex();

            IloNumVar[] xVar = cplex.numVarArray(userCount, 0.0, 1.0);
            
            // construct objective
            double[] v = new double [userCount];
            for (int u = 0; u < userCount; u++)
                v[u] = objWeight[u];
            cplex.addMaximize(cplex.scalProd(v, xVar));
        
            // add constraints
            IloRange[] lCons = new IloRange [linkCount];
            for (int l = 0; l < linkCount; l++) {
                int degree = problem.getFactorDegree(l);
                double[] b = new double [degree];
                IloNumVar[] xLVar = new IloNumVar [degree];
                for (int uIndex = 0; uIndex < degree; uIndex++) {
                    int u = problem.getFactorNeighbor(l, uIndex);
                    b[uIndex] = problem.getUserMinBandwidth(u);
                    xLVar[uIndex] = xVar[u];
                }
                lCons[l] = 
                    cplex.addLe(cplex.scalProd(b, xLVar), 
                                problem.getLinkCapacity(l));
            }
            
            boolean status = cplex.solve();
            if (!status)
                throw new IllegalStateException("cplex failed");

            double[] linkPrices = new double [linkCount];
            for (int l = 0; l < linkCount; l++)
                linkPrices[l] = cplex.getDual(lCons[l]);
            
            double[] bias = new double [userCount];
            for (int u = 0; u < userCount; u++) {
                double p = 0.0;
                int degree = problem.getVariableDegree(u);
                for (int lIndex = 0; lIndex < degree; lIndex++) {
                    int l = problem.getVariableNeighbor(u, lIndex);
                    p += linkPrices[l];
                }
                p *= problem.getUserMinBandwidth(u);
                bias[u] = p <= 0.0 
                    ? Double.POSITIVE_INFINITY
                    : objWeight[u] / p;
            }
            solution.setGreedy(bias);

            System.out.println("iter: " + (iter+1)
                               + " obj: " +
                               solution.getObjectiveValue());

            // compute weights for the next round
            if (iter + 1 < totalIterations) {
                for (int u = 0; u < userCount; u++) 
                    objWeight[u] = 
                        problem.getUserUtility(u) 
                        / (epsilon + 
                           problem.getUserMinBandwidth(u) 
                           * (1.0 - cplex.getValue(xVar[u])));
            }
        }

        return true;
    }

    public InelasticRateControlSolution getSolution() { return solution; }

    public boolean isOptimal() { return false; }
}

        
        
    