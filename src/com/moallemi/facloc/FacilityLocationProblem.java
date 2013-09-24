package com.moallemi.facloc;

public interface FacilityLocationProblem {

    public int getCityCount();
    
    public int getFacilityCount();

    public double getConstructionCost(int j);

    public double getDistance(int i, int j);

    public double getAllFacilityCost();

}
