package com.nikhilpb.abtesting;

import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class MyopicPolicy implements ABPolicy {
  private PSDMatrix sigmaInv;

  public MyopicPolicy(PSDMatrix sigmaMatrix) {
    sigmaInv = sigmaMatrix.inverse();
  }

  @Override
  public ABAction getAction(ABState state) {
    double addValue = 0., subValue = 0.;
    int dim = state.getDelta().length;
    double[] addVector = new double[dim], subVector = new double[dim];
    for (int i = 0; i < dim; ++ i) {
      addVector[i] = state.getDelta(i) + state.getDp().get(i);
      subVector[i] = state.getDelta(i) - state.getDp().get(i);
    }

    for (int i = 0; i < dim; ++ i) {
      for (int j = 0; j < dim; ++ j) {
        addValue += addVector[i] * addVector[j] * sigmaInv.get(i, j);
        subValue += subVector[i] * subVector[j] * sigmaInv.get(i, j);
      }
    }
    final int diff = state.getDiffCount();
    addValue += (diff + 1) * (diff + 1);
    subValue += (diff - 1) * (diff - 1);
    if (subValue < addValue) {
      return ABAction.MINUS;
    } else {
      return ABAction.PLUS;
    }
  }
}
