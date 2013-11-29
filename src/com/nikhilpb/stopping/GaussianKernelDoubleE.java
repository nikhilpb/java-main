package com.nikhilpb.stopping;

import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/29/13
 * Time: 1:25 PM
 * Evaluates E k(X, Y)
 */
public class GaussianKernelDoubleE {
  private MeanGaussianKernel meanGaussianKernel;

  /**
   * Class constructor.
   * @param sigma
   * @param rho
   * @param peak
   */
  public GaussianKernelDoubleE(PSDMatrix sigma, double rho, double peak) {
    meanGaussianKernel = new MeanGaussianKernel(PSDMatrix.times(sigma, 2.0), rho, peak);
  }

  /**
   * E k(X, Y)
   * @param xStatePrev
   * @param yStatePrev
   * @return
   */
  public double eval(StoppingState xStatePrev, StoppingState yStatePrev) {
    double[] diff = new double[xStatePrev.vector.length];
    for (int i = 0; i < diff.length; ++i) {
      diff[i] = xStatePrev.vector[i] - yStatePrev.vector[i];
    }
    return meanGaussianKernel.eval(diff);
  }
}
