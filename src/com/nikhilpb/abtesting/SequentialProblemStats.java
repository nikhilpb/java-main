package com.nikhilpb.abtesting;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class SequentialProblemStats {
  private int dim;
  private ArrayList<DataPoint> points;
  private ArrayList<ABAction> actions;
  private double normErr, approxNormErr, efficiency, perEfficiency, randEff;
  private static final double kTol = 1E-3;

  public SequentialProblemStats(final DataModel model) {
    this.dim = model.dim();
    points = new ArrayList<DataPoint>();
    actions = new ArrayList<ABAction>();
  }

  public void addPoint(DataPoint dp, ABAction action) {
    if (dp.getDataVector().length != dim - 1) {
      throw new RuntimeException("dimension mismatch");
    }
    points.add(dp);
    actions.add(action);
  }

  public void excludeRedundantCovariates() {
    double[][] sigma = new double[dim - 1][dim - 1];
    for (DataPoint dp : points) {
      for (int i = 0; i < dim - 1; ++i) {
        for (int j = 0; j < dim - 1; ++j) {
          sigma[i][j] += dp.get(i) * dp.get(j);
        }
      }
    }
    HashSet<Integer> excludeSet = new HashSet<Integer>();
    for (int i = 0; i < dim - 1; ++i) {
      for (int j = 0; j < dim - 1; ++j) {
        System.out.printf("%.3f ", sigma[i][j]);
      }
      System.out.println();
      if (sigma[i][i] < kTol) {
        System.out.println("Added 1: " + i);
        excludeSet.add(i);
        continue;
      }
      for (int j = 0; j < i; ++j) {
        if (Math.abs(sigma[i][j] - sigma[i][i]) + Math.abs(sigma[i][j] - sigma[j][j]) < kTol) {
          System.out.println("Added 2: " + i + "," + j);
          excludeSet.add(i);
        }
      }
    }

    final int newDim = dim - 1 - excludeSet.size();
    ArrayList<DataPoint> newPoints = new ArrayList<DataPoint>();
    for (DataPoint dp : points) {
      double[] newVector = new double[newDim], oldVector = dp.getDataVector();
      int cur = 0;
      for (int i = 0; i < oldVector.length; ++i) {
        if (!excludeSet.contains(i)) {
          newVector[cur] = oldVector[i];
        }
      }
      newPoints.add(new DataPoint(newVector));
    }
    points = newPoints;
    dim = newDim + 1;
  }

  public boolean aggregate() {
    // excludeRedundantCovariates();
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
    Matrix empCovarMat = (new Matrix(empCovarMatrixArr));
    Matrix empCovarMatrixInv;
    try {
      empCovarMatrixInv = empCovarMat.inverse();
    } catch (RuntimeException r) {
      return false;
    }
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
    return true;
  }

  public void aggregate2() {
    double[][] xArr = new double[points.size()][dim + 1];
    for (int i = 0; i < points.size(); ++i) {
      xArr[i][0] = actions.get(i).toInt();
      xArr[i][1] = 1.;
      for (int j = 0; j < dim - 1; ++j) {
        xArr[i][j+2] = points.get(i).get(j);
      }
    }
    Matrix xMat = new Matrix(xArr);
    xMat.transpose().times(xMat).print(xMat.getColumnDimension(), xMat.getColumnDimension());
    SingularValueDecomposition svd = xMat.svd();
    Matrix sMat = svd.getS();
    sMat.print(sMat.getRowDimension(), sMat.getColumnDimension());
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
