package com.nikhilpb.util.math;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import java.io.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/14/13
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSDMatrix {
    private Matrix mat, sqrtMat;
    private static final double kTol = 1E-4;

    public PSDMatrix(Matrix mat) {
        init(mat);
    }

    public PSDMatrix(final String fileName) throws IOException {
        Properties props = new Properties();
        FileInputStream fin = new FileInputStream(new File(fileName));
        props.load(fin);
        int n = Integer.parseInt(props.getProperty("size"));
        double[][] matArray = new double[n][n];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j <= i; ++j) {
                matArray[i][j] = Double.parseDouble(props.getProperty(j + "," + i));
                matArray[j][i] = matArray[i][j];
            }
        }
        Matrix matrix = new Matrix(matArray);
        init(matrix);
        fin.close();
    }

    private boolean isSymmetric() {
        if (mat.getRowDimension() != mat.getColumnDimension())
            return false;
        for (int i = 0; i < mat.getRowDimension(); ++i) {
            for (int j = 0; j < i; ++j) {
                if (Math.abs(mat.get(i, j) - mat.get(j, i)) > kTol) {
                    return false;
                }
                mat.set(j, i, mat.get(i, j));
            }
        }
        return true;
    }

    public Matrix sqrt() {
        return sqrtMat;
    }

    public Matrix mat() {
        return mat;
    }

    public void writeToFile(final String fileName) throws IOException {
        Properties props = new Properties();
        props.setProperty("size", Integer.toString(mat.getRowDimension()));
        for (int i = 0; i < mat.getRowDimension(); ++i) {
            for (int j = 0; j <= i; ++j) {
                props.setProperty(j + "," + i, Double.toString(mat.get(i, j)));
            }
        }
        FileOutputStream fout = new FileOutputStream(new File(fileName));
        props.store(fout, "");
        fout.close();
    }

    public boolean approxEqual(Matrix otherMat, double tol) {
        if (otherMat.minus(mat).normF() < tol)
            return true;
        return false;
    }

    public boolean approxEqual(PSDMatrix otherMat, double tol) {
        return approxEqual(otherMat.mat(), tol);
    }

    private void init(Matrix mat) {
        this.mat = mat;
        int n = mat.getColumnDimension();
        if (n < 1) {
            throw new IllegalArgumentException("matrix must be non-empty");
        }
        if (!isSymmetric())
            throw new IllegalArgumentException("matrix is not symmetric");
        EigenvalueDecomposition eig = mat.eig();
        Matrix dMat = eig.getD(), vMat = eig.getV();
        for (int i = 0; i < n; ++i) {
            if (dMat.get(i, i) < 0.)
                throw new IllegalArgumentException("matrix is not PSD");
            dMat.set(i, i, Math.sqrt(dMat.get(i, i)));
        }
        sqrtMat = vMat.times(dMat);
    }
}
