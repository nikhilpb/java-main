package com.nikhilpb.abtesting;

import Jama.Matrix;
import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class DataPointStats {
  private int dim;
  private double[][] sigmaArray;
  private double[] muArray;
  private int count;

  public DataPointStats(int dim) {
    this.dim = dim;
    this.sigmaArray = new double[dim][dim];
    this.muArray = new double[dim];
    this.count = 0;
  }

  public void add(DataPoint dp) {
    for (int i = 0; i < dim; ++i) {
      muArray[i] += dp.get(i);
      for (int j = 0; j < dim; ++j) {
        sigmaArray[i][j] += dp.get(i) * dp.get(j);
      }
    }
    count++;
  }

  public PSDMatrix getSigma() {
    double[][] sgmTemp = new double[dim][dim];
    for (int i = 0; i < dim; ++i) {
      for (int j = 0; j < dim; ++j) {
        sgmTemp[i][j]
                = (sigmaArray[i][j] / count)
                   - (muArray[i] / count) *
                     (muArray[j] / count);
      }
    }
    return new PSDMatrix(sgmTemp);
  }
}
