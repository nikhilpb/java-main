package com.moallemi.queueing;

import java.util.ArrayList;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;

public class QueueLengthBasisSet implements BasisSet {
    private StateFunction[] basis;

    public QueueLengthBasisSet(int queueCount, double[] poly) {
        ArrayList<StateFunction> list = 
            new ArrayList<StateFunction>();
        list.add(new ConstantFunction());
        for (int i = 0; i < queueCount; i++)
            for (int p = 0; p < poly.length; p++)
                list.add(new QueueLengthFunction(i, poly[p]));
        basis = list.toArray(new StateFunction [0]);
    }
        
    public int size() { return basis.length; }
//    public StateFunction getFunction(int i) { return basis[i]; }
    public String getFunctionName(int i) { return basis[i].toString(); }
    public double getMinValue(int i) { return -Double.MAX_VALUE; }
    public double getMaxValue(int i) { return Double.MAX_VALUE; }
    public void addConstraints(IloCplex cplex, IloNumVar[] rVar) 
        throws IloException
    {}
    public StateFunction getLinearCombination(double[] r) {
        return new LinearCombinationFunction(r, this);
    }

    public void evaluate(State state, double[] out) {
        for (int i = 0; i < basis.length; i++)
            out[i] = basis[i].getValue(state);
    }


}
