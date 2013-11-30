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
public class KernelContFunction implements StateFunction {
    private ArrayList<StoppingState> nextStates, curStates;
    private double[] nextLambda, curLambda;
    private double b, gamma;
    private GaussianKernelE gaussianKernelE;
    private GaussianKernelDoubleE gaussianKernelDoubleE;
    private StoppingModel model;

    public KernelContFunction(ArrayList<StoppingState> curStates,
                              ArrayList<StoppingState> nextStates,
                              double[] curLambda,
                              double[] nextLambda,
                              GaussianKernelE gaussianKernelE,
                              GaussianKernelDoubleE gaussianKernelDoubleE,
                              StoppingModel model,
                              double gamma,
                              double b) {
        this.curStates = curStates;
        this.nextStates = nextStates;
        this.curLambda = curLambda;
        this.nextLambda = nextLambda;
        this.gaussianKernelE = gaussianKernelE;
        this.gaussianKernelDoubleE = gaussianKernelDoubleE;
        this.model = model;
        this.gamma = gamma;
        this.b = b;
    }

    @Override
    public double value(State state) {
        double value = b;
        StoppingState stoppingState = (StoppingState)state;
        for (int i = 0; i < nextStates.size(); ++i) {
            StoppingState nState = nextStates.get(i);
            double thisVal = (1.0 / gamma) * (nextLambda[i]) * gaussianKernelE.eval(stoppingState, nState);
            value += thisVal;
        }
        for (int i = 0; i < curStates.size(); ++i) {
            StoppingState tState = curStates.get(i);
            value -= (1.0 / gamma) * curLambda[i] * gaussianKernelDoubleE.eval(stoppingState, tState);
        }
        return value;
    }

    public void setB(double b) {
        this.b = b;
    }
}
