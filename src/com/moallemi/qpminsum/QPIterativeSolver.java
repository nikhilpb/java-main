package com.moallemi.qpminsum;

public interface QPIterativeSolver {
    public void setProblem(QuadraticPairwiseProblem problem);
    public void iterate();
    public double[] getSolution();
}