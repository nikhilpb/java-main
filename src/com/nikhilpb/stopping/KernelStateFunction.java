package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/11/13
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class KernelStateFunction implements StateFunction {
    private ArrayList<StoppingState> nextStates, curStates;
    private double[] nextLambda, curLambda;
    private double b, gamma;
    private MeanGaussianKernel oneExp, twoExp;
    private StoppingModel model;

    public KernelStateFunction(ArrayList<StoppingState> curStates,
                               ArrayList<StoppingState> nextStates,
                               double[] curLambda,
                               double[] nextLambda,
                               MeanGaussianKernel oneExp,
                               MeanGaussianKernel twoExp,
                               StoppingModel model,
                               double gamma,
                               double b) {
        this.curStates = curStates;
        this.nextStates = nextStates;
        this.curLambda = curLambda;
        this.nextLambda = nextLambda;
        this.oneExp = oneExp;
        this.twoExp = twoExp;
        this.model = model;
        this.gamma = gamma;
        this.b = b;
        System.out.printf("cur state size: %d, lmd size: %d\n", curStates.size(), curLambda.length);
        System.out.printf("next state size: %d, lmd size: %d\n", nextStates.size(), nextLambda.length);
    }

    @Override
    public double value(State state) {
        double value = b;
        StoppingState stoppingState = (StoppingState)state;
        for (int i = 0; i < nextStates.size(); ++i) {
            StoppingState nState = nextStates.get(i);
            GaussianTransition gt = (GaussianTransition)model.getDistribution(state, StoppingAction.CONTINUE);
            double[] mu = gt.baseState;
            for (int j = 0; j < mu.length; ++j) {
                mu[j] -= nState.vector[j];
            }
            value += (1.0 / gamma) * (curLambda[i]) * oneExp.eval(mu);
        }
        for (int i = 0; i < curStates.size(); ++i) {
            StoppingState tState = curStates.get(i);
            double[] mu = tState.getDifference(stoppingState);
            value -= (1.0 / gamma) * curLambda[i] * twoExp.eval(mu);
        }
        return value;
    }
}
