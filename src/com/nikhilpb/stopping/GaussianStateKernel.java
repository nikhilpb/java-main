package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateKernel;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/21/13
 * Time: 1:55 PM
 * Evaluates expression of the type K exp(-||x -y||_2^2 / 2 \rho)
 */
public class GaussianStateKernel implements StateKernel {
    private final double bandWidth, peak;

    /**
     * Constructor.
     * @param bandWidth A misnomer, should be bandwidth^2
     * @param peak Value when x = y
     */
    public GaussianStateKernel(double bandWidth, double peak) {
        this.bandWidth = bandWidth;
        this.peak = peak;
    }

    /**
     * Evaluator of the expression.
     * @param state1
     * @param state2
     * @return
     */
    public double value(State state1, State state2) {
        double val = 0.;
        StoppingState sState1 = (StoppingState)state1, sState2 = (StoppingState)state2;
        for (int i = 0; i < sState1.vector.length; ++i) {
            val +=   (sState1.vector[i] - sState2.vector[i])
                    *(sState1.vector[i] - sState2.vector[i]);
        }
        val = peak * Math.exp(- val / (2. * bandWidth));
        return val;
    }

    public double getBandWidth() {
        return bandWidth;
    }

}
