package com.moallemi.math.graph;

import java.util.Iterator;

/**
 * Class for constructing a subgraph of another graph.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class SubgraphFactory {
    
    // listener that makes sure a subgraph does not contain elements
    // not in the parent
    private static class SubgraphListener 
	implements GraphModificationListener 
    {
	private Graph parent;
	public SubgraphListener(Graph parent) {
	    this.parent = parent;
	}
	public void graphModificationStarted(Graph graph) {}
	public void graphModificationFinished(Graph graph) {
	    verify(graph);
	}
	public void verify(Graph graph) {
	    for (int i = graph.getNodeCount(); --i >= 0; ) {
		if (!parent.containsNode(graph.getNode(i))) {
		    throw new GraphModificationException("cannot add foreign "
							 + "node to subgraph");
		}
	    }
	    for (int i = graph.getEdgeCount(); --i >= 0; ) {
		if (!parent.containsEdge(graph.getEdge(i))) {
		    throw new GraphModificationException("cannot add foreign "
							 + "edge to subgraph");
		}
	    }
	}
    }


    /**
     * Create an empty subgraph of an existing graph.
     *
     * @param graph the parent graph
     * @return the subgraph
     */
    public static Graph createSubgraph(Graph graph) {
	return createSubgraph(graph, null, null);
    }

    /**
     * Create a subgraph of an existing graph.
     *
     * @param graph the parent graph
     * @param nodes nodes to add to the subgraph, all edges in the
     * parent between these nodes will also be added (can be
     * <code>null</code> in which case no nodes will be added)
     * @param subgraph the subgraph, or <code>null</code> if a new
     * <code>Graph</code> object is to be created
     * @return the subgraph
     */
    public static Graph createSubgraph(Graph graph, 
				       Node[] nodes,
				       Graph subgraph) {
	if (subgraph == null)
	    subgraph = new Graph();
	SubgraphListener listener = new SubgraphListener(graph);
	subgraph.addGraphModificationListener(listener);
	subgraph.beginModification();
	subgraph.removeAllNodes();
	if (nodes != null && nodes.length > 0) {
	    for (int i = 0; i < nodes.length; i++)
		subgraph.addNode(nodes[i]);
	    for (int i = 0; i < nodes.length; i++) {
		for (Iterator j = graph.getEdgeIterator(nodes[i]); 
		     j.hasNext(); ) {
		    Edge edge = (Edge) j.next();
		    if (!subgraph.containsEdge(edge)
			&& subgraph.containsNode(edge.getFirst())
			&& subgraph.containsNode(edge.getSecond()))
			subgraph.addEdge(edge);
		}
	    }
	}
	subgraph.endModification();
	return subgraph;
    }

}
