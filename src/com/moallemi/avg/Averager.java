package com.moallemi.avg;

import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;

public abstract class Averager {
    protected Graph graph;
    protected double[] yValues;
    protected double yBar;

    public Averager(Graph graph, double[] yValues) {
        this.graph = graph;
        this.yValues = new double [yValues.length];
        System.arraycopy(yValues, 0, this.yValues, 0, yValues.length);

        if (yValues.length != graph.getNodeCount())
            throw new IllegalArgumentException("bad dimension");

        MVSampleStatistics stat = new MVSampleStatistics();
        for (int i = 0; i < yValues.length; i++)
            stat.addSample(yValues[i]);
        yBar = stat.getMean();
    }

    public abstract void iterate();

    public abstract double getEstimate(int i);

    public abstract void reset();

    public double getMean() { return yBar; }

    public double getConvergenceError() {
        SampleStatistics stat = new SampleStatistics();
        for (int i = 0; i < yValues.length; i++)
            stat.addSample(getEstimate(i));
        return stat.getRMSError();
    }

    public double getTrueError() {
        SampleStatistics stat = new SampleStatistics();
        for (int i = 0; i < yValues.length; i++)
            stat.addSample(getEstimate(i));
        return stat.getRMSError(yBar);
    }
}