package com.nikhilpb.abtesting;

/**
 * Created by nikhilpb on 9/16/14.
 */
public class DiscretizedFunction implements OneDFunction {
  private double upper;
  private int pointsCount;
  private double delta;
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

  public void setValue(int i, double val) {
    values[i] = val;
  }

  public double getPoint(int i) {
    if (i < 0 || i > pointsCount) {
      throw new ArrayIndexOutOfBoundsException("point not in range");
    }
    return points[i];
  }

  public double[] points() { return points; }

  @Override
  public double value(double x) {
    if (x < 0.) {
      return 0.;
    }
    if (x > upper) {
      return values[pointsCount-1] + (values[pointsCount-1] - values[pointsCount-2])
                                             * (x - points[pointsCount-1]) / delta;
    }
    int lind = (int) (x / delta);
    int hind = lind + 1;
    double dVal = values[hind] - values[lind];
    double h = x - points[lind];
    return values[lind] + (h * dVal / delta);
  }


}
