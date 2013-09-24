package com.moallemi.math.graph;

/**
 * A class representing a ring in a graph
 *
 * @author Keith A. Mason
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class Ring extends Graph
{
    // ensures we really have a ring
    private static final GraphModificationListener ringVerify =
	new GraphModificationListener() {
		public void graphModificationStarted(Graph graph) {}
		public void graphModificationFinished(Graph graph) {
		    int n = graph.getNodeCount();
		    int e = graph.getEdgeCount();
		    if (e != n) 
			throw new IllegalStateException("graph is not "
							+ "a ring");
		    for (int i = 0; i < n; i++) {
			Node first = graph.getNode(i);
			Node second = graph.getNode((i+1)%n);
			if (graph.getNodeDegree(first) != 2)
			    throw new IllegalStateException("graph is not "
							    + "a ring");
			if (graph.getEdge(first, second) != graph.getEdge(i))
			    throw new IllegalStateException("graph is not "
							    + "a ring");
		    }
  		}
	    };
    
    /**
     * Constructor.
     *
     * @param graph the parent graph
     */
    public Ring(Graph graph) {
	super();
	addGraphModificationListener(ringVerify);
    }
    
    /**
     * Reorder as a ring. The ring must be modifiable.
     */
    public void reorderAsRing() {
	int n = getNodeCount();
	int e = getEdgeCount();
	
	if (n != e)
	    throw new IllegalStateException("graph is not a ring: " +
					    "number of nodes differs from " +
					    "number of edges");
	if (n < 3)
	    throw new IllegalStateException("graph is not a ring: " +
					    "less than 3 nodes");
	if (e < 3)
	    throw new IllegalStateException("graph is not a ring: " +
					    "less than 3 edges");
	
	int[] nodeOrdering = new int [n];
	DFS dfs = new DFS(this, getNode(0));
	for (int i = 0; dfs.hasNext(); i++)
	    nodeOrdering[i] = 
		getNodeIndex((Node) dfs.next());
	reorderNodes(nodeOrdering);
	
	int[] edgeOrdering = new int [e];
	for (int i = 1; i <= n ; i++) {
	    Node first = getNode(i-1);
	    Node second = getNode(i < n ? i : 0);
	    if (getNodeDegree(first) != 2)
		throw new IllegalStateException("graph is not a ring: " +
						"node degree not equal to 2");
	    if (!areConnected(first, second))
		throw new IllegalStateException("graph is not a ring: " +
						"adjacent nodes " +
						"are not connected");
	    edgeOrdering[i-1] = 
		getEdgeIndex(getEdge(first, second));
	}
	reorderEdges(edgeOrdering);
    }
}


