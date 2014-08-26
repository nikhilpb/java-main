package com.nikhilpb.doe;

/**
 * Created by nikhilpb on 8/26/14.
 */

public interface Policy {
  public int getAction(double[] state, int diff, DataPoint nextPoint);
}
