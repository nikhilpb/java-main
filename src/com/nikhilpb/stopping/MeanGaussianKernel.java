package com.nikhilpb.stopping;

import Jama.Matrix;
import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/21/13
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class MeanGaussianKernel {
  private PSDMatrix sigma;
  private double[][] sigmaHat;
  private double rho, scale, peak;
  private int size;

  public MeanGaussianKernel(PSDMatrix sigma, double rho, double peak) {
    this.rho = rho;
    this.sigma = sigma;
    this.peak = peak;
    init();
  }

  private void init() {
    Matrix sigmaMat = sigma.mat().copy();
    size = sigmaMat.getColumnDimension();
    Matrix scaledSigmaMat = sigmaMat.times(1. / rho);
    scale = Matrix.identity(size, size)
                    .plus(scaledSigmaMat).det();
    scale = peak / Math.sqrt(scale);
    Matrix sigmaInv = sigmaMat.inverse();
    Matrix mat = sigmaInv.plus(Matrix.identity(size, size)
                                       .times(1. / rho))
                         .inverse();
    Matrix mat2 = sigmaInv.copy()
                          .times(mat)
                          .times(sigmaInv)
                          .times(- 1.)
                          .plus(sigmaInv);
    sigmaHat = mat2.getArray();
  }

  public double eval(double[] mu) {
    double ip = 0.;
    for (int i = 0; i < size; ++ i) {
      for (int j = 0; j < size; ++ j) {
        ip += mu[i] * sigmaHat[i][j] * mu[j];
      }
    }
    return Math.exp(ip * - .5) * scale;
  }
}
