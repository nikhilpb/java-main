package com.moallemi.minsum;

import java.util.Arrays;

public class FactorGraph {

    protected int varCount;
    protected int factorCount;
    private int[][] varNeighborMap;
    private int[][] factorNeighborMap;
    private int[][] varNeighborOffsetMap;
    private int[][] factorNeighborOffsetMap;
    private int[] varDegree;
    private int[] factorDegree;


    public FactorGraph(int varCount, int factorCount) {
        this.varCount = varCount;
        this.factorCount = factorCount;

        varDegree = new int [varCount];
        Arrays.fill(varDegree, 0);
        factorDegree = new int [factorCount];
        Arrays.fill(factorDegree, 0);

        varNeighborMap = new int [varCount][0];
        varNeighborOffsetMap = new int [varCount][0];
        factorNeighborMap = new int [factorCount][0];
        factorNeighborOffsetMap = new int [factorCount][0];
    }


    public void reset() {
        Arrays.fill(varDegree, 0);
        Arrays.fill(factorDegree, 0);
        for (int v = 0; v < varCount; v++) {
            Arrays.fill(varNeighborMap[v], -1);
            Arrays.fill(varNeighborOffsetMap[v], -1);
        }
        for (int f = 0; f < factorCount; f++) {
            Arrays.fill(factorNeighborMap[f], -1);
            Arrays.fill(factorNeighborOffsetMap[f], -1);
        }
    }

    public boolean isConnected(int v, int f) {
        for (int fIndex = 0; fIndex < varDegree[v]; fIndex++) {
            if (varNeighborMap[v][fIndex] == f)
                return true;
        }
        return false;
    }

    public boolean addEdge(int v, int f) {
        // make sure the edge doesn't already exist
        if (isConnected(v, f))
            return false;

        // create space for the new entry
        int vSize = varDegree[v] + 1;
        if (varNeighborMap[v].length < vSize) {
            int[] tmp = new int [2*vSize];
            System.arraycopy(varNeighborMap[v],
                             0, 
                             tmp,
                             0,
                             varDegree[v]);
            varNeighborMap[v] = tmp;
            tmp = new int [varNeighborMap[v].length];
            System.arraycopy(varNeighborOffsetMap[v],
                             0,
                             tmp,
                             0,
                             varDegree[v]);
            varNeighborOffsetMap[v] = tmp;
        }

        int fSize = factorDegree[f] + 1;
        if (factorNeighborMap[f].length < fSize) {
            int[] tmp = new int [2*fSize];
            System.arraycopy(factorNeighborMap[f],
                             0,
                             tmp,
                             0,
                             factorDegree[f]);
            factorNeighborMap[f] = tmp;
            tmp = new int [factorNeighborMap[f].length];
            System.arraycopy(factorNeighborOffsetMap[f], 
                             0,
                             tmp,
                             0,
                             factorDegree[f]);
            factorNeighborOffsetMap[f] = tmp;
        }

        // add the new entry
        varNeighborMap[v][varDegree[v]] = f;
        varNeighborOffsetMap[v][varDegree[v]] = factorDegree[f];
        factorNeighborMap[f][factorDegree[f]] = v;
        factorNeighborOffsetMap[f][factorDegree[f]] = varDegree[v];
        varDegree[v]++;
        factorDegree[f]++;

        return true;
    }

    public int getVariableCount() { return varDegree.length; }
    public int getFactorCount() { return factorDegree.length; }

    public int getVariableDegree(int v) { return varDegree[v]; }
    public int getFactorDegree(int f) { return factorDegree[f]; }

    public int getVariableNeighbor(int v, int fIndex) {
        return varNeighborMap[v][fIndex];
    }

    public int getVariableNeighborOffset(int v, int fIndex) {
        return varNeighborOffsetMap[v][fIndex];
    }

    public int getFactorNeighbor(int f, int vIndex) {
        return factorNeighborMap[f][vIndex];
    }

    public int getFactorNeighborOffset(int f, int vIndex) {
        return factorNeighborOffsetMap[f][vIndex];
    }

    public void compress() {
        for (int v = 0; v < varNeighborMap.length; v++) {
            if (varNeighborMap[v].length != varDegree[v]) {
                int[] tmp = new int [varDegree[v]];
                System.arraycopy(varNeighborMap[v],
                                 0,
                                 tmp,
                                 0,
                                 varDegree[v]);
                varNeighborMap[v] = tmp;
                tmp = new int [varDegree[v]];
                System.arraycopy(varNeighborOffsetMap[v], 
                                 0,
                                 tmp, 
                                 0,
                                 varDegree[v]);
                varNeighborOffsetMap[v] = tmp;
            }
        }

        for (int f = 0; f < factorNeighborMap.length; f++) {
            if (factorNeighborMap[f].length != factorDegree[f]) {
                int[] tmp = new int [factorDegree[f]];
                System.arraycopy(factorNeighborMap[f],
                                 0,
                                 tmp,
                                 0,
                                 factorDegree[f]);
                factorNeighborMap[f] = tmp;
                tmp = new int [factorDegree[f]];
                System.arraycopy(factorNeighborOffsetMap[f], 
                                 0,
                                 tmp, 
                                 0,
                                 factorDegree[f]);
                factorNeighborOffsetMap[f] = tmp;

            }
        }
    }
}