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
  GaussianKernelE gaussianKernelE;
  private StoppingModel model;
  private double gamma;

  public KernelStateFunction(ArrayList<StoppingState> prevStates,
                             ArrayList<StoppingState> curStates,
                             double[] prevLambda,
                             double[] curLambda,
                             GaussianStateKernel kernel,
                             GaussianKernelE gaussianKernelE,
                             StoppingModel model,
                             double gamma) {
    this.prevStates = prevStates;
    this.curStates = curStates;
    this.prevLambda = prevLambda;
    this.curLambda = curLambda;
    this.kernel = kernel;
    this.gaussianKernelE = gaussianKernelE;
    this.model = model;
    this.gamma = gamma;
  }

  @Override
  public double value(State state) {
    double value = 0.;
    for (int i = 0; i < curStates.size(); ++ i) {
      value += (1. / gamma) * curLambda[i] * kernel.value(curStates.get(i), state);
    }
    for (int i = 0; i < prevStates.size(); ++ i) {
      State prevState = prevStates.get(i);
      StoppingState stopState = (StoppingState) state;
      value -= (1. / gamma) * prevLambda[i] * gaussianKernelE.eval((StoppingState) prevState, stopState);
    }
    return value;
  }
}
