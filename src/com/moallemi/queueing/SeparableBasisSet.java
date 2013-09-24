package com.moallemi.queueing;

import java.util.ArrayList;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;

public class SeparableBasisSet implements BasisSet {
    private StateFunction[] basis;
    private int[][] indexes;
    private int queueCount;
    private int cutoff;

    public SeparableBasisSet(int queueCount, int cutoff, double[] poly) {
        this.queueCount = queueCount;
        this.cutoff = cutoff;
        ArrayList<StateFunction> list = new ArrayList<StateFunction>();
        list.add(new ConstantFunction());
        for (int i = 0; i < queueCount; i++)
            for (int p = 0; p < poly.length; p++)
                list.add(new QueueLengthFunction(i, poly[p]));
        indexes = new int [queueCount][cutoff+1];
        for (int i = 0; i < queueCount; i++) {
            for (int q = 0; q <= cutoff; q++) {
                indexes[i][q] = list.size();
                list.add(new SeparableQueueFunction(i, q));
            }
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
        for (int i = 0; i < queueCount; i++) 
            for (int q = 0; q < cutoff; q++) 
                cplex.addGe(cplex
                            .sum(rVar[indexes[i][q+1]],
                                 cplex.prod(-1.0,
                                            rVar[indexes[i][q]])),
                            0.0);
        // convexity constraints
        for (int i = 0; i < queueCount; i++) 
            for (int q = 0; q < cutoff - 1; q++) 
                cplex.addGe(cplex
                            .sum(rVar[indexes[i][q+2]],
                                 cplex.prod(-2.0,
                                            rVar[indexes[i][q+1]]),
                                 rVar[indexes[i][q]]),
                            0.0);
        */
    }

    public StateFunction getLinearCombination(double[] r) {
        return new LinearCombinationFunction(r, this);
    }

    public void evaluate(State state, double[] out) {
        for (int i = 0; i < basis.length; i++)
            out[i] = basis[i].getValue(state);
    }

}
