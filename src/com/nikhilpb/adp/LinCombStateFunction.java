package com.nikhilpb.adp;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/3/13
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinCombStateFunction implements StateFunction {
  private final double[] coeffs;
  private final BasisSet stateFunctions;

  public LinCombStateFunction(double[] coeffs, BasisSet stateFunctions) {
    if (coeffs.length != stateFunctions.size()) {
      throw new RuntimeException("sizes of coefficients and state functions don't match");
    }
    this.coeffs = coeffs;
    this.stateFunctions = stateFunctions;
  }

  @Override
  public double value(State state) {
    double value = 0.;
    for (int i = 0; i < coeffs.length; ++ i) {
      value += coeffs[i] * stateFunctions.get(i).value(state);
    }
    return value;
  }

  @Override
  public String toString() {
    String name = "";
    for (int i = 0; i < coeffs.length; ++ i) {
      name += coeffs[i] + "*" + stateFunctions.get(i).toString();
      if (i < coeffs.length - 1) {
        name += " + ";
      }
    }
    return name;
  }
}
