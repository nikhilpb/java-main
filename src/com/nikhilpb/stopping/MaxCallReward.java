package com.nikhilpb.stopping;

import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.RewardFunction;
import com.nikhilpb.adp.State;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/29/13
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaxCallReward implements RewardFunction {
    private double K, r;

    public MaxCallReward(double K, double r) { this.K = K; this.r = r; }

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
        if (value < 0.) {
            return 0.;
        }
        return value * Math.pow((1+r), stoppingState.time);
    }
}
