package com.moallemi.queueing;

import com.moallemi.adp.*;

public class SymmetricQueueLengthFunction implements StateFunction {
    private double power;

    public SymmetricQueueLengthFunction(double power) {
        this.power = power;
    }

    public double getValue(State state) {
        QueueState qState = (QueueState) state;
        int queueCount = qState.getQueueCount();
        double sum = 0.0;
        for (int i = 0; i < queueCount; i++) {
            int q = qState.getQueueLength(i);
            sum += Math.pow(q, power);
        }
        return sum;
    }

    public String toString() {
        return "symqlen^( " + power + " )";
    }

}
