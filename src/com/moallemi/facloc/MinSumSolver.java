package com.moallemi.facloc;

public interface MinSumSolver extends FacilityLocationSolver {
    public void iterate();
    public double getBellmanError();
    public double getObjectiveValue();
    public double getSumNorm();
    public String getOptimalFacilitiesString();
    public boolean[] getOptimalFacilities();
    public boolean isGlobalOptimum();
}
