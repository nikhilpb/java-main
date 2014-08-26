package com.nikhilpb.doe;

import java.io.PrintStream;

/**
 * Created by nikhilpb on 3/20/14.
 */
public class DiscretizedFunction implements OneDFunction {
  private static final int kPrintCount = 1000;
  private double upper, delta;
  private int pointsCount;
  private double[] points;
  private double[] values;

  public DiscretizedFunction(double upper, int pointsCount) {
    if (upper < 0 && pointsCount <= 1) {
      throw new IllegalArgumentException();
    }

    this.upper = upper;
    this.pointsCount = pointsCount;
    this.delta = upper / (pointsCount - 1);

    points = new double[pointsCount];
    values = new double[pointsCount];
    for (int i = 0; i < pointsCount; ++ i) {
      points[i] = i * delta;
    }
  }

  public void setValues(double[] oValues) {
    if (values.length != oValues.length) {
      throw new IllegalArgumentException("length mismatch");
    }
    for (int i = 0; i < pointsCount; ++ i) {
      values[i] = oValues[i];
    }
  }

  public double getPoint(int i) {
    if (i < 0 || i > pointsCount) {
      throw new ArrayIndexOutOfBoundsException("point not in range");
    }
    return points[i];
  }

  @Override
  public double value(double x) {
    if (x < 0. || x > upper) {
      return 0.;
    }
    int lind = (int) (x / delta);
    int hind = lind + 1;
    double dVal = values[hind] - values[lind];
    double h = x - points[lind];
    return values[lind] + (h * dVal / delta);
  }

  @Override
  public void printFn(PrintStream stream, double low, double high) {
    final double delta = (high - low) / kPrintCount;
    for (int i = 0; i < kPrintCount + 1; ++ i) {
      stream.println((low + i * delta) + "," + value(low + i * delta));
    }
  }

  @Override
  public double minAt(double searchLower, double searchUpper) {
    int minInd = - 1;
    double minVal = Double.MAX_VALUE;
    for (int i = 0; i < values.length && (points[i] >= searchLower) && (points[i] <= searchUpper); ++ i) {
      if (values[i] < minVal) {
        minVal = values[i];
        minInd = i;
      }
    }
    return points[minInd];
  }
}
