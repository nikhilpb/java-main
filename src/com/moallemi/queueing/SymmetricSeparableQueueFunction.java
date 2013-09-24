package com.moallemi.queueing;

import com.moallemi.adp.*;

public class SymmetricSeparableQueueFunction implements StateFunction {
    private int dimension, length;

    public SymmetricSeparableQueueFunction(int length) {
        this.length = length;
    }

    public double getValue(State state) {
        QueueState qState = (QueueState) state;
        int queueCount = qState.getQueueCount();
        double sum = 0.0;
        for (int i = 0; i < queueCount; i++) {
            if (qState.getQueueLength(i) == length)
                sum++;
        }
        return sum;
    }

    public String toString() {
        return "symsep[ " + length + " ]";
    }
}
