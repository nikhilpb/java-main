package com.nikhilpb.abtesting;

import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created by nikhilpb on 8/27/14.
 */
public interface DataModel {
  public DataPoint next();
  public PSDMatrix getSigma();
}
