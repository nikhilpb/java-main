package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 2/4/14
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class KernelContFunction2 implements StateFunction {
  private ArrayList<StoppingState> nextStates, curStates;
  private double[] nextLambda, curLambda;
  private double b;
  private GaussianKernelE gaussianKernelE;
  private GaussianKernelDoubleE gaussianKernelDoubleE;
  private StoppingModel model;

  public KernelContFunction2(ArrayList<StoppingState> curStates,
                             ArrayList<StoppingState> nextStates,
                             double[] curLambda,
                             double[] nextLambda,
                             GaussianKernelE gaussianKernelE,
                             GaussianKernelDoubleE gaussianKernelDoubleE,
                             StoppingModel model,
                             double b) {
    this.curStates = curStates;
    this.nextStates = nextStates;
    this.curLambda = curLambda;
    this.nextLambda = nextLambda;
    this.gaussianKernelE = gaussianKernelE;
    this.gaussianKernelDoubleE = gaussianKernelDoubleE;
    this.model = model;
    this.b = b;
  }

  @Override
  public double value(State state) {
    double value = b;
    StoppingState stoppingState = (StoppingState) state;
    for (int i = 0; i < nextStates.size(); ++ i) {
      StoppingState nState = nextStates.get(i);
      double thisVal = (nextLambda[i]) * gaussianKernelE.eval(stoppingState, nState);
      value += thisVal;
    }
    for (int i = 0; i < curStates.size(); ++ i) {
      StoppingState tState = curStates.get(i);
      value += curLambda[i] * gaussianKernelDoubleE.eval(stoppingState, tState);
    }
    return value;
  }
}