package com.moallemi.contresalloc;

import com.moallemi.minsum.*;

public class ContRateControlProblem extends FactorGraph {
    
    protected double[] userMinBandwidth;
    protected double[] userUtility;
    protected double[] linkCapacity;
    protected double barrierCoefficient;


    public ContRateControlProblem(int varCount, 
				  int factorCount) {
        super(varCount, factorCount);
        userMinBandwidth = new double [varCount];
        userUtility = new double [varCount];
        linkCapacity = new double [factorCount];
	barrierCoefficient = 0;
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
    public void setBarrierCoefficient(double f){
	barrierCoefficient = f;
    }

    public double getUserMinBandwidth(int u) { return userMinBandwidth[u]; }
    public double getUserUtility(int u) { return userUtility[u]; }
    public double getLinkCapacity(int l) { return linkCapacity[l]; }
    public double getBarrierCoefficient() { return barrierCoefficient;}
}
