package com.moallemi.math.graph;

import java.util.*;

/**
 * Breadth-first search of a graph starting at a particular
 * node. Useful for generating a subgraph in the neighborhood of a
 * node. Closely follows that presented in Cormen, Leiserson, Rivest,
 * "Introduction to Algorithms".
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2006-09-13 19:23:11 $
 */
public class BFS implements Iterator<Node> {
    // underlying graph
    private Graph graph;
    // modification count to detect changes in the graph
    private int initialModCount;

    // info map, tracks information on nodes being visited
    private Map<Node,NodeInfo> infoMap = new HashMap<Node,NodeInfo>();

    /**
     * Status flag indicating a node has not been visited.
     */
    public static final Object STATUS_NOT_VISITED = new Object();
    /**
     * Status flag indicating has been discovered but not finished.
     */
    public static final Object STATUS_IN_PROGRESS = new Object();
    /**
     * Status flag indicating a node has finished.
     */    
    public static final Object STATUS_FINISHED = new Object();

    // stack of node states currently being visited
    private LinkedList<NodeState> stack = new LinkedList<NodeState>();
    // the next node to return
    private Node nextNode = null;
    // the maximum depth to search
    private int maxDepth;
    // the graph of all nodes/edges with the given depth
    private Graph depthGraph;
    // are we done?
    private boolean finished = false;
    // discovery time
    private int time = -1;
    // a comparator for node ordering
    private Comparator<Node> nodeComparator;

    /**
     * Constructor.
     *
     * @param graph the graph to search
     * @param node starting node
     * @param depth maximum depth to search from <code>node</code>
     */
    public BFS(Graph graph, Node node, int maxDepth) {
	this(graph, node, maxDepth, null);
    }

    /**
     * Constructor.
     *
     * @param graph the graph to search
     * @param node starting node
     * @param depth maximum depth to search from <code>node</code>
     * @param nodeComparator a comparator for node ordering
     */
    public BFS(Graph graph, 
	       Node node, 
	       int maxDepth,
	       Comparator<Node> nodeComparator) 
    {
	this.graph = graph;
	initialModCount = graph.getModificationCount();
	this.maxDepth = maxDepth;
	this.nodeComparator = nodeComparator;
	depthGraph = new Graph();
	depthGraph.beginModification();
	discover(node, null, -1);
    }

    /**
     * Do we have additional nodes remaining?
     *
     * @return <code>true</code> if additional nodes remain,
     * <code>false</code> otherwise.
     * @throws ConcurrentModificationException if the graph has been modified
     */
    public boolean hasNext() throws ConcurrentModificationException {
	if (graph.getModificationCount() != initialModCount)
	    throw new ConcurrentModificationException();
	fetchNext();
	return nextNode != null;
    }

    /**
     * Return the next node in the search.
     *
     * @return the next node
     * @throws ConcurrentModificationException if the graph has been modified
     * @throws NoSuchElementException if there are no additional nodes
     */
    public Node next() 
	throws NoSuchElementException, ConcurrentModificationException
    {
	if (graph.getModificationCount() != initialModCount)
	    throw new ConcurrentModificationException();
	fetchNext();
	if (nextNode == null)
	    throw new NoSuchElementException();
	Node returnNode = nextNode;
	nextNode = null;
	return returnNode;
    }

    /**
     * Remove the last node returned. Unsupported.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }


    /**
     * Get the status of a node.
     *
     * @param node the node
     * @return one of the 3 status flags
     */
    public Object getStatus(Node node) {
	NodeInfo info = infoMap.get(node);
	return info == null ? STATUS_NOT_VISITED : info.status;
    }

    /**
     * Get the depth of a node.
     *
     * @param node the node
     * @return the depth, or -1 if the node has not been found
     */
    public int getDepth(Node node) {
	NodeInfo info = infoMap.get(node);
	return info == null ? -1 : info.depth;
    }

    /**
     * Get the discovery time of a node. Can be used for topological sort.
     *
     * @param node the node
     * @return the discovery time, or -1 if the node has not been found
     */
    public int getDiscoveryTime(Node node) {
	NodeInfo info = infoMap.get(node);
	return info == null ? -1 : info.discoveryTime;
    }


    /**
     * Get a graph containing all nodes/edges found from the start node
     * thus far.
     *
     * @return the depth graph
     */
    public Graph getDepthGraph() { return depthGraph; }

    /**
     * Get the predecessor of a node in the DFS tree.
     *
     * @param node the node
     * @return the predecessor, or <code>null</null> if there is no
     * predecessor or the node has not been discovered yet
     */
    public Node getPredecessor(Node node) {
	NodeInfo info = infoMap.get(node);
	return info == null ? null : info.predecessor;
    }

    // utility class to hold state
    private class NodeState {
	private Node node;
	@SuppressWarnings("rawtypes")
	private Iterator neighborIterator;

	public NodeState(Node node) {
	    this.node = node;
	    if (nodeComparator == null) 
		neighborIterator = graph.getConnectedNodeIterator(node);
	    else {
		Node[] neighbors = new Node [graph.getNodeDegree(node)];
		for (int i = 0; i < neighbors.length; i++)
		    neighbors[i] = graph.getConnectedNode(node, i);
		Arrays.sort(neighbors, nodeComparator);
		neighborIterator = Arrays.asList(neighbors).iterator();
	    }
	}
	public Node getNode() { return node; }
	@SuppressWarnings("rawtypes")
	public Iterator getIterator() { return neighborIterator; }
    }


    // utility class to hold discovered node info
    private class NodeInfo {
	public Object status = STATUS_NOT_VISITED;
	public int depth = -1;
	public Node predecessor = null;
	public int discoveryTime = -1;
    }


    // discover a new node
    private void discover(Node node, Node predecessor, int predecessorDepth) {
	NodeInfo info = new NodeInfo();
	info.predecessor = predecessor;
	info.status = STATUS_IN_PROGRESS;
	info.depth = predecessorDepth + 1;
	info.discoveryTime = ++time;

	if (infoMap.put(node, info) != null)
	    throw new IllegalStateException("node discovered twice");

	depthGraph.addNode(node);
	if (predecessor != null)
	    depthGraph.addEdge(graph.getEdge(node, predecessor));

	stack.addLast(new NodeState(node));

	nextNode = node;
    }

    // continue the search to fetch the next node
    @SuppressWarnings("rawtypes")
	private void fetchNext() 
    {
	if (finished || nextNode != null)
	    return;

	// backtrace through the stack to find a new node
	while (!stack.isEmpty()) {
	    NodeState state = stack.getFirst();
	    Node node = state.getNode();
	    NodeInfo info = infoMap.get(node);
	    if (info.depth < maxDepth) {
		for (Iterator i = state.getIterator(); i.hasNext(); ) {
		    Node neighbor = (Node) i.next();
		    if (infoMap.get(neighbor) == null) {
			discover(neighbor, node, info.depth);
			return;
		    }
		    else {
			Edge edge = graph.getEdge(node, neighbor);
			if (!depthGraph.containsEdge(edge))
			    depthGraph.addEdge(edge);
		    }
		}
	    }
	    info.status = STATUS_FINISHED;
		
	    stack.removeFirst();
	}

	// we are done
	if (!finished) {
	    finished = true;
	    depthGraph.endModification();
	}
    }

    /**
     * Compute the full BFS.
     *
     * @return <code>this</code>, for syntax convenience
     */
    public BFS compute() {
	while (hasNext())
	    next();
	return this;
    }
}
	
