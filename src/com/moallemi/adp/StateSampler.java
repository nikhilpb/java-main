package com.moallemi.adp;

import java.util.Random;

public interface StateSampler {

    public void sample(Random random, int stateCount);

    public StateList getStateList();

    public double[] getWeights();
}