package com.moallemi.resalloc;

import java.util.Arrays;
import java.util.Random;

import com.moallemi.math.Shuffle;

public class InelasticRateControlSolution {
    private InelasticRateControlProblem problem;

    private boolean[] userStatus;
    private double[] freeLinkCapacity;
    private double objective;
    private boolean isFeasible;

    private static class BiasPair implements Comparable {
        private int u;
        private double bias;

        public int compareTo(Object other) {
            BiasPair o = (BiasPair) other;
            if (bias < o.bias)
                return -1;
            if (bias > o.bias)
                return 1;
            return 0;
        }
    }
    private BiasPair[] biasPair;


    public InelasticRateControlSolution(InelasticRateControlProblem problem) {
        this.problem = problem;

        int userCount = problem.getVariableCount();
        int linkCount = problem.getFactorCount();

        userStatus = new boolean [userCount];
        freeLinkCapacity = new double [linkCount];
        biasPair = new BiasPair [userCount];
        for (int u = 0; u < userCount; u++)
            biasPair[u] = new BiasPair();

        reset();
    }

    public void reset() {
        Arrays.fill(userStatus, false);
        for (int l = 0; l < freeLinkCapacity.length; l++)
            freeLinkCapacity[l] = problem.getLinkCapacity(l);
        objective = 0.0;
        isFeasible = true;
    }

    public double getObjectiveValue() { return objective; }
    public boolean isFeasible() { return isFeasible; }

    public int setGreedy(double[] bias) {
        reset();

        for (int u = 0; u < biasPair.length; u++) {
            biasPair[u].u = u;
            biasPair[u].bias = bias[u];
        }
        Arrays.sort(biasPair);
        
        int lastAdd = -1;
        for (int i = biasPair.length - 1; i >= 0; i--) {
            if (isFeasibleToAdd(biasPair[i].u)) {
                addUser(biasPair[i].u);
                lastAdd = biasPair[i].u;
            }
        }

        return lastAdd;
    }

    public int setGreedyShuffle(double[] bias, Random r) {
        reset();

        for (int u = 0; u < biasPair.length; u++) {
            biasPair[u].u = u;
            biasPair[u].bias = bias[u];
        }
        Shuffle.shuffle(r, biasPair);
        Arrays.sort(biasPair);
        
        int lastAdd = -1;
        for (int i = biasPair.length - 1; i >= 0; i--) {
            if (isFeasibleToAdd(biasPair[i].u)) {
                addUser(biasPair[i].u);
                lastAdd = biasPair[i].u;
            }
        }

        return lastAdd;
    }


    public boolean isFeasibleToAdd(int u) {
        if (userStatus[u])
            return false;

        int degree = problem.getVariableDegree(u);
        double b_u = problem.getUserMinBandwidth(u);
        for (int lIndex = 0; lIndex < degree; lIndex++) {
            int l = problem.getVariableNeighbor(u, lIndex);
            if (freeLinkCapacity[l] < b_u)
                return false;
        }
        return true;
    }

    public void addUser(int u) {
        if (userStatus[u])
            return;

        int degree = problem.getVariableDegree(u);
        double b_u = problem.getUserMinBandwidth(u);
        for (int lIndex = 0; lIndex < degree; lIndex++) {
            int l = problem.getVariableNeighbor(u, lIndex);
            freeLinkCapacity[l] -= b_u;
            if (freeLinkCapacity[l] < 0.0)
                isFeasible = false;
        }
        objective += problem.getUserUtility(u);
        userStatus[u] = true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int u = 0; u < userStatus.length; u++) {
            if (userStatus[u]) {
                if (sb.length() > 0)
                    sb.append(",");
                sb.append((u+1));
            }
        }
        return sb.toString();
    }

}