package com.moallemi.resalloc;

import com.moallemi.minsum.*;

public class InelasticRateControlProblem extends FactorGraph {
    
    protected double[] userMinBandwidth;
    protected double[] userUtility;
    protected double[] linkCapacity;

    public InelasticRateControlProblem(int varCount, 
                                       int factorCount) {
        super(varCount, factorCount);
        userMinBandwidth = new double [varCount];
        userUtility = new double [varCount];
        linkCapacity = new double [factorCount];
    }

    public void setUserMinBandwidth(int u, double x) {
        userMinBandwidth[u] = x;
    }
    public void setUserUtility(int u, double x) {
        userUtility[u] = x;
    }
    public void setLinkCapacity(int l, double x) {
        linkCapacity[l] = x;
    }

    public double getUserMinBandwidth(int u) { return userMinBandwidth[u]; }
    public double getUserUtility(int u) { return userUtility[u]; }
    public double getLinkCapacity(int l) { return linkCapacity[l]; }
}