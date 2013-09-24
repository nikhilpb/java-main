package com.moallemi.contresalloc;

public interface ContRateControlIterativeSolver {
    public void setProblem(ContRateControlProblem problem);
    public void iterate();
    public ContRateControlSolution getSolution();
    public boolean hasConverged();
}