package com.nikhilpb.util.math;

import Jama.Matrix;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/1/13
 * Time: 6:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Regression {
  public static double[] LinLeastSq(double[][] xData, double[] yData) {
    Matrix xMatrix = new Matrix(xData);
    double[][] yDataAlt = new double[yData.length][1];
    for (int i = 0; i < yData.length; ++ i) {
      yDataAlt[i][0] = yData[i];
    }
    Matrix yMatrix = new Matrix(yDataAlt);
    Matrix weights = xMatrix.solve(yMatrix).transpose();
    return weights.getArray()[0];
  }
}
