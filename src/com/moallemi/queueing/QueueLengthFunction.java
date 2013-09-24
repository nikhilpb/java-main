package com.moallemi.queueing;

import com.moallemi.adp.*;

public class QueueLengthFunction implements StateFunction {
    private int dimension;
    private double power;

    public QueueLengthFunction(int dimension, double power) {
        this.dimension = dimension;
        this.power = power;
    }

    public double getValue(State state) {
        return Math.pow(((QueueState) state).getQueueLength(dimension),
                        power);
    }

    public String toString() {
        return "len^( " + power + " )[ " + (dimension+1) + " ]";
    }

}
