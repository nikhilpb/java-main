package com.moallemi.iqswitch;

import java.io.PrintStream;

import ilog.concert.IloException;

public interface PolicySolver {
    public boolean solve() throws IloException;

    public SeparableFunction getPolicyFunction() throws IloException;

    public void dumpInfo(PrintStream out) throws IloException;

    //public void dumpStateInfo(PrintStream out) throws IloException;

    //public void exportModel(String fileName) throws IloException;
}
