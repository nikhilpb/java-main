package com.nikhilpb.stopping;

import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/29/13
 * Time: 12:46 PM
 *
 * A class that computes E k(X, y), where k is a Gaussian kernel, X is a mean \mu and covariance \Sigma normal rand
 * variable.
 */
public class GaussianKernelE {
  private MeanGaussianKernel meanGaussianKernel;
  private double[] mean;

  /**
   *
   * @param sigma the covariance matrix
   * @param rho the badwidth
   * @param peak the magnitude of the kernel at the center
   */
  public GaussianKernelE(double[] mean, PSDMatrix sigma, double rho, double peak) {
    meanGaussianKernel = new MeanGaussianKernel(sigma, rho, peak);
    this.mean = mean;
  }

  /**
   *
   * @param xStatePrev
   * @param yState
   * @return Returns E k(X,y)
   */
  public double eval(StoppingState xStatePrev, StoppingState yState) {
    double[] diff = new double[mean.length];
    for (int i = 0; i < diff.length; ++i) {
      diff[i] = xStatePrev.vector[i] + mean[i] - yState.vector[i];
    }
    return meanGaussianKernel.eval(diff);
  }
}
