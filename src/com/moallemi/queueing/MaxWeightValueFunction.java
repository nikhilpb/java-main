package com.moallemi.queueing;

import com.moallemi.adp.*;

public class MaxWeightValueFunction implements StateFunction {
    private OpenQueueingNetworkModel model;
    private double alpha;

    public MaxWeightValueFunction(OpenQueueingNetworkModel model, 
                                  double alpha) {
        this.model = model;
        this.alpha = alpha;
    }

    public double getValue(State state) {
        QueueState qState = (QueueState) state;
        int queueCount = qState.getQueueCount();
        double sum = 0.0;
        for (int i = 0; i < queueCount; i++)
            sum += model.getCost(i)
                * Math.pow(qState.getQueueLength(i), alpha);
        return sum;
    }
}