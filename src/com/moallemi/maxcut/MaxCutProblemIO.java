package com.moallemi.maxcut;

import java.io.*;
import java.util.regex.*;
import java.text.DecimalFormat;

import com.moallemi.math.graph.*;

public class MaxCutProblemIO {
    private static final Pattern p1 
        = Pattern.compile("^(\\d+)\\s+(\\d+)\\s*$");
    private static final Pattern p2
        = Pattern.compile("^(\\d+)\\s+(\\d+)\\s+(\\S+)\\s*$");

    public static MaxCutProblem readModel(BufferedReader in) 
        throws IOException {
        String line;
        Matcher m;

        line = in.readLine();
        m = p1.matcher(line);
        if (!m.matches())
            throw new IllegalArgumentException("unable to parse: " + line);
        int nodeCount = Integer.parseInt(m.group(1));
        int edgeCount = Integer.parseInt(m.group(2));
        double[] costByEdge = new double [edgeCount];

        Graph graph = new Graph();
        graph.beginModification();

        for (int i = 0; i < nodeCount; i++)
            graph.addNode(new Node());
        while ((line = in.readLine()) != null) {
            m = p2.matcher(line);
            if (!m.matches())
                throw new IllegalArgumentException("unable to parse: " + line);
            int i = Integer.parseInt(m.group(1)) - 1;
            int j = Integer.parseInt(m.group(2)) - 1;
            double cost = Double.parseDouble(m.group(3));
            Edge edge = new Edge(graph.getNode(i), graph.getNode(j));
            graph.addEdge(edge);
            int e = graph.getEdgeIndex(edge);
            costByEdge[e] = cost;
        }
        graph.endModification();

        if (graph.getEdgeCount() != edgeCount)
            throw new IllegalArgumentException("bad edge count");

        return new MaxCutProblem(graph, costByEdge);
    }

    public static void writeModel(MaxCutProblem problem, 
                                  PrintWriter out) {
        DecimalFormat df = new DecimalFormat("0.000000");
        Graph graph = problem.getGraph();
        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();
        out.println(nodeCount + " " + edgeCount);
        for (int i = 0; i < nodeCount; i++) {
            Node iNode = graph.getNode(i);
            int degree = graph.getNodeDegree(iNode);
            for (int jIndex = 0; jIndex < degree; jIndex++) {
                Node jNode = graph.getConnectedNode(iNode, jIndex);
                int j = graph.getNodeIndex(jNode);
                if (i < j) 
                    out.println((i+1) + " " + (j+1)
                                + " " 
                                + df.format(problem.getEdgeCost(i, jIndex)));
            }
        }
    }
}