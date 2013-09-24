package com.moallemi.resalloc;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.CplexFactory;

public class IRCLPGreedySolver implements InelasticRateControlSolver {
    private InelasticRateControlProblem problem;
    private InelasticRateControlSolution solution;
    private IloCplex cplex;
    private boolean isOptimal = false;

    public IRCLPGreedySolver(InelasticRateControlProblem problem,
                             CplexFactory factory) throws IloException
    {
        this.problem = problem;
        cplex = factory.getCplex();
        solution = new InelasticRateControlSolution(problem);
    }

    public boolean solve() throws IloException {
        int userCount = problem.getVariableCount();
        int linkCount = problem.getFactorCount();

        IloNumVar[] xVar = cplex.numVarArray(userCount, 0.0, 1.0);

        // construct objective
        double[] v = new double [userCount];
        for (int u = 0; u < userCount; u++)
            v[u] = problem.getUserUtility(u);
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
        
        // check if we got an integral solution
        isOptimal = true;
        for (int u = 0; u < userCount; u++) {
            double x = cplex.getValue(xVar[u]);
            if (x > 0.0 && x < 1.0) {
                isOptimal = false;
                break;
            }
        }
        
        if (isOptimal) {
            solution.reset();
            for (int u = 0; u < userCount; u++) {            
                double x = cplex.getValue(xVar[u]);
                if (x > 0.5)
                    solution.addUser(u);
            }
        }
        else {
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
                    : problem.getUserUtility(u) / p;
            }
            solution.setGreedy(bias);
        }

        return status;
    }

    public InelasticRateControlSolution getSolution() { return solution; }

    public boolean isOptimal() { return isOptimal; }
}

        
        
    