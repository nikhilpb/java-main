package com.nikhilpb.abtesting;

/**
 * Created by nikhilpb on 9/8/14.
 */
public interface ABPolicy {
  public ABAction getAction(ABState state, DataPoint nextPoint);
}
