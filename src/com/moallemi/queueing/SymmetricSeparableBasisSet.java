package com.moallemi.queueing;

import java.util.ArrayList;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;

public class SymmetricSeparableBasisSet implements BasisSet {
    private StateFunction[] basis;
    private int[] indexes;
    private int queueCount;
    private int cutoff;

    public SymmetricSeparableBasisSet(int queueCount, 
                                      int cutoff, 
                                      double[] poly) 
    {
        this.queueCount = queueCount;
        this.cutoff = cutoff;
        ArrayList<StateFunction> list = new ArrayList<StateFunction>();
        list.add(new ConstantFunction());
        for (int p = 0; p < poly.length; p++)
            list.add(new SymmetricQueueLengthFunction(poly[p]));
        indexes = new int [cutoff+1];
        for (int q = 0; q <= cutoff; q++) {
            indexes[q] = list.size();
            list.add(new SymmetricSeparableQueueFunction(q));
        }
        basis = list.toArray(new StateFunction [0]);
    }
        
    public int size() { return basis.length; }
//     public StateFunction getFunction(int i) { return basis[i]; }
    public String getFunctionName(int i) { return basis[i].toString(); }
    public double getMinValue(int i) { return -Double.MAX_VALUE; }
    public double getMaxValue(int i) { return Double.MAX_VALUE; }
    public void addConstraints(IloCplex cplex, IloNumVar[] rVar) 
        throws IloException {
        /*
        // positive slope constraints
        for (int q = 0; q < cutoff; q++) 
            cplex.addGe(cplex
                        .sum(rVar[indexes[q+1]],
                             cplex.prod(-1.0,
                                        rVar[indexes[q]])),
                            0.0);
        // convexity constraints
        for (int q = 0; q < cutoff - 1; q++) 
            cplex.addGe(cplex
                        .sum(rVar[indexes[q+2]],
                             cplex.prod(-2.0,
                                        rVar[indexes[q+1]]),
                             rVar[indexes[q]]),
                        0.0);
        */
    }

    public StateFunction getLinearCombination(double[] r) {
        return new LinearCombinationFunction(r, this);
    }

    // not efficient, evaluate in a loop
    public void evaluate(State state, double[] out) {
        for (int i = 0; i < basis.length; i++)
            out[i] = basis[i].getValue(state);
    }

}
