package com.nikhilpb.doe;

import com.nikhilpb.util.math.PSDMatrix;

public class MyopicPolicy implements Policy {
  private PSDMatrix sigmaInv;

  public MyopicPolicy(PSDMatrix sigmaMatrix) {
    sigmaInv = sigmaMatrix.inverse();
  }

  @Override
  public int getAction(double[] state, int diff, DataPoint nextPoint) {
    int action = 1;
    double addValue = 0., subValue = 0.;
    int dim = state.length;
    double[] addVector = new double[dim], subVector = new double[dim];
    double[] dataPointVec = nextPoint.getDataVector();
    for (int i = 0; i < dim; ++ i) {
      addVector[i] = state[i] + dataPointVec[i];
      subVector[i] = state[i] - dataPointVec[i];
    }

    for (int i = 0; i < dim; ++ i) {
      for (int j = 0; j < dim; ++ j) {
        addValue += addVector[i] * addVector[j] * sigmaInv.get(i, j);
        subValue += subVector[i] * subVector[j] * sigmaInv.get(i, j);
      }
    }
    addValue += (diff + 1) * (diff + 1);
    subValue += (diff - 1) * (diff - 1);
    if (subValue < addValue) {
      action = - 1;
    } else {
      action = 1;
    }
    return action;
  }
}