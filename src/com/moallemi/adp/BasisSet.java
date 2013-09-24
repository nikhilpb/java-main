package com.moallemi.adp;

import ilog.concert.*;
import ilog.cplex.*;

public interface BasisSet {
    public int size();
//    public StateFunction getFunction(int i);
    public String getFunctionName(int i);
    public double getMinValue(int i);
    public double getMaxValue(int i);
    public void evaluate(State state, double[] out);
    public void addConstraints(IloCplex cplex, IloNumVar[] rVar) 
        throws IloException;
    public StateFunction getLinearCombination(double[] r);
}
