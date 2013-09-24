package com.moallemi.adp;

import java.io.PrintStream;
import java.util.Arrays;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.*;

public class ApproximatePolicySolver implements Solver {
    // list of sampled states
    private StateList states;
    // discount factor
    //private double alpha;
    // basis functions
    private BasisSet basis;

    // cplex stuff
    IloCplex cplex;
    IloNumVar rhoVar = null;
    IloNumVar[] rVar;
    IloRange[][] stateActionRng;

    public ApproximatePolicySolver(CplexFactory factory,
                                   StateList states,
                                   StateFunction costFunction,
                                   double[] weights,
                                   BasisSet basis,
                                   double alpha)
        throws IloException
    {
        this.states = states;
        this.basis = basis;

        if (alpha < 1.0 && !DiscreteDistribution.isDistribution(weights))
            throw new IllegalArgumentException("weights do not "
                                               + "form a distribution");

        int stateCount = states.getStateCount();
        int basisCount = basis.size();

        cplex = factory.getCplex();
        
        double[] lb = new double [basisCount];
        double[] ub = new double [basisCount];
        for (int i = 0; i < basisCount; i++) {
            lb[i] = basis.getMinValue(i);
            ub[i] = basis.getMaxValue(i);
        }
        rVar = cplex.numVarArray(basisCount, lb, ub);


        if (alpha >= 1.0) {
            rhoVar = cplex.numVar(-Double.MAX_VALUE, Double.MAX_VALUE);
            cplex.addMaximize(rhoVar);
        }
        else {
            double[] objCoeff = new double [basisCount];

            Arrays.fill(objCoeff, 0.0);
            double[] psi = new double [basisCount];
            for (int s = 0; s < stateCount; s++) {
                State state = states.getState(s);
                basis.evaluate(state, psi);
                for (int i = 0; i < basisCount; i++) 
                    objCoeff[i] += weights[s] * psi[i];
            }
            cplex.addMaximize(cplex.scalProd(objCoeff, rVar));
        }
      

        stateActionRng = new IloRange [stateCount][];
        double[] values = new double [basisCount];
        double[] tmp = new double [basisCount];
        double[] bEV = new double [basisCount];

        for (int s = 0; s < stateCount; s++) {
            State state = states.getState(s);
            StateInfo info = states.getStateInfo(state);
            double cost = costFunction.getValue(state);
            int actionCount = info.getActionCount();
            stateActionRng[s] = new IloRange [actionCount];
            
            basis.evaluate(state, values);
            for (int a = 0; a < actionCount; a++) {
                StateDistribution dist = info.getDistribution(a);
                dist.expectedBasisValue(basis, tmp, bEV);

                double[] coeffs = new double [basisCount];
                if (alpha >= 1.0) {
                    for (int i = 0; i < basisCount; i++) 
                        coeffs[i] = values[i] - bEV[i];
                    
                    stateActionRng[s][a] = 
                        cplex
                        .addLe(cplex.sum(cplex.scalProd(coeffs,
                                                        rVar),
                                         rhoVar),
                               cost);
                    
                }
                else {
                    for (int i = 0; i < basisCount; i++) 
                        coeffs[i] = values[i] - alpha * bEV[i];
                    
                    stateActionRng[s][a] = 
                        cplex
                        .addLe(cplex.scalProd(coeffs,
                                              rVar),
                               cost);
                }
                
            }
        }

        basis.addConstraints(cplex, rVar);
    }

    public boolean solve() throws IloException {
        boolean status = cplex.solve();
        System.out.println("objective = " + cplex.getObjValue());
        return status;
    }

    public StateFunction getValueEstimate() throws IloException {
        double[] r = cplex.getValues(rVar);
        return new LinearCombinationFunction(r, basis);
    }
 
    public void dumpInfo(PrintStream out) throws IloException {
        int basisCount = basis.size();

        if (rhoVar != null)
            out.println("rho = " + cplex.getValue(rhoVar));
        for (int i = 0; i < basisCount; i++)
            out.println("coeff " 
                        + basis.getFunctionName(i)
                        + " = " 
                        + cplex.getValue(rVar[i]));

    }   

    public void dumpStateInfo(PrintStream out) throws IloException {
        int stateCount = states.getStateCount();
        for (int s = 0; s < stateCount; s++) {
            State state = states.getState(s);
            StateInfo info = states.getStateInfo(state);
            int actionCount = info.getActionCount();

            for (int a = 0; a < actionCount; a++) {
                IloRange cons = stateActionRng[s][a];
                double slack = cplex.getSlack(cons);
                out.print("STATE: "  + state.toString() + " ");
                out.print("ACTION: " + info.getAction(a).toString() + " ");
                out.print(slack);
                if (slack < 1e-3)
                    out.print(" active");
                out.println();
            }
        }
    }

    public void exportModel(String fileName) throws IloException {
	cplex.exportModel(fileName);
    }
}
