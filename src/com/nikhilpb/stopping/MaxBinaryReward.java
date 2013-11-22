package com.nikhilpb.stopping;

import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.RewardFunction;
import com.nikhilpb.adp.State;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/22/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class MaxBinaryReward implements RewardFunction {
    private double K, r, up;

    public MaxBinaryReward(double K, double r, double up) {
        this.K = K;
        this.r = r;
        this.up = up;
    }

    @Override
    public double value(State state, Action action) {
        double value = 0.;
        StoppingState stoppingState = (StoppingState) state;
        StoppingAction stoppingAction = (StoppingAction) action;
        if (stoppingAction == StoppingAction.CONTINUE) {
            return value;
        }
        int n = stoppingState.vector.length;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < n; ++i) {
            double stockPrice = Math.exp(stoppingState.vector[i]);
            if (stockPrice > max) {
                max = stockPrice;
            }
        }
        value = max - K;
        if (value < 0. || value > 10.) {
            return 0.;
        } else {
            return 1.0 / Math.pow((1+r), stoppingState.time);
        }
    }
}