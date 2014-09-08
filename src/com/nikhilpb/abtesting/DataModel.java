package com.nikhilpb.abtesting;

import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created by nikhilpb on 8/27/14.
 */
public interface DataModel {
  /**
   *
   * @return The next data point.
   */
  public DataPoint next();

  /**
   *
   * @return Covariance matrix.
   */
  public PSDMatrix getSigma();

  /**
   *
   * @return dimension
   */
  public int dim();
}
