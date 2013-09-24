package com.moallemi.facloc;

public interface FacilityLocationSolver {
    public double getObjectiveValue();
    public String getOptimalFacilitiesString();
    public boolean[] getOptimalFacilities();
    public boolean isGlobalOptimum();
}
