package com.nikhilpb.abtesting;

import Jama.Matrix;
import com.nikhilpb.util.math.Distributions;
import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created by nikhilpb on 8/28/14.
 */
public class GaussianModel implements DataModel {
  private Distributions.GaussianVectorGen gaussianVectorGen;
  private int timePeriods;
  private Matrix mu;
  private PSDMatrix covarMatrix;
  private long seed;
  private int dim;

  public GaussianModel(Matrix mu,
                       PSDMatrix covarMatrix,
                       int timePeriods,
                       long seed) {
    gaussianVectorGen = new Distributions.GaussianVectorGen(mu, covarMatrix, seed);
    this.timePeriods = timePeriods;
    this.seed = seed;
    this.mu = mu;
    this.covarMatrix = covarMatrix;
    this.dim = covarMatrix.mat().getColumnDimension();
  }

  /**
   * Returns a Gaussian vector of given mean and covariance matrices.
   * @return
   */
  @Override
  public DataPoint next() {
    return new DataPoint(gaussianVectorGen.nextValue());
  }

  /**
   * Returns the covariance matrix.
   * @return
   */
  @Override
  public PSDMatrix getSigma() {
    return covarMatrix;
  }


  @Override
  public int dim() { return dim + 1; }
}
