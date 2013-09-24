package com.moallemi.avg;

import com.moallemi.math.graph.*;

public class PairwiseAverager extends Averager {
    private double[] xValues;
    private double[] xNewValues;

    public PairwiseAverager(Graph graph, double[] yValues) {
        super(graph, yValues);
        xValues = new double [yValues.length];
        xNewValues = new double [yValues.length];
        reset();
    }

    public void iterate() {
        int nodeCount = graph.getNodeCount();
        for (int i = 0; i < nodeCount; i++) {
            double sum = 0.0;
            Node node = graph.getNode(i);
            int nodeDegree = graph.getNodeDegree(node);
            double f = 1.0 / (1.0 + nodeDegree);
            for (int c = 0; c < nodeDegree; c++) {
                Node other = graph.getConnectedNode(node, c);
                int otherDegree = graph.getNodeDegree(other);
                if (otherDegree != nodeDegree)
                    throw new 
                        RuntimeException("cannot handle irregular graphs");
                int otherIndex = graph.getNodeIndex(other);
                sum += f * xValues[otherIndex];
            }
            sum += f * xValues[i];
            xNewValues[i] = sum;
        }

        double[] tmp = xValues;
        xValues = xNewValues;
        xNewValues = tmp;
    }

    public double getEstimate(int i) { return xValues[i]; }

    public void reset() {
        System.arraycopy(yValues, 0, xValues, 0, yValues.length);
    }

}