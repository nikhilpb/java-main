package com.moallemi.contresalloc;

import java.util.Arrays;
import java.util.Random;

import com.moallemi.math.Shuffle;

public class ContRateControlSolution {
    private ContRateControlProblem problem;
    private double[] userAllocation;
    private double[] linkAllocation;
    private double objective;
    private boolean isFeasible;
    private int userCount;
    private int linkCount;

    public ContRateControlSolution(ContRateControlProblem problem) {
        this.problem = problem;
        userCount = problem.getVariableCount();
        linkCount = problem.getFactorCount();
        userAllocation = new double [userCount];
        linkAllocation = new double [linkCount];
        reset();
    }

    public void reset() {
	Arrays.fill(userAllocation, 0.0);
	Arrays.fill(linkAllocation, 0.0);
        objective = Double.NEGATIVE_INFINITY;
        isFeasible = false;
    }

    public double getObjectiveValue() { return objective; }

    public boolean isFeasible() { return isFeasible; }

    public void set(double[] newUserAllocation) {
        System.arraycopy(newUserAllocation, 0, userAllocation, 0, userCount);
        update();
    }

    public void set(ContRateControlSolution other, 
                    double scale, 
                    double[] increment) 
    {
	for (int u = 0; u < userCount; u++) 
            userAllocation[u] = other.userAllocation[u]
                + scale * increment[u];
        update();
    }

    private void update() {
	objective = 0.0;
	isFeasible = true;

	for (int u = 0; u < userCount; u++) {
            if (isFeasible) {
                if (userAllocation[u] <= 0.0) 
                    isFeasible = false;
                else 
                    objective += problem.getUserUtility(u) 
                        * Math.log(userAllocation[u]);
            }
	}

	for (int l = 0; l < linkCount; l++) {
	    int degree = problem.getFactorDegree(l);
	    double capacity = problem.getLinkCapacity(l);
	    double usedCapacity = 0.0;
	    for (int uIndex = 0; uIndex < degree; uIndex++) {
		int u = problem.getFactorNeighbor(l, uIndex);
		usedCapacity += userAllocation[u];
	    }	
            linkAllocation[l] = usedCapacity;
            if (isFeasible) {
                if (usedCapacity >= capacity)
                    isFeasible = false;
                else 
                    objective += problem.getBarrierCoefficient() 
                        * Math.log(capacity - usedCapacity);
            }
        }

        if (!isFeasible)
            objective = Double.NEGATIVE_INFINITY;
    }

    public double getUserAllocation(int u) {
        return userAllocation[u];
    }

    public double getLinkAllocation(int l) {
        return linkAllocation[l];
    }

    public String toString() { 
        StringBuffer sb = new StringBuffer();
        for (int u = 0; u < userCount; u++) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(userAllocation[u]);
        }
        return sb.toString();
    }

}
