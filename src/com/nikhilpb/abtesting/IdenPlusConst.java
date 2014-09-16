package com.nikhilpb.abtesting;

/**
 * Created by nikhilpb on 9/16/14.
 */
public class IdenPlusConst implements OneDFunction {
  private double offset;

  public IdenPlusConst(double offset) {
    this.offset = offset;
  }

  @Override
  public double value(double lambda) {
    return offset + lambda;
  }
}
