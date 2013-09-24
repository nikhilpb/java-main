package com.moallemi.iqswitch;

import java.util.Arrays;

public class MatchingMatrix {
    private double[][] weights;
    private double offset;
     
    public MatchingMatrix(SwitchModel model) {
        int switchSize = model.getSwitchSize();
        weights = new double [switchSize][switchSize];
    }

    public void reset() {
        for (int src = 0; src < weights.length; src++)
            Arrays.fill(weights[src], 0.0);
        offset = 0.0;
    }

    public void addWeight(int src, int dest, double w) {
        weights[src][dest] += w;
    }
    
    public void addOffset(double w) {
        offset += w;
    }

    public double[][] getWeights() {
        return weights;
    }

    public double getWeight(int src, int dest) {
        return weights[src][dest];
    }

    public double getOffset() {
        return offset;
    }
}