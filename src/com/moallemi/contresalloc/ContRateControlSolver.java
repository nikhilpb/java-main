package com.moallemi.contresalloc;

public interface ContRateControlSolver {
    public boolean solve() throws Exception;
    public ContRateControlSolution getSolution();
    public boolean isOptimal();
}