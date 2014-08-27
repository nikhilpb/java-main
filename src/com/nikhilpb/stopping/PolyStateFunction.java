package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/29/13
 * Time: 8:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PolyStateFunction implements StateFunction {
  private int degree;
  private int index;

  public PolyStateFunction(int degree, int index) {
    this.degree = degree;
    this.index = index;
  }

  @Override
  public double value(State state) {
    StoppingState stoppingState = (StoppingState) state;
    return Math.pow(stoppingState.vector[index], (double) degree);
  }

  @Override
  public String toString() {
    return "poly_" + degree + "," + index;
  }
}
