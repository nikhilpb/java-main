package com.moallemi.adp;

import java.io.PrintStream;
import java.util.Arrays;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.CplexFactory;
import com.moallemi.util.PropertySet;

public class OptimalPolicySolver implements Solver {
    // enumerated states
    private StateList states;
    // discount factor
    private double alpha;

    // cplex stuff
    IloCplex cplex;
    IloNumVar rhoVar = null;
    IloNumVar[] jVar;
    IloRange[][] stateActionRng;

    public OptimalPolicySolver(CplexFactory factory,
                               StateList states,
                               StateFunction costFunction,
                               double alpha)
        throws IloException
    {
        this.states = states;

        this.alpha = alpha;

        int stateCount = states.getStateCount();

        cplex = factory.getCplex();

        jVar = cplex.numVarArray(stateCount,
                                 -Double.MAX_VALUE,
                                 Double.MAX_VALUE);

        if (alpha >= 1.0) {
            rhoVar = cplex.numVar(-Double.MAX_VALUE, Double.MAX_VALUE);
            cplex.addMaximize(rhoVar);
        }
        else {
            double[] ones = new double [stateCount];
            Arrays.fill(ones, 1.0/((double) stateCount));
            cplex.addMaximize(cplex.scalProd(ones, jVar));
        }

        stateActionRng = new IloRange [stateCount][];
        for (int s = 0; s < stateCount; s++) {
            State state = states.getState(s);
            StateInfo info = states.getStateInfo(state);
            double cost = costFunction.getValue(state);
            int actionCount = info.getActionCount();
            stateActionRng[s] = new IloRange [actionCount];
            
            for (int a = 0; a < actionCount; a++) {
                StateDistribution dist = info.getDistribution(a);
                int nextStateCount = dist.getNextStateCount();
                IloNumVar[] nextStateJ = new IloNumVar [nextStateCount];
                double[] nextStateP = new double [nextStateCount];

                for (int n = 0; n < nextStateCount; n++) {
                    State nextState = dist.getNextState(n);
                    nextStateJ[n] = jVar[states.getStateIndex(nextState)];
                    nextStateP[n] = dist.getProbability(n);
                }

                if (alpha >= 1.0) {
                    stateActionRng[s][a] = 
                        cplex
                        .addLe(cplex
                               .sum(jVar[s],
                                    cplex.prod(-1.0,
                                               cplex.scalProd(nextStateP,
                                                              nextStateJ)),
                                    rhoVar),
                               cost);
                }
                else {
                    stateActionRng[s][a] = 
                        cplex
                        .addLe(cplex
                               .sum(jVar[s],
                                    cplex.prod(-alpha,
                                               cplex.scalProd(nextStateP,
                                                              nextStateJ))),
                               cost);
                }
            }
        }
    }

    public boolean solve() throws IloException {
        boolean status = cplex.solve();
        System.out.println("objective = " + cplex.getObjValue());
        return status;
    }


//     public Policy getPolicy() throws IloException {
//         return 
//             new ValueFunctionPolicy(getValueEstimate());
//         return 
//             new Policy() {
//                 public int getAction(State state, StateInfo info) {
//                     try {
//                         int s = states.getStateIndex(state);
//                         int actionCount = info.getActionCount();
                        
//                         for (int a = 0; a < actionCount; a++) {
//                             if (cplex.getBasisStatus(stateActionRng[s][a])
//                                 .equals(IloCplex
//                                         .BasisStatus.NotABasicStatus)) {
//                                 return a;
//                             }
//                         }
//                     }
//                     catch (IloException e) {
//                         throw new RuntimeException(e);
//                     }
            
//                     throw new IllegalStateException("no optimal action");
//                 }
//             };
//    }

    public StateFunction getValueEstimate() throws IloException {
        return 
            new StateFunction() {
                public double getValue(State state) {
                    try {
                        int s = states.getStateIndex(state);
                        return cplex.getValue(jVar[s]);
                    }
                    catch (IloException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
    }

    public void dumpInfo(PrintStream out) throws IloException {
        int stateCount = states.getStateCount();
        if (rhoVar != null)
            out.println("rho = " + cplex.getValue(rhoVar));
        for (int s = 0; s < stateCount; s++) {
            State state = states.getState(s);
            StateInfo info = states.getStateInfo(state);
            int actionCount = info.getActionCount();

            out.print("STATE: "  + state.toString() + " ");
            out.print(cplex.getValue(jVar[s]));
            out.println();

            for (int a = 0; a < actionCount; a++) {
                IloRange cons = stateActionRng[s][a];
                double slack = cplex.getSlack(cons);
                out.print("ACTION: " + info.getAction(a).toString() + " ");
                out.print(slack);
                if (slack < 1e-3)
                    out.print(" active");
                out.println();
            }
        }
    }
 
    public void dumpStateInfo(PrintStream out) throws IloException {
        dumpInfo(out);
    }
  
    public void exportModel(String fileName) throws IloException {
	cplex.exportModel(fileName);
    }
}