package com.nikhilpb.abtesting;

import Jama.Matrix;

import java.util.ArrayList;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class SequentialProblemStats {
  private int dim;
  private ArrayList<DataPoint> points;
  private ArrayList<ABAction> actions;
  private double normErr, approxNormErr, efficiency, perEfficiency, randEff;
  private Matrix invCovarMatrix;

  public SequentialProblemStats(final DataModel model) {
    this.dim = model.dim();
    points = new ArrayList<DataPoint>();
    actions = new ArrayList<ABAction>();
    invCovarMatrix = model.getSigma().mat().inverse();
  }

  public void addPoint(DataPoint dp, ABAction action) {
    if (dp.getDataVector().length != dim - 1) {
      throw new RuntimeException("dimension mismatch");
    }
    points.add(dp);
    actions.add(action);
  }

  public void aggregate() {
    double[][] empCovarMatrixArr = new double[dim][dim];
    double[] state = new double[dim];
    double[] mean = new double[dim];
    double valX, valY;
    for (int t = 0; t < points.size(); ++t) {
      for (int i = 0; i < dim; ++ i) {
        if (i == dim - 1) {
          valX = 1.;
        } else {
          valX = points.get(t).get(i);
        }
        for (int j = 0; j < dim; ++ j) {
          if (j == dim - 1) {
            valY = 1.;
          } else {
            valY = points.get(t).get(j);
          }
          empCovarMatrixArr[i][j] += valX * valY;
        }
      }

      for (int i = 0; i < dim; ++ i) {
        if (i < dim - 1) {
          state[i] += actions.get(t).toInt() * points.get(t).get(i);
        } else {
          state[i] += actions.get(t).toInt();
        }
      }

      for (int i = 0; i < dim; ++ i) {
        if (i < dim - 1) {
          mean[i] += points.get(t).get(i);
        } else {
          mean[i] += 1.;
        }
      }
    }


    for (int i = 0; i < dim; ++ i) {
      for (int j = 0; j < dim; ++ j) {
        empCovarMatrixArr[i][j] = empCovarMatrixArr[i][j] / points.size();
      }
      mean[i] = mean[i] / points.size();
    }

    Matrix empCovarMatrixInv = (new Matrix(empCovarMatrixArr)).inverse();

    normErr = 0.;
    for (int i = 0; i < dim; ++ i) {
      for (int j = 0; j < dim; ++ j) {
        normErr += state[i] * state[j] * empCovarMatrixInv.get(i, j);
      }
    }

    double ip = 0.;
    for (int i = 0; i < dim; ++ i) {
      for (int j = 0; j < dim; ++ j) {
        ip += mean[i] * mean[j] * empCovarMatrixInv.get(i, j);
      }
    }

    randEff = (1 - ((dim - ip) / (points.size() - 1)));
    efficiency = points.size() - normErr / points.size();
    perEfficiency = efficiency / points.size();
  }

  public double getNormErr() {
    return normErr;
  }

  public double getPerEfficiency() {
    return perEfficiency;
  }

  public double getEfficiency() {
    return efficiency;
  }

  public double getApproxNormErr() {
    return approxNormErr;
  }

  public double getRandEf() {
    return randEff;
  }
}
