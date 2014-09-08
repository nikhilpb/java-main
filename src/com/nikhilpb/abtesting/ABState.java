package com.nikhilpb.abtesting;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class ABState {
  private int diffCount;
  private double delta[];
  private DataPoint dp;

  public void setDiffCount(int diffCount) {
    this.diffCount = diffCount;
  }

  public void setDelta(double[] delta) {
    this.delta = delta;
  }

  public void setDelta(int i, double val) {
    delta[i] = val;
  }

  public void setDp(DataPoint dp) {
    this.dp = dp;
  }

  public int getDiffCount() {

    return diffCount;
  }

  public double[] getDelta() {
    return delta;
  }

  public double getDelta(int i) {
    return delta[i];
  }

  public DataPoint getDp() {
    return dp;
  }
}
