package com.moallemi.math.graph;

import java.util.*;

/**
 * A simple ring finder, identifies nodes/edges which are present in
 * rings in O(V + E) time. Also give a set of basis rings. Uses a
 * depth first search variation, as explained in Cormen, Leiserson,
 * Rivest, "Algorithms".  The basis set is an arbitrary basis, this
 * soes not perform SSSR!
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class SimpleRingFinder {
    private Graph graph;
    private Graph ringGraph;
    private Ring[] basisRings;

    /**
     * Constructor.
     *
     * @param graph the graph
     */
    public SimpleRingFinder(Graph graph) {
	this.graph = graph;

	// perform a depth first search
	DFS dfs = new DFS(graph);
	dfs.compute();

	Graph treeGraph = dfs.getTreeGraph();
	Edge[] backEdges = new Edge [graph.getEdgeCount() 
				     - treeGraph.getEdgeCount()];
	int cnt = 0;
	for (Iterator i = graph.getEdgeIterator(); i.hasNext(); ) {
	    Edge edge = (Edge) i.next();
	    if (!treeGraph.containsEdge(edge))
		backEdges[cnt++] = edge;
	}
	// make sure we have the correct number of back edges
	if (cnt != backEdges.length)
	    throw new IllegalStateException("incorrect number of back edges");
	if (backEdges.length != 
	    graph.getEdgeCount() - graph.getNodeCount() 
	    + dfs.getConnectedComponentCount())
	    throw new IllegalStateException("incorrect number of back edges");


	// construct basis rings from the list of back edges
	ringGraph = new Graph();
	ringGraph.beginModification();
	basisRings = new Ring [backEdges.length];
	Set firstSet = new HashSet();
        for (int i = 0; i < basisRings.length; i++) {
            Edge edge = backEdges[i];
            Node first = edge.getFirst();
            Node second = edge.getSecond();

            Node lastNode, nextNode;


            // construct a set of all predecessors of the first node
	    firstSet.clear();
            nextNode = first;
            do {
                firstSet.add(nextNode);
            } while ((nextNode = dfs.getPredecessor(nextNode)) != null);


            // start building the ring
            Ring ring = new Ring(graph);
            ring.beginModification();

            // follow the path up from the second node and
            // compute the terminal node
            Node terminalNode;
            lastNode = null;
            nextNode = second;
            for (;;) {
                ring.addNode(nextNode);
                if (lastNode != null)
                    ring.addEdge(graph.getEdge(lastNode, nextNode));
                if (firstSet.contains(nextNode)) {
                    terminalNode = nextNode;
                    break;
                }
                lastNode = nextNode;
                if ((nextNode = dfs.getPredecessor(lastNode)) == null) {
                    if (lastNode != second)
                        throw new IllegalStateException("found "
                                                        + "non-intersecting "
                                                        + "DFS paths");
                    terminalNode = second;
                    break;
                }
            }


            // follow the path up from the first node
            lastNode = null;
            nextNode = first;
            while (nextNode != terminalNode) {
                ring.addNode(nextNode);
                if (lastNode != null)
                    ring.addEdge(graph.getEdge(lastNode, nextNode));
                lastNode = nextNode;
                nextNode = dfs.getPredecessor(lastNode);
                if (nextNode == null)
                    throw new IllegalStateException("bad DFS backtrace");
            }
            if (lastNode != null)
                ring.addEdge(graph.getEdge(lastNode, nextNode));

            // finally, add the back edge
            ring.addEdge(edge);


            ring.reorderAsRing();
            ring.endModification();

            basisRings[i] = ring;
            ringGraph.addGraph(ring);
        }

        ringGraph.endModification();

    }

    /**
     * Get the graph this ring finder operated on.
     *
     * @return the graph
     */
    public Graph getGraph() { return graph; }
    
    /**
     * Get the ring graph. This graph contains every node and edge
     * that is in at least one ring.
     *
     * @return the ring graph
     */
    public Graph getRingGraph() { return ringGraph; }

    /**
     * Get the dimension of the ring space.
     *
     * @return the ring space dimension
     */
    public int getRingDimension() { return basisRings.length; }

    /**
     * Get a basis ring. (These are an arbitrary basis, not the SSSR!
     *
     * @param index the index of the basis ring
     * @return the basis ring
     */
    public Ring getBasisRing(int index) { return basisRings[index]; }
}


		
		
		    

	
