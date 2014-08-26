package com.nikhilpb.doe;

import Jama.Matrix;
import com.nikhilpb.util.math.Distributions;
import com.nikhilpb.util.math.PSDMatrix;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 5/5/14
 * Time: 8:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaussianModel {
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

  public DataPoint next() {
    return new DataPoint(gaussianVectorGen.nextValue());
  }

  public PSDMatrix getCovarMatrix() {
    return covarMatrix;
  }

  public int getTimePeriods() {
    return timePeriods;
  }

  public int getDim() {
    return dim;
  }
}
