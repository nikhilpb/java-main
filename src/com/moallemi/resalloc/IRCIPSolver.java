package com.moallemi.resalloc;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.CplexFactory;

public class IRCIPSolver implements InelasticRateControlSolver {
    private InelasticRateControlProblem problem;
    private InelasticRateControlSolution solution;
    private IloCplex cplex;
    private boolean isOptimal = false;

    public IRCIPSolver(InelasticRateControlProblem problem,
                       CplexFactory factory) throws IloException
    {
        this.problem = problem;
        cplex = factory.getCplex();
        solution = new InelasticRateControlSolution(problem);
    }

    public boolean solve() throws IloException {
        int userCount = problem.getVariableCount();
        int linkCount = problem.getFactorCount();

        IloIntVar[] xVar = cplex.boolVarArray(userCount);

        // construct objective
        double[] v = new double [userCount];
        for (int u = 0; u < userCount; u++)
            v[u] = problem.getUserUtility(u);
        cplex.addMaximize(cplex.scalProd(v, xVar));
        
        // add constraints
        for (int l = 0; l < linkCount; l++) {
            int degree = problem.getFactorDegree(l);
            double[] b = new double [degree];
            IloIntVar[] xLVar = new IloIntVar [degree];
            for (int uIndex = 0; uIndex < degree; uIndex++) {
                int u = problem.getFactorNeighbor(l, uIndex);
                b[uIndex] = problem.getUserMinBandwidth(u);
                xLVar[uIndex] = xVar[u];
            }
            cplex.addLe(cplex.scalProd(b, xLVar), problem.getLinkCapacity(l));
        }

        boolean status = cplex.solve();
        isOptimal = cplex.getStatus() == IloCplex.Status.Optimal;

        solution.reset();
        for (int u = 0; u < userCount; u++) {
            double x = cplex.getValue(xVar[u]);
            if (x > 0.5)
                solution.addUser(u);
        }

        return status;
    }

    public InelasticRateControlSolution getSolution() { return solution; }

    public boolean isOptimal() { return isOptimal; }
}

        
        
    