package com.moallemi.resalloc;

public interface InelasticRateControlSolver {
    public boolean solve() throws Exception;
    public InelasticRateControlSolution getSolution();
    public boolean isOptimal();
}