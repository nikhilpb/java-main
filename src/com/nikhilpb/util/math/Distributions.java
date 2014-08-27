package com.nikhilpb.util.math;

import Jama.Matrix;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/14/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Distributions {
  public static class GaussianVectorGen {
    private Matrix mean, sqrtSigma;
    private Random random;
    private int size;

    public GaussianVectorGen(Matrix mean, PSDMatrix sigma, long seed) {
      if (mean.getColumnDimension() != 1) {
        throw new IllegalArgumentException("mean must gave one column");
      }

      this.mean = mean;
      sqrtSigma = sigma.sqrt();
      if (sqrtSigma.getRowDimension() != mean.getRowDimension())
        throw new IllegalArgumentException("mean and sigma dimensions don't match");
      size = sqrtSigma.getRowDimension();
      random = new Random(seed);
    }

    private Matrix standardNormal() {
      double[][] outVec = new double[mean.getRowDimension()][1];
      for (int i = 0; i < outVec.length; ++ i) {
        outVec[i][0] = random.nextGaussian();
      }
      return new Matrix(outVec);
    }

    public double[] nextValue() {
      Matrix out = standardNormal();
      out = sqrtSigma.times(out);
      out = out.plus(mean);
      out = out.transpose();
      return out.getArray()[0];
    }

    public int size() {
      return size;
    }

    public void resetSeed(long seed) {
      random = new Random(seed);
    }

    public Matrix getMean() {
      return mean;
    }
  }
}
