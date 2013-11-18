package com.nikhilpb.stopping;

import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.RewardFunction;
import com.nikhilpb.adp.State;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/18/13
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaxPutReward implements RewardFunction {
    private double K;

    public MaxPutReward(double K) { this.K = K; }

    @Override
    public double value(State state, Action action) {
        double value = 0.;
        StoppingState stoppingState = (StoppingState) state;
        StoppingAction stoppingAction = (StoppingAction) action;
        if (stoppingAction == StoppingAction.CONTINUE) {
            return value;
        }
        int n = stoppingState.vector.length;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < n; ++i) {
            double stockPrice = Math.exp(stoppingState.vector[i]);
            if (stockPrice < min) {
                min = stockPrice;
            }
        }
        value = K - min;
        if (value < 0.) {
            return 0.;
        }
        return value;
    }
}
