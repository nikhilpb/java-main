package com.moallemi.adp;

import java.util.Arrays;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.CplexFactory;
import com.moallemi.util.PropertySet;

public class ValueFunctionCalculator {
    // enumerated states
    private StateList states;
    // discount factor
    private double alpha;

    // cplex stuff
    IloCplex cplex;
    IloNumVar rhoVar = null;
    IloNumVar[] jVar;
    IloRange[] stateRng;

    public ValueFunctionCalculator(CplexFactory factory,
                                   StateList states,
                                   StateFunction costFunction,
                                   Policy policy,
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
            cplex.addMaximize(jVar[0]);
        }

        stateRng = new IloRange [stateCount];
        for (int s = 0; s < stateCount; s++) {
            State state = states.getState(s);
            StateInfo info = states.getStateInfo(state);
            double cost = costFunction.getValue(state);
            int a = policy.getAction(state, info);

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
                stateRng[s] = 
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
                stateRng[s] = 
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

    public boolean solve() throws IloException {
        boolean status = cplex.solve();
        System.out.println("objective = " + cplex.getObjValue());
        return status;
    }


    public double getValue(State baseState) throws IloException {
        if (rhoVar != null)
            return cplex.getValue(rhoVar);
        int s = states.getStateIndex(baseState);
        return cplex.getValue(jVar[s]);
    }

    public StateFunction getValueFunction() {
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

}