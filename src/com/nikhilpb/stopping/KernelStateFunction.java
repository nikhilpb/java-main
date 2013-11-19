package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/19/13
 * Time: 12:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class KernelStateFunction implements StateFunction {
    private ArrayList<StoppingState> prevStates, curStates;
    private double[] prevLambda, curLambda;
    private GaussianStateKernel kernel;
    private MeanGaussianKernel oneExp;
    private StoppingModel model;
    private double gamma;

    public KernelStateFunction(ArrayList<StoppingState> prevStates,
                               ArrayList<StoppingState> curStates,
                               double[] prevLambda,
                               double[] curLambda,
                               GaussianStateKernel kernel,
                               MeanGaussianKernel oneExp,
                               StoppingModel model,
                               double gamma) {
        this.prevStates = prevStates;
        this.curStates = curStates;
        this.prevLambda = prevLambda;
        this.curLambda = curLambda;
        this.kernel = kernel;
        this.oneExp = oneExp;
        this.model = model;
        this.gamma = gamma;
    }

    @Override
    public double value(State state) {
        double value = 0.;
        for (int i = 0; i < curStates.size(); ++i) {
            value += (1./gamma) * curLambda[i] * kernel.value(curStates.get(i), state);
        }
        for (int i = 0; i < prevStates.size(); ++i) {
            State prevState = prevStates.get(i);
            GaussianTransition gt = (GaussianTransition)model.getDistribution(prevState, StoppingAction.CONTINUE);
            StoppingState stopState = (StoppingState)state;
            double[] mu = new double[gt.getMean().length];
            for (int j = 0; j < stopState.vector.length; ++j) {
                mu[j] = gt.getMean()[j] - stopState.vector[j];
            }
            value -= (1./gamma) * prevLambda[i] * oneExp.eval(mu);
        }
        return value;
    }
}
