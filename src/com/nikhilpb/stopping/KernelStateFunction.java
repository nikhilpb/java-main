package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/11/13
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class KernelStateFunction implements StateFunction {
    private ArrayList<StoppingState> thisStates, prevStates;
    private double[] prevLambda, curLambda;
    private double b, gamma;
    GaussianStateKernel kernel;
    MeanGaussianKernel oneExp;
    StoppingModel model;

    public KernelStateFunction(ArrayList<StoppingState> thisStates,
                               ArrayList<StoppingState> prevStates,
                               double[] prevLambda,
                               double[] curLambda,
                               GaussianStateKernel kernel,
                               MeanGaussianKernel oneExp,
                               StoppingModel model,
                               double gamma,
                               double b) {
        this.thisStates = thisStates;
        this.prevStates = prevStates;
        this.prevLambda = prevLambda;
        this.curLambda = curLambda;
        this.b = b;
        this.kernel = kernel;
        this.oneExp = oneExp;
        this.gamma = gamma;
        this.model = model;
    }

    @Override
    public double value(State state) {
        double value = b;
        StoppingState stoppingState = (StoppingState)state;
        for (int i = 0; i < thisStates.size(); ++i) {
            value += (1.0 / gamma) * (curLambda[i]) * kernel.value(thisStates.get(i), stoppingState);
        }
        for (int i = 0; i < prevStates.size(); ++i) {
            StoppingState pState = prevStates.get(i);
            GaussianTransition gt = (GaussianTransition)model.getDistribution(pState, StoppingAction.CONTINUE);
            double[] mu = gt.getMean();
            for (int j = 0; j < mu.length; ++j) {
                mu[j] -= stoppingState.vector[j];
            }
            value -= (1.0 / gamma) * prevLambda[i] * oneExp.eval(mu);
        }
        return value;
    }
}
