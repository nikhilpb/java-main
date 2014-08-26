package com.nikhilpb.doe;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 5/5/14
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
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
    String str = "";
    for (int i = 0; i < dataVector.length; ++ i) {
      str += dataVector[i] + " ";
    }
    return str;
  }
}
