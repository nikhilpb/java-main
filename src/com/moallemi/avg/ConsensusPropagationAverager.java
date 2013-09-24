package com.moallemi.avg;

import java.io.PrintStream;
import java.util.*;

import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.data.Pair;

public class ConsensusPropagationAverager extends Averager {
    protected double[][] kValues;
    protected double[][] muValues;    
    protected double beta;
    protected double[] xValues;

    protected double[][] inK, inMu, inNewK, inNewMu;


    public ConsensusPropagationAverager(Graph graph, 
                                        double[] yValues,
                                        double beta) 
    {
        super(graph, yValues);
        this.beta = beta;
        int nodeCount = yValues.length;

        xValues = new double [nodeCount];
        System.arraycopy(yValues, 0, xValues, 0, yValues.length);

        inK = new double [nodeCount][];
        inMu = new double [nodeCount][];
        inNewK = new double [nodeCount][];
        inNewMu = new double [nodeCount][];
        for (int i = 0; i < nodeCount; i++) {
            int nodeDegree = graph.getNodeDegree(i);
            inK[i] = new double [nodeDegree];
            inMu[i] = new double [nodeDegree];
            inNewK[i] = new double [nodeDegree];
            inNewMu[i] = new double [nodeDegree];
        }
    }

    public void reset() {
        int nodeCount = yValues.length;
        System.arraycopy(yValues, 0, xValues, 0, nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            Arrays.fill(inK[i], 0.0);
            Arrays.fill(inMu[i], 0.0);
            Arrays.fill(inNewK[i], 0.0);
            Arrays.fill(inNewMu[i], 0.0);
        }
    }


    public void iterate() {
        int nodeCount = yValues.length;

        // compute new messages
        for (int i = 0; i < nodeCount; i++) {
            Node source = graph.getNode(i);
            int sourceDegree = inK[i].length;

            double sumK = 0.0;
            double sumMu = 0.0;
            for (int c = 0; c < sourceDegree; c++) {
                sumK += inK[i][c];
                sumMu += inK[i][c] * inMu[i][c];
            }

            for (int c = 0; c < sourceDegree; c++) {
                double norm = 1.0 + sumK - inK[i][c];
		double outNewK = beta < Double.MAX_VALUE
		    ? norm/(norm/beta + 1.0)
		    : norm;
                double outNewMu = 
                    (yValues[i] + (sumMu - inK[i][c]*inMu[i][c]))
                    / norm;
                Node dest = graph.getConnectedNode(source, c);
                int destIndex = graph.getNodeIndex(dest);
                int sourceOffset = graph.getConnectedNodeOffset(dest, source);
                inNewK[destIndex][sourceOffset] = outNewK;
                inNewMu[destIndex][sourceOffset] = outNewMu;
            }
        }

        // swap
        double[][] tmp;
        tmp = inK; inK = inNewK; inNewK = tmp;
        tmp = inMu; inMu = inNewMu; inNewMu = tmp;

        // update estimates
        for (int i = 0; i < nodeCount; i++) {
            Node source = graph.getNode(i);
            int sourceDegree = inK[i].length;

            double sumK = 0.0;
            double sumMu = 0.0;
            for (int c = 0; c < sourceDegree; c++) {
                sumK += inK[i][c];
                sumMu += inK[i][c] * inMu[i][c];
            }

            xValues[i] = (yValues[i] + sumMu) / (1.0 + sumK);
        }
    }

    public double getKMean() {
	MVSampleStatistics stat = new MVSampleStatistics();
        for (int i = 0; i < inK.length; i++) 
            for (int c = 0; c < inK[i].length; c++)
                stat.addSample(inK[i][c]);
	return stat.getMean();
    }

    public double getKStdDev() {
	SampleStatistics stat = new SampleStatistics();
        for (int i = 0; i < inK.length; i++) 
            for (int c = 0; c < inK[i].length; c++)
                stat.addSample(inK[i][c]);
	return stat.getStandardDeviation();
    }

    public double getMuMean() {
	MVSampleStatistics stat = new MVSampleStatistics();
        for (int i = 0; i < inMu.length; i++) 
            for (int c = 0; c < inMu[i].length; c++)
                stat.addSample(inMu[i][c]);
	return stat.getMean();
    }

    public double getMuStdDev() {
	SampleStatistics stat = new SampleStatistics();
        for (int i = 0; i < inMu.length; i++) 
            for (int c = 0; c < inMu[i].length; c++)
                stat.addSample(inMu[i][c]);
	return stat.getStandardDeviation();
    }


    public double getEstimate(int i) { return xValues[i]; }

    public void dumpInfo(PrintStream out) {
        for (int i = 0; i < inK.length; i++) {
            Node node = graph.getNode(i);
            for (int c = 0; c < inK[i].length; c++) {
                int j = graph.getNodeIndex(graph.getConnectedNode(node, c));
                out.println("(" + (i+1)
                            +"," + (j+1)
                            +") " + inK[i][c] + " " + inMu[i][c]);
            }
        }
	for (int i = 0; i < yValues.length; i++) 
	    out.println((i+1) + ": " + yValues[i] + " " + getEstimate(i));
        out.println("objective: " + getObjective());
    }

    public double getObjective() {
        double v = 0.0;
        for (int i = 0; i < yValues.length; i++) {
            double x = yValues[i] - xValues[i];
            v += 0.5*x*x;
        }
        for (int i = 0; i < yValues.length; i++) {
            Node source = graph.getNode(i);
            int nodeDegree = graph.getNodeDegree(i);
            for (int c = 0; c < nodeDegree; c++) {
                int j = graph.getNodeIndex(graph.getConnectedNode(source, c));
                if (i < j) {
                    double x = xValues[i] - xValues[j];
                    v += 0.5*x*x;
                }
            }
        }
        return v;
    }
}
