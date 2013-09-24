package com.moallemi.gaussianbp;

import java.io.*;
import java.text.DecimalFormat;

import com.moallemi.math.graph.*;

public class GaussianBP {
    protected Graph graph;
    protected FactoredGaussian dist;
    protected Gaussian1D[][] messages;
    protected Gaussian1D[][] prevMessages;
    protected double[] means;

    public GaussianBP(Graph graph, FactoredGaussian dist) {
        this.graph = graph;
        this.dist = dist;

        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();

        messages = new Gaussian1D [nodeCount][nodeCount];
        prevMessages = new Gaussian1D [nodeCount][nodeCount];
        for (int e = 0; e < edgeCount; e++) {
            Edge edge = graph.getEdge(e);
            int i = graph.getNodeIndex(edge.getFirst());
            int j = graph.getNodeIndex(edge.getSecond());

            messages[i][j] = new Gaussian1D();
            messages[j][i] = new Gaussian1D();
            prevMessages[i][j] = new Gaussian1D();
            prevMessages[j][i] = new Gaussian1D();
        }

        means = dist.getMeans();
    }

    public void reset() {
        int nodeCount = graph.getNodeCount();
        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < i; j++) {
                if (messages[i][j] != null) {
                    messages[i][j].reset();
                    messages[j][i].reset();
                }
            }
        }
    }

    public void iterate() {
        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();

        // swap current and previous messages
        Gaussian1D[][] tmp = prevMessages;
        prevMessages = messages;
        messages = tmp;

        for (int e = 0; e < edgeCount; e++) {
            Edge edge = graph.getEdge(e);
            int i = graph.getNodeIndex(edge.getFirst());
            int j = graph.getNodeIndex(edge.getSecond());
            updateMessage(i, j);
            updateMessage(j, i);
        }
    }

    protected void updateMessage(int i, int j) {
        Gaussian1D psi_i = dist.getSingle(i);
        Gaussian2D psi_ij = dist.getPairwise(i, j);
        double J_ii = psi_i.K + psi_ij.K00;
        double J_ij = psi_ij.K01;
        double J_jj = psi_ij.K11;
        double h_i = psi_i.h + psi_ij.h0;
        double h_j = psi_ij.h1;

        Node node_i = graph.getNode(i);
        Node node_j = graph.getNode(j);
        int nodeDegree = graph.getNodeDegree(node_i);
        for (int n = 0; n < nodeDegree; n++) {
            Node node_u = graph.getConnectedNode(node_i, n);
            if (node_u == node_j)
                continue;
            int u = graph.getNodeIndex(node_u);

            J_ii += prevMessages[u][i].K;
            h_i += prevMessages[u][i].h;
        }

        messages[i][j].K = J_jj - J_ij * J_ij / J_ii;
        messages[i][j].h = h_j - J_ij * h_i / J_ii;

    }

    public Gaussian1D getBelief(int i) {
        Gaussian1D psi_i = dist.getSingle(i);
        Gaussian1D belief = new Gaussian1D();
        belief.K = psi_i.K;
        belief.h = psi_i.h;

        Node node_i = graph.getNode(i);
        int nodeDegree = graph.getNodeDegree(node_i);
        for (int n = 0; n < nodeDegree; n++) {
            Node node_u = graph.getConnectedNode(node_i, n);
            int u = graph.getNodeIndex(node_u);
            belief.K += prevMessages[u][i].K;
            belief.h += prevMessages[u][i].h;
        }

        return belief;
    }
 

    public void dumpInfo(PrintStream out) throws IOException {
        DecimalFormat df = new DecimalFormat("0.0000");
        int nodeCount = graph.getNodeCount();
        for (int i = 0; i < nodeCount; i++) {
            Gaussian1D belief = getBelief(i);
            double error = Math.abs(belief.getMean() - means[i]);
            out.print(" " + df.format(error));
        }
        out.println();
    }
}