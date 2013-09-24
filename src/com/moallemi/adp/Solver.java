package com.moallemi.adp;

import java.io.PrintStream;

import ilog.concert.IloException;

public interface Solver {
    public boolean solve() throws IloException;
    
    //public Policy getPolicy() throws IloException;

    public StateFunction getValueEstimate() throws IloException;

    public void dumpInfo(PrintStream out) throws IloException;

    public void dumpStateInfo(PrintStream out) throws IloException;

    public void exportModel(String fileName) throws IloException;
}
