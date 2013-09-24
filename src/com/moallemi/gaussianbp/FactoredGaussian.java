package com.moallemi.gaussianbp;

import Jama.*;

import com.moallemi.math.graph.*;

public class FactoredGaussian {
    protected int size;
    protected Gaussian1D[] singlePotenialMap;
    protected Gaussian2D[][] pairwisePotenialMap;
    
    public FactoredGaussian(int size) {
        this.size = size;
        singlePotenialMap = new Gaussian1D [size];
        pairwisePotenialMap = new Gaussian2D [size][size];
    }

    // first variable in potential refers to i, second to j
    // thus setPairwise(i,j) != setPairwise(j,i)
    public void setPairwise(int i, int j, Gaussian2D potential) {
        if (i == j || pairwisePotenialMap[i][j] != null)
            throw new IllegalArgumentException("cannot set potential twice");
        
        pairwisePotenialMap[i][j] = potential;
        Gaussian2D reversed = new Gaussian2D(potential.K11,
                                             potential.K01,
                                             potential.K00,
                                             potential.h1,
                                             potential.h0);
        pairwisePotenialMap[j][i] = reversed;
    }

    public void setSingle(int i, Gaussian1D potential) {
        if (singlePotenialMap[i] != null)
            throw new IllegalArgumentException("cannot set potential twice");
        
        singlePotenialMap[i]= potential;
    }
 
    public Gaussian1D getSingle(int i) { return singlePotenialMap[i]; }
    public Gaussian2D getPairwise(int i, int j) { 
        return pairwisePotenialMap[i][j]; 
    }

    public Matrix getJ() {
        Matrix J = new Matrix(size, size);
        
        for (int i = 0; i < size; i++)
            J.set(i, i, singlePotenialMap[i].K);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < i; j++) {
                Gaussian2D potential = pairwisePotenialMap[i][j];
                if (potential != null) {
                    J.set(i, i, J.get(i,i) + potential.K00);
                    J.set(i, j, potential.K01);
                    J.set(j, i, potential.K01);
                    J.set(j, j, J.get(j,j) + potential.K11);
                }
            }
        }

        return J;
    }

    public Matrix getH() {
        Matrix h = new Matrix(size, 1);
        for (int i = 0; i < size; i++)
            h.set(i, 0, singlePotenialMap[i].h);
        return h;
    }

    public double[] getMeans() {
        double[] means = new double [size];
        Matrix J = getJ();
        Matrix h = getH();
        Matrix result = J.solve(h);
        for (int i = 0; i < size; i++)
            means[i] = result.get(i, 0);
        return means;
    }

    public boolean isValid(Graph graph) {
        // check support
        for (int i = 0; i < size; i++) {
            Node node_i = graph.getNode(i);
            for (int j = 0; j < i; j++) {
                Node node_j = graph.getNode(j);
                boolean containsEdge = graph.areConnected(node_i, node_j);
                Gaussian2D potential = pairwisePotenialMap[i][j];
                if (potential == null && containsEdge)
                    return false;
                if (potential != null && !containsEdge)
                    return false;
            }
        }

        Matrix J = getJ();

        // check symmetry
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < i; j++) {
                if (J.get(i,j) != J.get(j,i))
                    return false;
            }
        }

        // check positive definiteness, Sylvester's criterion
        for (int i = 0; i < size; i++) {
            Matrix sub = J.getMatrix(0, i, 0, i);
            if (sub.det() <= 0.0)
                return false;
        }

        return true;
    }


}