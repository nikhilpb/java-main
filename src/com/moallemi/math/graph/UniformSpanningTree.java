package com.moallemi.math.graph;

import java.util.*;

/**
 * Uniformly sample a spanning tree. Uses the Aldous/Broder algorithm.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2006-09-13 19:23:02 $
 */
public class UniformSpanningTree implements Iterator<Graph> {
    // the underlying graph
    private Graph graph;
    private Graph[] components;
    // the current tree
    private Graph treeGraph;
    // randomness source
    private Random r;

    /**
     * Constructor.
     *
     * @param graph the underlying graph
     * @param r a randomness source
     */
    public UniformSpanningTree(Graph graph, Random r) {
        this.graph = graph;
        this.r = r;
        DFS dfs = (new DFS(graph)).compute();
        components = dfs.buildConnectedComponents();
        treeGraph = new Graph();
    }

    /**
     * Is there another tree?
     *
     * @return always <code>true</code>
     */
    public boolean hasNext() { return true; }

    /**
     * Return the next tree. Always modifies the same structure in
     * place.
     *
     * @return the next sampled tree
     */
    public Graph next() {
        treeGraph.beginModification(); 
        treeGraph.removeAllNodes();
     
        for (int c = 0; c < components.length; c++) {
            int nodeCount = components[c].getNodeCount();;
            Node prev = null;
            Node current = components[c].getNode(r.nextInt(nodeCount));
            treeGraph.addNode(current);

            int visitCount = 1;
            while (visitCount < nodeCount) {
                // sample next state
                prev = current;
                int degree = graph.getNodeDegree(current);
                current = graph.getConnectedNode(current,
                                                 r.nextInt(degree));

                // add the current node if it is freshly visited
                if (!treeGraph.containsNode(current)) {
                    treeGraph.addNode(current);
                    treeGraph.addEdge(new Edge(prev, current));
                    visitCount++;
                }
            }
        }
        
        treeGraph.endModification();
        return treeGraph;
    }


    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

}            
            

