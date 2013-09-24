package com.moallemi.queueing;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;

public class QueueStateBasisSet implements BasisSet {
    private StateFunction[] basis;
    private int queueCount;
    private int cutoff;

    public QueueStateBasisSet(int queueCount, int cutoff, double[] poly) 
    {
        this.queueCount = queueCount;
        this.cutoff = cutoff;
        ArrayList<StateFunction> list = 
            new ArrayList<StateFunction>();
        list.add(new ConstantFunction());
        for (int i = 0; i < queueCount; i++)
            for (int p = 0; p < poly.length; p++)
                list.add(new QueueLengthFunction(i, poly[p]));

        int[] queueLengths = new int [queueCount];
        Arrays.fill(queueLengths, 0);
        boolean found = true;
        while (found) {
            int[] copy = new int [queueCount];
            System.arraycopy(queueLengths, 0, copy, 0, queueCount);
            list.add(new QueueStateFunction(new QueueState(copy)));

            found = false;
            for (int i = 0; i < queueCount && !found; i++) {
                queueLengths[i]++;
                if (queueLengths[i] > cutoff)
                    queueLengths[i] = 0;
                else
                    found = true;
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
        throws IloException 
    {}
    public StateFunction getLinearCombination(double[] r) {
        return new LinearCombinationFunction(r, this);
    }

    // not efficient, should use a hash table!
    public void evaluate(State state, double[] out) {
        for (int i = 0; i < basis.length; i++)
            out[i] = basis[i].getValue(state);
    }

}
