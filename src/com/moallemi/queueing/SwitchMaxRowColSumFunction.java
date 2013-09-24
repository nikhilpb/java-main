package com.moallemi.queueing;

import com.moallemi.adp.*;

public class SwitchMaxRowColSumFunction implements StateFunction {
    private SwitchModel model;
    private double power;

    public SwitchMaxRowColSumFunction(SwitchModel model, double power) {
        this.model = model;
        this.power = power;
    }

    public double getValue(State state) {
        QueueState qState = (QueueState) state;
        double max = 0.0;
        int switchSize = model.getSwitchSize();

        // check row sums
        for (int row = 0; row < switchSize; row++) {
            double sum = 0.0;
            for (int col = 0; col < switchSize; col++) {
                int q = model.getQueueIndex(row, col);
                sum += Math.pow(qState.getQueueLength(q), power);
            }
            if (sum > max)
                max = sum;
        }

        // check column sums
        for (int col = 0; col < switchSize; col++) {
            double sum = 0.0;
            for (int row = 0; row < switchSize; row++) {
                int q = model.getQueueIndex(row, col);
                sum += Math.pow(qState.getQueueLength(q), power);
            }
            if (sum > max)
                max = sum;
        }

        return max;
    }

    public String toString() {
        return "swmaxrowcol^( " + power + " )";
    }

}
