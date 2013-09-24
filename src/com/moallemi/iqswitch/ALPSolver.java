package com.moallemi.iqswitch; 

import java.io.PrintStream;
import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.*;

public class ALPSolver implements PolicySolver {
    private SwitchModel model;
    private double alpha;
    private BasisSet basis;
    
    private IloCplex cplex;
    private IloNumVar rhoVar = null;
    private IloNumVar[] rVar;

    public ALPSolver(SwitchModel model,
                     BasisSet basis,
                     SwitchState[] stateList,
                     double[] stateRelevance,
                     double alpha,
                     CplexFactory factory) 
        throws IloException
    {
        this.model = model;
        this.basis = basis;
        this.alpha = alpha;

        int stateCount = stateList.length;
        int basisSize = basis.size();
        int switchSize = model.getSwitchSize();
        Function costFunction = model.getCostFunction();

        cplex = factory.getCplex();


        double[] lb = new double [basisSize];
        double[] ub = new double [basisSize];
        Arrays.fill(lb, -Double.MAX_VALUE);
        Arrays.fill(ub, Double.MAX_VALUE);
        rVar = cplex.numVarArray(basisSize, lb, ub);

        // will hold objective value coefficients for discounted case
        double[] objCoeff = null;
        if (alpha == 1.0) {
            rhoVar = cplex.numVar(-Double.MAX_VALUE, Double.MAX_VALUE);
        }
        else if (alpha >= 0.0 && alpha < 1.0) {
            objCoeff = new double [basisSize];
            Arrays.fill(objCoeff, 0.0);
        }
        else 
            throw new IllegalArgumentException("alpha must be in [0,1]");
        
        // temporary arrays
        double[] psi = new double [basisSize];
        MatchingMatrix[] matrix = new MatchingMatrix [basisSize];
        for (int i = 0; i < basisSize; i++)
            matrix[i] = new MatchingMatrix(model);
        double[] uLB = new double [switchSize];
        double[] uUB = new double [switchSize];
        Arrays.fill(uLB, -Double.MAX_VALUE);
        Arrays.fill(uUB, Double.MAX_VALUE);
        double[] vLB = new double [switchSize];
        double[] vUB = new double [switchSize];
        Arrays.fill(vLB, -Double.MAX_VALUE);
        Arrays.fill(vUB, Double.MAX_VALUE);
        double[] w = new double [basisSize];
        double[] rCoeff = new double [basisSize];
        
        for (int s = 0; s < stateCount; s++) {
            SwitchState state = stateList[s];

            // evaluate basis functions
            basis.evaluate(state, psi);

            // update objective coefficients
            if (objCoeff != null) {
                double pi = stateRelevance[s];
                for (int i = 0; i < basisSize; i++)
                    objCoeff[i] += pi * psi[i];
            }

            // add dual form of constraints

            // construct dual variables
            IloNumVar[] uVar = cplex.numVarArray(switchSize, uLB, uUB);
            IloNumVar[] vVar = cplex.numVarArray(switchSize, vLB, vUB);

            // evaluate policy matrices
            for (int i = 0; i < basisSize; i++)
                matrix[i].reset();
            basis.addToMatrix(state, matrix);

            // add u_i + v_j <= w_ij constraints
            for (int src = 0; src < switchSize; src++) {
                for (int dest = 0; dest < switchSize; dest++) {
                    for (int i = 0; i < basisSize; i++)
                        w[i] = -matrix[i].getWeight(src, dest);
                    cplex.addLe(cplex.sum(uVar[src],
                                          vVar[dest],
                                          cplex.scalProd(w, rVar)),
                                0.0);
                }
            }
            
            // add psi r <= c(x) + alpha min_sigma \psi_sigma r
            for (int i = 0; i < basisSize; i++)
                rCoeff[i] = psi[i] - alpha * matrix[i].getOffset();
            
            if (rhoVar != null) {
                cplex.addLe(cplex
                            .sum(cplex.scalProd(rCoeff, rVar),
                                 cplex.negative(cplex.sum(cplex.sum(uVar),
                                                          cplex.sum(vVar))),
                                 rhoVar),
                            costFunction.getValue(state));
            }
            else {
                cplex.addLe(cplex.sum(cplex.scalProd(rCoeff, rVar),
                                      cplex.prod(-alpha, 
                                                 cplex.sum(cplex.sum(uVar),
                                                           cplex.sum(vVar)))),
                            costFunction.getValue(state));
            }
        }

        // add objective
        if (rhoVar != null)
            cplex.addMaximize(rhoVar);
        else
            cplex.addMaximize(cplex.scalProd(objCoeff, rVar));

    }

    public boolean solve() throws IloException {
        boolean status = cplex.solve();
        System.out.println("objective = " + cplex.getObjValue());
        return status;
    }

    public SeparableFunction getPolicyFunction() 
        throws IloException {
        double[] r = cplex.getValues(rVar);
        return new LinearCombinationFunction(r, basis);
    }

    public void dumpInfo(PrintStream out) throws IloException {
        int basisSize = basis.size();
        if (rhoVar != null)
            out.println("rho = " + cplex.getValue(rhoVar));
        for (int i = 0; i < basisSize; i++)
            out.println("coeff[" + (i+1) + "] "
                        + basis.getFunctionName(i)
                        + " = " 
                        + cplex.getValue(rVar[i]));
    }

    public void exportModel(String fileName) throws IloException {
        throw new UnsupportedOperationException();
    }
}
