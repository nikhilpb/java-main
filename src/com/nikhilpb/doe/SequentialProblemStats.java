package com.nikhilpb.doe;

import Jama.Matrix;

import java.util.ArrayList;

/**
 * Created by nikhilpb on 5/22/14.
 */
public class SequentialProblemStats {
    private int dim;
    private ArrayList<DataPoint> points;
    private ArrayList<Integer> actions;
    private double normErr, approxNormErr, efficiency, perEfficiency;
    private Matrix invCovarMatrix;


    public SequentialProblemStats(int dim, Matrix covarMatrix) {
        this.dim = dim;
        points = new ArrayList<DataPoint>();
        actions = new ArrayList<Integer>();
        invCovarMatrix = covarMatrix.inverse();
    }

    public void addPoint(DataPoint dp, int action) {
        if (dp.getDataVector().length != dim) {
            throw new RuntimeException("dimension mismatch");
        }
        points.add(dp);
        actions.add(action);
    }

    public void aggregate() {
        double[][] empCovarMatrixArr = new double[dim][dim];
        double[] state = new double[dim];
        for (int t = 0; t < points.size(); ++t) {
            for (int i = 0; i < dim; ++i) {
                for (int j = 0; j < dim; ++j) {
                    empCovarMatrixArr[i][j] += points.get(t).get(i) * points.get(t).get(j);
                }
            }

            for (int i = 0; i < dim; ++i) {
                state[i] += actions.get(t) * points.get(t).get(i);
            }
        }

        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j < dim; ++j) {
                empCovarMatrixArr[i][j] = empCovarMatrixArr[i][j] / points.size();
            }
        }


        Matrix empCovarMatrix = new Matrix(empCovarMatrixArr);
        Matrix empCovarMatrixInv = empCovarMatrix.inverse();

        normErr = 0.;
        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j < dim; ++j) {
                normErr += state[i] * state[j] * empCovarMatrixInv.get(i, j);
            }
        }

        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j < dim; ++j) {
                approxNormErr += state[i] * state[j] * invCovarMatrix.get(i, j);
            }
        }

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
}
