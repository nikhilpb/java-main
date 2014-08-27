package com.nikhilpb.abtesting;

/**
 * Created by nikhilpb on 8/27/14.
 */
public class DataPoint {
  private final double[] dataVector;

  public DataPoint(double[] dataVector) {
    this.dataVector = dataVector;
  }

  public double[] getDataVector() {
    return dataVector;
  }

  public double get(int i) {
    return dataVector[i];
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < dataVector.length; ++ i) {
      builder.append(dataVector[i])
             .append(" ");
    }
    return builder.toString();
  }
}
