package com.moallemi.adp;

import java.io.PrintStream;
import java.util.Arrays;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.*;

public class ApproximateSlackPolicySolver implements Solver {
    // list of sampled states
    private StateList states;
    // discount factor
    private double alpha;
    // basis functions
    private BasisSet basis;

    // cplex stuff
    IloCplex cplex;
    IloNumVar s1Var;
    IloNumVar s2Var;
    IloNumVar[] rVar;
    IloRange[][] stateActionRng;

    public ApproximateSlackPolicySolver(CplexFactory factory,
                                        StateList states,
                                        StateFunction costFunction,
                                        double[] weights,
                                        BasisSet basis,
                                        double alpha,
                                        double eta,
                                        StateFunction slackFunction)
        throws IloException
    {
        this.states = states;
        this.basis = basis;
        this.alpha = alpha;

        if (alpha >= 1.0)
            throw new IllegalArgumentException("only discounted case "
                                               + "is supported");
        if (!DiscreteDistribution.isDistribution(weights))
            throw new IllegalArgumentException("weights do not "
                                               + "form a distribution");
        int stateCount = states.getStateCount();
        int basisCount = basis.size();

        cplex = factory.getCplex();

        s1Var = cplex.numVar(-Double.MAX_VALUE, Double.MAX_VALUE);
        s2Var = cplex.numVar(0, Double.MAX_VALUE);
        cplex.addMinimize(cplex.sum(s1Var, cplex.prod(eta, s2Var)));

        double[] lb = new double [basisCount];
        double[] ub = new double [basisCount];
        for (int i = 0; i < basisCount; i++) {
            lb[i] = basis.getMinValue(i);
            ub[i] = basis.getMaxValue(i);
        }
        rVar = cplex.numVarArray(basisCount, lb, ub);


        double[] cPhiR = new double [basisCount];
        Arrays.fill(cPhiR, 0.0);
        double[] psi = new double [basisCount];
        for (int s = 0; s < stateCount; s++) {
            State state = states.getState(s);
            basis.evaluate(state, psi);
            for (int i = 0; i < basisCount; i++) 
                cPhiR[i] += weights[s] * psi[i];
        }

      
        stateActionRng = new IloRange [stateCount][];
        double[] values = new double [basisCount];
        double[] tmp = new double [basisCount];
        double[] bEV = new double [basisCount];

        for (int s = 0; s < stateCount; s++) {
            State state = states.getState(s);
            StateInfo info = states.getStateInfo(state);
            double cost = costFunction.getValue(state);
            double slack = slackFunction.getValue(state);
            int actionCount = info.getActionCount();
            stateActionRng[s] = new IloRange [actionCount];
            
            basis.evaluate(state, values);
            for (int a = 0; a < actionCount; a++) {
                StateDistribution dist = info.getDistribution(a);
                dist.expectedBasisValue(basis, tmp, bEV);

                double[] coeffs = new double [basisCount];
                           
                for (int i = 0; i < basisCount; i++) 
                    coeffs[i] = values[i] 
                        - (alpha * bEV[i])
                        - (1.0 - alpha) * cPhiR[i];
                    
                stateActionRng[s][a] = 
                    cplex
                    .addLe(cplex.sum(cplex.scalProd(coeffs,
                                                    rVar),
                                     cplex.prod(-1.0, s1Var),
                                     cplex.prod(-slack, s2Var)),
                           cost);
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