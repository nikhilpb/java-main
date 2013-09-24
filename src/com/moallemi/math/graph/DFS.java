package com.moallemi.math.graph;

import java.util.*;

/**
 * Depth-first search of a graph. Closely follows that presented in
 * Cormen, Leiserson, Rivest, "Introduction to Algorithms".
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2006-09-13 19:23:11 $
 */
public class DFS implements Iterator<Node> {
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

    // time variable
    private int time = -1;
    // stack of node states currently being visited
    private Stack<NodeState> stack = new Stack<NodeState>();
    // the next node to return
    private Node nextNode = null;
    // iterator across all nodes in the graph
    private Iterator<Node> allNodeIterator;
    // number of connected components
    private int compCount = 0;
    // the resulting tree graph
    private Graph treeGraph;
    // are we done?
    private boolean finished = false;

    /**
     * Constructor.
     *
     * @param graph the graph to search
     */
    public DFS(Graph graph) {
	this.graph = graph;
	allNodeIterator = graph.getNodeIterator();
	initialModCount = graph.getModificationCount();
	treeGraph = new Graph();
	treeGraph.beginModification();
    }

    /**
     * Constructor.
     *
     * @param graph the graph to search
     * @param node node to start the search at
     */
    public DFS(Graph graph, Node node) {
	this(graph);
	discover(node, null);
    }

    /**
     * Do we have additional nodes remaining?
     *
     * @return <code>true</code> if additional node remain,
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
     * Get the discovery time of a node.
     *
     * @param node the node
     * @return the discovery time, or -1 if the node has not been discovered
     */
    public int getDiscoveryTime(Node node) {
	NodeInfo info = infoMap.get(node);
	return info == null ? -1 : info.discoveryTime;
    }

    /**
     * Get the finish time of a node.
     *
     * @param node the node
     * @return the finish time, or -1 if the node has not finished
     */
    public int getFinishTime(Node node) {
	NodeInfo info = infoMap.get(node);
	return info == null ? -1 : info.finishTime;
    }

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

    /**
     * Get the connected component of a node in the DFS tree.
     *
     * @param node the node
     * @return the connected component, or -1 if the
     * the connected component is not known yet
     */
    public int getConnectedComponent(Node node) {
	NodeInfo info = infoMap.get(node);
	return info == null ? -1 : info.component;
    }

    /**
     * Get the number of connected components discovered thus far.
     *
     * @return the number of connected components
     */
    public int getConnectedComponentCount() { return compCount; }


    /**
     * Get the tree graph. Contains all nodes visited and edges
     * traversed in the DFS thus far.
     *
     * @return the tree graph
     */
    public Graph getTreeGraph() { return treeGraph; }

    /**
     * Build a list of connected components based on the DFS thus far.
     *
     * @return the set of connected components
     */
    public Graph[] buildConnectedComponents() {
	// construct the component graphs
	Graph[] components = new Graph [compCount];
	for (int i = 0; i < components.length; i++) {
	    components[i] = new Graph();
	    components[i].beginModification();
	}
	// add the nodes
	for (Iterator<Map.Entry<Node,NodeInfo>> i = 
                 infoMap.entrySet().iterator(); 
             i.hasNext(); ) {
            Map.Entry<Node,NodeInfo> e = i.next();
	    Node node = e.getKey();
	    NodeInfo info = e.getValue();
	    components[info.component].addNode(node);
	}
	// add the edges & finish
	int edgeCount = 0;
	for (int i = 0; i < components.length; i++) {
	    for (int j = components[i].getNodeCount(); --j >= 0; ) {
		Node node = components[i].getNode(j);
		for (int k = graph.getNodeDegree(node); --k >= 0; ) {
		    Edge edge = graph.getEdge(node, k);
		    // only add edges once
		    if (node == edge.getFirst()
			&& components[i].containsNode(edge.getSecond())) {
			components[i].addEdge(edge);
			edgeCount++;
		    }
		}
	    }
	    components[i].endModification();
	}
	// sanity check for cases when DFS is finished
	if (infoMap.size() == graph.getNodeCount()
	    && edgeCount != graph.getEdgeCount())
	    throw new IllegalStateException("connected components "
					    + "have incorrect number "
					    + "of edges");
	return components;
    }


    // utility class to hold state
    private class NodeState {
	private Node node;
	@SuppressWarnings("rawtypes")
	private Iterator neighborIterator;
	public NodeState(Node node) {
	    this.node = node;
	    neighborIterator = graph.getConnectedNodeIterator(node);
	}
	public Node getNode() { return node; }
	@SuppressWarnings("rawtypes")
	public Iterator getIterator() { return neighborIterator; }
    }


    // utility class to hold discovered node info
    private class NodeInfo {
	public Object status = STATUS_NOT_VISITED;
	public int discoveryTime = -1;
	public int finishTime = -1;
	public Node predecessor = null;
	public int component = -1;
    }


    // discover a new node
    private void discover(Node node, Node predecessor) {
	NodeInfo info = new NodeInfo();
	info.predecessor = predecessor;
	info.status = STATUS_IN_PROGRESS;
	info.discoveryTime = ++time;
	if (predecessor == null)
	    compCount++;
	info.component = compCount - 1;

	if (infoMap.put(node, info) != null)
	    throw new IllegalStateException("node discovered twice");

	treeGraph.addNode(node);
	if (predecessor != null)
	    treeGraph.addEdge(graph.getEdge(node, predecessor));

	stack.push(new NodeState(node));

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
	    NodeState state = stack.peek();
	    Node node = state.getNode();
	    NodeInfo info = infoMap.get(node);
	    for (Iterator i = state.getIterator(); i.hasNext(); ) {
		Node neighbor = (Node) i.next();
		if (infoMap.get(neighbor) == null) {
		    discover(neighbor, node);
		    return;
		}
	    }
	    info.status = STATUS_FINISHED;
	    info.finishTime = ++time;
		
	    stack.pop();
	}

	// otherwise iterate through remaining nodes
	while (allNodeIterator.hasNext()) {
	    Node node = allNodeIterator.next();
	    if (infoMap.get(node) == null) {
		discover(node, null);
		return;
	    }
	}

	// we are done
	if (!finished) {
	    finished = true;
	    treeGraph.endModification();
	}
    }

    /**
     * Compute the full DFS.
     *
     * @return <code>this</code>, for syntax convenience
     */
    public DFS compute() {
	while (hasNext())
	    next();
	return this;
    }
}
	
