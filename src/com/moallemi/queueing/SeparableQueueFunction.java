package com.moallemi.queueing;

import com.moallemi.adp.*;

public class SeparableQueueFunction implements StateFunction {
    private int dimension, length;

    public SeparableQueueFunction(int dimension, int length) {
        this.dimension = dimension;
        this.length = length;
    }

    public double getValue(State state) {
        return ((QueueState) state).getQueueLength(dimension) == length 
            ? 1.0 : 0.0;
    }

    public String toString() {
        return "separable[ " + (dimension+1) + " ][ " + length + " ]";
    }
}
