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

    public SequentialProblemStats(int dim) {
        this.dim = dim;
        points = new ArrayList<DataPoint>();
        actions = new ArrayList<Integer>();
    }

    public void addPoint(DataPoint dp, int action) {
        if (dp.getDataVector().length != dim) {
            throw new RuntimeException("dimension mismatch");
        }
        points.add(dp);
        actions.add(action);
    }

    public void printStats() {
        double[][] empCovarMatrixArr = new double[dim][dim];
        double[] state = new double[dim];
        for (int t = 0; t < points.size(); ++t) {
            for (int i = 0; i < dim; ++i) {
                for (int j = 0; j < dim; ++j) {
                    empCovarMatrixArr[i][j] += points.get(t).get(i) * points.get(t).get(j);
                }
            }

            for (int i = 0; i < dim; ++i) {
                state[i] += actions.get(i) * points.get(t).get(i);
            }
        }
        Matrix empCovarMatrix = new Matrix(empCovarMatrixArr);
        Matrix empCovarMatrixInv = empCovarMatrix.inverse();

        double normErr = 0.;
        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j < dim; ++j) {
                normErr += state[i] * state[j] * empCovarMatrixInv.get(i, j);
            }
        }

        double efficiency = normErr / points.size();

        System.out.println("norm error: " + normErr
                + ", efficiency: " + efficiency
                + ", % efficiency: " + efficiency / points.size());
    }
}
