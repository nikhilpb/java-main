package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;
import com.nikhilpb.adp.StateDistribution;
import com.nikhilpb.util.math.Distributions;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/13/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaussianTransition implements StateDistribution {
    Distributions.GaussianVectorGen gen;
    double[] baseState;
    int timePeriod;

    public GaussianTransition(Distributions.GaussianVectorGen gen,
                              double[] baseState,
                              int timePeriod) {
        this.gen = gen;
        if (gen.size() != baseState.length) {
            throw new IllegalArgumentException("the base state must be the same size as the generator");
        }
        this.baseState = baseState;
        this.timePeriod = timePeriod;
    }

    public double expectedValue(StateFunction s) {
        return 0.0;
    }

    public State nextSample() {
        double[] nextState = gen.nextValue();
        System.out.println(Arrays.toString(nextState));
        for (int i = 0; i < nextState.length; ++i) {
            nextState[i] += baseState[i];
        }
        return new StoppingState(nextState, timePeriod);
    }

    public double[] getMean() {
        double[][] meanArray = gen.getMean().getArray();
        double[] mean = new double[meanArray.length];
        for (int i = 0; i < mean.length; ++i) {
            mean[i] = baseState[i] + meanArray[i][0];
        }
        return mean;
    }

}
