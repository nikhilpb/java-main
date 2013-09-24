package com.moallemi.avg;

import java.io.PrintStream;
import java.util.*;

import no.uib.cipr.matrix.*;

import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.data.Pair;
import com.moallemi.minsum.*;

public class AttenuatedCPAverager extends Averager {
    protected double[][] kValues;
    protected double[][] muValues;    
    protected double beta;
    protected double[] xValues;

    protected double[][] inK, inMu, inNewK, inNewMu;

    protected double alpha;
    protected double[][] w;
    protected double[][] omega;
    protected double[] bigOmega;
    protected int directedEdgeCount;
    protected DirectedEdgeSet des;
    protected DenseMatrix xsysA, xsysAn;
    protected DenseVector xsysB, xsysBn, xsysSol;

    public AttenuatedCPAverager(Graph graph, 
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

        initializeGraph();
    }

    private void initializeGraph() {
        int nodeCount = yValues.length;
        int edgeCount = graph.getEdgeCount();
        des = new DirectedEdgeSet(graph);

        directedEdgeCount = des.getDirectedEdgeCount();
        DenseMatrix A = new DenseMatrix(directedEdgeCount, directedEdgeCount);
        for (int ij = 0; ij < directedEdgeCount; ij++) {
            int degree = des.getConnectedEdgeDegree(ij);
            for (int offset = 0; offset < degree; offset++) {
                int ui = des.getConnectedEdgeIndex(ij, offset);
                A.set(ij, ui, 1.0);
            }
        }


        EVD evd = new EVD(directedEdgeCount, true, false);
        try {
            evd.factor(A);
        }
        catch (NotConvergedException e) {
            throw new RuntimeException("unable to computed evd", e);
        }
        double[] eigRe = evd.getRealEigenvalues();
        alpha = 0.0;
        int maxIndex = -1;
        for (int ij = 0; ij < directedEdgeCount; ij++) {
            if (eigRe[ij] > alpha) {
                alpha = eigRe[ij];
                maxIndex = ij;
            }
        }
        if (maxIndex == -1) {
            throw new IllegalStateException("could not get spectral radius");
        }
        DenseMatrix V = evd.getLeftEigenvectors();
        w = new double [nodeCount][];
        double sumW = 0.0;
        for (int i = 0; i < nodeCount; i++) {
            int nodeDegree = graph.getNodeDegree(i);
            w[i] = new double [nodeDegree];
            for (int offset = 0; offset < nodeDegree; offset++) {
                double x = V.get(des.getOutgoingEdgeIndex(i, offset),
                                 maxIndex);
                sumW += x;
                w[i][offset] = x;
            }
        }
        for (int i = 0; i < nodeCount; i++) {
            int nodeDegree = w[i].length;
            for (int offset = 0; offset < nodeDegree; offset++) {
                w[i][offset] /= sumW;
                if (w[i][offset] < 0.0) {
                    throw new IllegalStateException("eigenvector not "
                                                    + "non-negative");
                }
            }
        }
        omega = new double [nodeCount][];
        bigOmega = new double [nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            int nodeDegree = w[i].length;
            Node node = graph.getNode(i);
            omega[i] = new double [nodeDegree];
            for (int offset = 0; offset < nodeDegree; offset++) {
                Node other = graph.getConnectedNode(node, offset);
                int j = graph.getNodeIndex(other);
                int jOffset = graph.getConnectedNodeOffset(other, node);
                omega[i][offset] = w[i][offset] + w[j][jOffset];
                if (omega[i][offset] <= 0.0) 
                    throw new IllegalStateException("omega not positive");
                bigOmega[i] += w[i][offset];
            }
            if (bigOmega[i] <= 0.0) 
                throw new IllegalStateException("bigOmega not positive");
        }
        

        xsysA = new DenseMatrix(directedEdgeCount, nodeCount);
        xsysAn = new DenseMatrix(nodeCount, nodeCount);
        xsysB = new DenseVector(directedEdgeCount);
        xsysBn = new DenseVector(nodeCount);
        xsysSol = new DenseVector(nodeCount);
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
                double norm = 1.0/bigOmega[i] + (sumK - inK[i][c])/alpha;
		double outNewK = beta < Double.MAX_VALUE
		    ? norm/(norm*omega[i][c]/beta + 1.0)
		    : norm;
                double outNewMu = 
                    (yValues[i]/bigOmega[i] + (sumMu 
                                               - inK[i][c]*inMu[i][c])/alpha)
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
//                  sumK += omega[i][c] * inK[i][c];
//                  sumMu += omega[i][c] * inK[i][c] * inMu[i][c];
                  sumK += inK[i][c];
                  sumMu += inK[i][c] * inMu[i][c];
             }

             xValues[i] = (yValues[i] + sumMu) 
                 / (1.0 + sumK);
        }

        xsysA.zero();
        xsysB.zero();
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
                int ij = des.getOutgoingEdgeIndex(i, c);
                Node dest = des.getDirectedEdge(ij).getSecond();
                int j = graph.getNodeIndex(dest);
                double norm = 1.0/bigOmega[i] 
                    + beta/omega[i][c] 
                    + (sumK - inK[i][c])/alpha;
                xsysA.set(ij, i, norm);
                xsysA.set(ij, j, -beta*omega[i][c]);
                xsysB.set(ij, 
                          yValues[i]/bigOmega[i] 
                          + (sumMu - (inK[i][c] * inMu[i][c]))/alpha);
            }
        }
        xsysA.transAmult(xsysA, xsysAn);
        xsysA.transMult(xsysB, xsysBn);
        xsysAn.solve(xsysBn, xsysSol);
//         for (int i = 0; i < nodeCount; i++)
//             xValues[i] = xsysSol.get(i);

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
                            +") " + inK[i][c] 
                            + " " + inMu[i][c]
                            + " " + omega[i][c]
                            );
            }
        }
	for (int i = 0; i < yValues.length; i++) 
	    out.println((i+1) + ": " 
                        + yValues[i] 
                        + " " + getEstimate(i)
                        + " " + bigOmega[i]
                        );
        out.println("objective: " + getObjective());
        //out.println(xsysA.toString());
        //out.println(xsysB.toString());
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
