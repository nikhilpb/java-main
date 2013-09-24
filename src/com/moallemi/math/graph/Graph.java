package com.moallemi.math.graph;

import java.util.*;
import java.io.*;
import javax.swing.event.EventListenerList;

import com.moallemi.util.data.AttributeMap;

/**
 * A generic class for graphs, i.e. collections of edges and nodes.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.5 $, $Date: 2006-09-05 22:00:11 $
 */
public class Graph {
    /**
     * A class holding information for a graph node
     */
    private class NodeInfo {
	// the undelying node 
	private Node node;
	// the index of this node within the graph
	private int index;
	// a collection of the edges connected to this node
	private List<Edge> nodeEdges = new ArrayList<Edge>();

	/**
	 * Constructs a <code>NodeInfo</code> object with a specific index
	 *
	 * @param node the underlying node
	 * @param index the index of the <code>Node</code> with which
	 * this object is associated
	 */
	public NodeInfo(Node node, int index) { 
	    this.node = node;
	    setIndex(index); 
	}
	
	/**
	 * Return the undelying node.
	 *
	 * @return the undelying node
	 */
	public Node getNode() { return node; }

	/**
	 * Return the index of this node within the graph
	 *
	 * @return the index of the <code>Node</code>
	 */
	public int getIndex() { return index; }

	/**
	 * Set the index of this node
	 *
	 * @param index the index of this <code>Node</code>
	 */
	public void setIndex(int index) { this.index = index; }

	/**
	 * Get the graph edges connected to this node
	 *
	 * @return a <code>List</code> of the edges connected to this node
	 */
	public List<Edge> getEdges() { return nodeEdges; }

    }

    /**
     * A class containing information related to a graph edge
     */
    private class EdgeInfo {
	// the underlying edge
	private Edge edge;
	// the index of this edge within the graph
	private int index;	

	/**
	 * Constucts an <code>EdgeInfo</code> object with a specified index
	 *
	 * @param edge the underlying edge
	 * @param index the index of the edge associated with this object
	 */
	public EdgeInfo(Edge edge, int index) { 
	    this.edge = edge;
	    setIndex(index); 
	}

	/**
	 * Get the underlying edge.
	 *
	 * @return the underlying edge
	 */
	public Edge getEdge() { return edge; }

	/** 
	 * Get the index of this edge within the graph
	 *
	 * @return the index of this edge within the graph
	 */
	public int getIndex() { return index; }

	/**
	 * Set the index of this edge within the graph
	 *
	 * @param index the index of this edge
	 */
	public void setIndex(int index) { this.index = index; }



    }

    // running tally of the number of modifications made to this graph
    private int modCount = 0;
    // is the graph being modified
    private boolean isInModification = false;
    // list of modification listeners
    private EventListenerList listenerList = new EventListenerList();

    // collection of all node info objects contained by the graph
    private List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
    // map associating a node info object with a node
    private Map<Node,NodeInfo> nodeInfoMap = new HashMap<Node,NodeInfo>();
    // collection of all edge info objects contained by this graph
    private List<EdgeInfo> edgeInfoList = new ArrayList<EdgeInfo>();
    // map associating an edge info object with an edge
    private Map<Edge,EdgeInfo> edgeInfoMap = new HashMap<Edge,EdgeInfo>();
    // attributes for this graph
    private AttributeMap attributeMap = new AttributeMap();

    /**
     * Default constructor.
     */
    public Graph() {}

    /**
     * Construct a clone of the nodes and edges another graph.
     * Does a shallow copy of the graph properties.
     *
     * @param other graph to clone
     */
    public Graph(Graph other) {
	beginModification();
	addGraph(other);
	attributeMap.setAll(other.attributeMap);
	endModification();
    }


    /**
     * Get the attributes associated with this graph.
     *
     * @return the attribute map
     */
    public AttributeMap getAttributeMap() { return attributeMap; }

    /**
     * Check to see if this graph contains the specified node
     *
     * @param node the <code>Node</code> to check for
     * @return <code>true</code> if the node is present,
     * <code>false</code> otherwise
     */
    public boolean containsNode(Node node) {
	return getNodeInfo(node) != null;
    }

    /**
     * Get the average degree of the graph.
     *
     * @return the average degree (0.0 if the graph is empty)
     */
    public double getAverageDegree() {
        if (getNodeCount() == 0)
            return 0.0;
        return 2.0 * ((double) getEdgeCount()) / ((double) getNodeCount());
    }

    /**
     * Add a node to this graph.
     *
     * @param node the <code>Node</code> to be added
     * @throws IllegalArgumentException if the graph contains the node already
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void addNode(Node node) 
	throws IllegalArgumentException, GraphModificationException
    {
	assertInModification();
	if (getNodeInfo(node) != null)
	    throw new IllegalArgumentException("duplicate nodes defined");
	NodeInfo nodeInfo = new NodeInfo(node, nodeInfoList.size());
	nodeInfoMap.put(node, nodeInfo);
	nodeInfoList.add(nodeInfo);
	modCount++;
    }

    /**
     * Get the node associated with the specified index.
     *
     * @param i the index of the <code>Node</code> to retrieve
     * @return the <code>Node</code> contained by this graph at the
     * specified index
     */
    public Node getNode(int i) {
	return nodeInfoList.get(i).getNode();
    }

    /**
     * Get a node at an index connected to the specified node.
     *
     * @param node the node to lookup connected nodes for
     * @param index the index of the connected node
     * @return the connected node
     */
    public Node getConnectedNode(Node node, int index) {
	NodeInfo nodeInfo = getNodeInfo(node);
	Edge edge = nodeInfo.getEdges().get(index);
	if (edge.getFirst().equals(node)) 
	    return edge.getSecond();
	else if (edge.getSecond().equals(node)) 
	    return edge.getFirst();
	else
	    throw new IllegalStateException("edge connected to wrong"
					    + " node");
    }

    /**
     * Get the offset of a connected node.
     *
     * @param node the node to lookup connected nodes for
     * @param other the connected node
     * @return the connected node offset, or -1 if not connected
     */
    public int getConnectedNodeOffset(Node node, Node other) {
	NodeInfo nodeInfo = getNodeInfo(node);
        List<Edge> edges = nodeInfo.getEdges();
        int nodeDegree = edges.size();
        for (int c = 0; c < nodeDegree; c++) {
            Edge edge = edges.get(c);
            if (edge.getFirst().equals(other)) 
                return c;
            else if (edge.getSecond().equals(other)) 
                return c;
        }
        return -1;
    }


    /**
     * Get the edge at an index connected to the specified node.
     *
     * @param node the node to lookup connected nodes for
     * @param index the index of the edge
     * @return the edge
     */
    public Edge getEdge(Node node, int index) {
	NodeInfo nodeInfo = getNodeInfo(node);
	return nodeInfo.getEdges().get(index);
    }

    // class for iterating across connected nodes
    private class ConnectedNodeIterator implements Iterator<Node> {
	private int index, initialModCount;
	private NodeInfo nodeInfo;
	public ConnectedNodeIterator(Node node) {
	    index = 0;
	    initialModCount = modCount;
	    nodeInfo = getNodeInfo(node);
	}
	public boolean hasNext() throws ConcurrentModificationException {
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    return index < nodeInfo.getEdges().size();
	}
	public Node next() 
	    throws NoSuchElementException, ConcurrentModificationException
	{
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    if (index >= nodeInfo.getEdges().size())
		throw new NoSuchElementException();
	    Edge edge = nodeInfo.getEdges().get(index);
	    Node node = nodeInfo.getNode();
	    index++;
	    if (edge.getFirst().equals(node))
		return edge.getSecond();
	    else if (edge.getSecond().equals(node))
		return edge.getFirst();
	    else
		throw new IllegalStateException("edge connected to wrong"
						 + " node");
	}
	public void remove() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * Get an iterator across all nodes connected to a specific node.
     *
     * @param node the node
     * @return an iterator over connected nodes
     */
    public Iterator<Node> getConnectedNodeIterator(Node node) {
	return new ConnectedNodeIterator(node);
    }

    /**
     * Get the degree of a node, or the number of nodes connected to
     * that node.
     *
     * @param node the node
     * @return the degree
     * @throws IllegalArgumentException if the graph does not contain the node
     */
    public int getNodeDegree(Node node) throws IllegalArgumentException {
	NodeInfo nodeInfo = getNodeInfo(node);
	if (nodeInfo == null)
	    throw new IllegalArgumentException("unknown node");
	return nodeInfo.getEdges().size();
    }

    /**
     * Get the degree of a node, or the number of nodes connected to
     * that node.
     *
     * @param index the node index
     * @return the degree
     */
    public int getNodeDegree(int index) {
	NodeInfo nodeInfo = nodeInfoList.get(index);
	return nodeInfo.getEdges().size();
    }

    /**
     * Get the index of the specified node
     *
     * @param node the node to lookup an index for
     * @return the index of the node in this graph, <code>-1</code> if
     * the node is not present
     */
    public int getNodeIndex(Node node) {
	NodeInfo nodeInfo = getNodeInfo(node);
	if (nodeInfo == null) return (-1);
	return (nodeInfo.getIndex());
    }

    // an iterator class over nodes
    private class NodeIterator implements Iterator<Node> {
	private int index, initialModCount;
	private Node lastNode;
	public NodeIterator() {
	    lastNode = null;
	    index = 0;
	    initialModCount = modCount;
	}
	public boolean hasNext() throws ConcurrentModificationException {
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    return index < getNodeCount();
	}
	public Node next() 
	    throws NoSuchElementException, ConcurrentModificationException
	{
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    if (index >= getNodeCount())
		throw new NoSuchElementException();
	    lastNode = getNode(index);
	    index++;
	    return lastNode;
	}
	public void remove() {
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    if (lastNode == null)
		throw new IllegalStateException();
	    removeNode(lastNode);
	    lastNode = null;
	    index--;
	    initialModCount = modCount;
	}
    }

    /**
     * Return an iterator over the nodes.
     *
     * @return an iterator over the nodes
     */
    public Iterator<Node> getNodeIterator() {
	return new NodeIterator();
    }

    /**
     * Remove a node from this graph
     *
     * @param node the <code>Node</code> to be removed
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void removeNode(Node node) throws GraphModificationException {
	assertInModification();
	NodeInfo nodeInfo = getNodeInfo(node);
	if (nodeInfo == null)
	    throw new NoSuchElementException("cannot remove unknown node");

	//must cache edges to be removed to prevent
	//ConcurrentModificationException on underlying edge
	//collection
	ArrayList<Edge> removeEdgeCache = new ArrayList<Edge>();
	for (Iterator<Edge> i = nodeInfo.getEdges().iterator(); i.hasNext(); )
	    removeEdgeCache.add(i.next());
	for (Iterator<Edge> i = removeEdgeCache.iterator(); i.hasNext(); )
	    internalRemoveEdge(i.next());
	
	renumberEdges();
	
	nodeInfoMap.remove(node);
	nodeInfoList.remove(nodeInfo);
	renumberNodes();

	modCount++;
    }

    /**
     * Remove a node from this graph
     *
     * @param index the index of the node to be removed
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void removeNode(int index) throws GraphModificationException {
	removeNode(getNode(index));
    }


    /**
     * Clear the graph. Remove all nodes and edges. Attributes are not
     * changed.
     *
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void removeAllNodes() throws GraphModificationException {
	assertInModification();
	nodeInfoList.clear();
	nodeInfoMap.clear();
	edgeInfoList.clear();
	edgeInfoMap.clear();
	modCount++;
    }

    /**
     * Renumbers all nodes contained by this graph
     */
    private void renumberNodes() {
	for (int i = nodeInfoList.size(); --i >= 0; ) {
	    NodeInfo nodeInfo = nodeInfoList.get(i);
	    nodeInfo.setIndex(i);
	}
    }

    /**
     * Orders a particular node as the graph root (index 0)
     *
     * @param node the node to be set as the root
     * @throws IllegalArgumentException if this graph does 
     *         not contain the specified node
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void orderAsRoot(Node node)
	throws IllegalArgumentException, GraphModificationException
    {
	assertInModification();

	int swapIndex = getNodeIndex(node);
	if (swapIndex == -1) 
	    throw new IllegalArgumentException("Graph does not contain " +
					       "specified node");

	
	NodeInfo oldRootNodeInfo = nodeInfoList.get(0);
	NodeInfo newRootNodeInfo = nodeInfoList.get(swapIndex);
	oldRootNodeInfo.setIndex(swapIndex);
	nodeInfoList.set(swapIndex, oldRootNodeInfo);
	newRootNodeInfo.setIndex(0);
	nodeInfoList.set(0, newRootNodeInfo);
	
	modCount++;
    }

    /**
     * Reorders nodes in this graph according to a specified set of
     * indices User must take care to ensure the indices array is the
     * same size as the number of nodes in the graph and contains one
     * and only one copy of an index for every node.
     *
     * @param indices the set of indices to apply to the node numbering
     * @throws IllegalArgumentException if the indices array is not
     * a bijective mapping
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void reorderNodes(int[] indices) 
	throws IllegalArgumentException, GraphModificationException
    {
	assertInModification();
	if (nodeInfoList.size() != indices.length)
	    throw new IllegalArgumentException("invalid index array length");
	
	boolean[] used = new boolean [nodeInfoList.size()];
	Arrays.fill(used, false);

	ArrayList<NodeInfo> newNodeInfoList = 
            new ArrayList<NodeInfo>(nodeInfoList.size());
	for (int i = 0; i < indices.length; i++) {
	    int idx = indices[i];
	    if (used[idx]) 
		throw new IllegalArgumentException("index array is not "
						   + "bijective");
	    used[idx] = true;
	    newNodeInfoList.add(nodeInfoList.get(idx));
	}
	nodeInfoList = newNodeInfoList;

	renumberNodes();
	modCount++;
    }

    /**
     * Get the number of nodes contained by this graph
     *
     * @return the number of nodes contained by this graph
     */
    public int getNodeCount() { return nodeInfoList.size(); }

    /**
     * Get the node information associated with a specified node
     *
     * @param node the <code>Node</code> for which information is to
     * be retrieved
     * @return the associated <code>NodeInfo</code> object
     */
    private NodeInfo getNodeInfo(Node node) {
	return nodeInfoMap.get(node);
    }

    /**
     * Check to see if this graph contains the specified edge
     *
     * @param edge the <code>Edge</code> to check for
     * @return <code>true</code> if the edge is present,
     * <code>false</code> otherwise
     */
    public boolean containsEdge(Edge edge) {
	return edgeInfoMap.get(edge) != null;
    }

    /**
     * Add an edge to this graph
     * Throws an exception if this graph already contains information for
     * the specified edge.
     *
     * @param edge the <code>Edge</code> to be added
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void addEdge(Edge edge) throws GraphModificationException {
	assertInModification();
	if (edgeInfoMap.get(edge) != null)
	    throw new IllegalArgumentException("duplicate edges defined");
	NodeInfo firstNodeInfo = getNodeInfo(edge.getFirst());
	NodeInfo secondNodeInfo = getNodeInfo(edge.getSecond());
	if (firstNodeInfo == null || secondNodeInfo == null)
	    throw new IllegalArgumentException("edge attached to "
					       + "unknown nodes");
	firstNodeInfo.getEdges().add(edge);
	secondNodeInfo.getEdges().add(edge);

	EdgeInfo edgeInfo =  new EdgeInfo(edge, edgeInfoList.size());
	edgeInfoMap.put(edge,edgeInfo);
	edgeInfoList.add(edgeInfo);
	modCount++;
    }

    /**
     * Get the edge associated with the specified index
     *
     * @param i the index of the <code>Edge</code> to retrieve
     * @return the <code>Edge</code> contained by this graph at the
     * specified index
     */
    public Edge getEdge(int i) {
	return edgeInfoList.get(i).getEdge();
    }

    /**
     * Get the index of the specified edge
     *
     * @param edge the edge to lookup an index for
     * @return the index of the edge in this graph, <code>-1</code> if
     * the edge is not present
     */
    public int getEdgeIndex(Edge edge) {
	EdgeInfo edgeInfo = getEdgeInfo(edge);
	if (edgeInfo == null) return (-1);
	return (edgeInfo.getIndex());
    }

    /**
     * Remove an edge from this graph
     *
     * @param edge the edge to be removed
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void removeEdge(Edge edge) throws GraphModificationException {
	assertInModification();
	internalRemoveEdge(edge);
	renumberEdges();
	modCount++;
    }

    /**
     * Remove an edge from this graph
     *
     * @param index the index of the edge to be removed
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void removeEdge(int index) throws GraphModificationException {
	removeEdge(getEdge(index));
    }


    /**
     * Renumbers all edges contained by this graph
     */
    private void renumberEdges() {
	for (int i = edgeInfoList.size(); --i >= 0; ) {
	    EdgeInfo edgeInfo = edgeInfoList.get(i);
	    edgeInfo.setIndex(i);
	}
    }

    /**
     * Reorders edges in this graph according to a specified set of
     * indices User must take care to ensure the indices array is the
     * same size as the number of edges in the graph and contains one
     * and only one copy of an index for every edge.
     *
     * @param indices the set of indices to apply to the edge numbering
     * @throws IllegalArgumentException if the indices array is not
     * a bijective mapping
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void reorderEdges(int[] indices) {
	assertInModification();
	if (edgeInfoList.size() != indices.length)
	    throw new IllegalArgumentException("invalid index array length");
	
	boolean[] used = new boolean [edgeInfoList.size()];
	Arrays.fill(used, false);

	ArrayList<EdgeInfo> newEdgeInfoList = new ArrayList<EdgeInfo>();
	for (int i = 0; i < indices.length; i++) {
	    int idx = indices[i];
	    if (used[idx]) 
		throw new IllegalArgumentException("index array is not "
						   + "bijective");
	    used[idx] = true;
	    newEdgeInfoList.add(edgeInfoList.get(idx));
	}
	edgeInfoList = newEdgeInfoList;

	renumberEdges();
	modCount++;
    }

    /**
     * Get the number of edges contained by this graph
     *
     * @return the number of edges contained by this graph
     */
    public int getEdgeCount() { return edgeInfoList.size(); }

    /**
     * Get the edge information associated with a specified edge
     *
     * @param edge the <code>Edge</code> for which information is to
     * be retrieved
     * @return the associated <code>EdgeInfo</code> object
     */
    private EdgeInfo getEdgeInfo(Edge edge) {
	return edgeInfoMap.get(edge);
    }

    /**
     * Takes care of removing connectivities when an edge is removed
     *
     * @param edge the <code>Edge</code> being removed
     */
    private void internalRemoveEdge(Edge edge) {
	EdgeInfo edgeInfo = getEdgeInfo(edge);
	if (edgeInfo == null)
	    throw new NoSuchElementException("cannot remove unknown edge");
	getNodeInfo(edge.getFirst()).getEdges().remove(edge);
	getNodeInfo(edge.getSecond()).getEdges().remove(edge);
	edgeInfoMap.remove(edge);
	edgeInfoList.remove(edgeInfo);
    }

    /**
     * Check to see if two nodes are connected
     *
     * @param firstNode the first node to check
     * @param secondNode the second node to check
     * @return <code>true></code> if the nodes are connected,
     * <code>false</code> otherwise
     */
    public boolean areConnected(Node firstNode, Node secondNode) {
	return getEdge(firstNode, secondNode) != null;
    }

    /**
     * Get the edge between two nodes
     *
     * @param firstNode the first node of the edge in question
     * @param secondNode the second node of the edge in question
     * @return the <code>Edge</code> between the two nodes, or
     * <code>null</code>
     * @throws IllegalArgumentException if either node is not in the graph
     */
    public Edge getEdge(Node firstNode, Node secondNode) {
	NodeInfo firstNodeInfo = getNodeInfo(firstNode);
	NodeInfo secondNodeInfo = getNodeInfo(secondNode);
	if (firstNodeInfo == null || secondNodeInfo == null) 
	    throw new IllegalArgumentException("graph does not contain node");
	List<Edge> edges = firstNodeInfo.getEdges();
	for (int i = edges.size(); --i >= 0; ) {
	    Edge edge = (Edge) edges.get(i);
	    if (edge.getFirst().equals(secondNode) 
		|| edge.getSecond().equals(secondNode))
		return (edge);
	}
	return null;
    }



    // an iterator class over all edges
    private class EdgeIterator implements Iterator<Edge> {
	private int index, initialModCount;
	private Edge lastEdge;
	public EdgeIterator() {
	    lastEdge = null;
	    index = 0;
	    initialModCount = modCount;
	}
	public boolean hasNext() throws ConcurrentModificationException {
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    return index < getEdgeCount();
	}
	public Edge next() 
	    throws NoSuchElementException, ConcurrentModificationException
	{
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    if (index >= getEdgeCount())
		throw new NoSuchElementException();
	    lastEdge = getEdge(index);
	    index++;
	    return lastEdge;
	}
	public void remove() {
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    if (lastEdge == null)
		throw new IllegalStateException();
	    removeEdge(lastEdge);
	    lastEdge = null;
	    index--;
	    initialModCount = modCount;
	}
    }

    /**
     * Return an iterator over the edges.
     *
     * @return an iterator over the edges
     */
    public Iterator<Edge> getEdgeIterator() {
	return new EdgeIterator();
    }


    // an iterator class over edges connected to a single node
    private class SingleNodeEdgeIterator implements Iterator<Edge> {
	private NodeInfo nodeInfo;
	private int index, initialModCount;
	private Edge lastEdge;
	public SingleNodeEdgeIterator(Node node) {
	    nodeInfo = getNodeInfo(node);
	    lastEdge = null;
	    index = 0;
	    initialModCount = modCount;
	}
	public boolean hasNext() throws ConcurrentModificationException {
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    return index < nodeInfo.getEdges().size();
	}
	public Edge next() 
	    throws NoSuchElementException, ConcurrentModificationException
	{
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    if (index >= getEdgeCount())
		throw new NoSuchElementException();
	    lastEdge = nodeInfo.getEdges().get(index);
	    index++;
	    return lastEdge;
	}
	public void remove() {
	    if (initialModCount != modCount)
		throw new ConcurrentModificationException();
	    if (lastEdge == null)
		throw new IllegalStateException();
	    removeEdge(lastEdge);
	    lastEdge = null;
	    index--;
	    initialModCount = modCount;
	}
    }

    /**
     * Return an iterator over the edges of a single node.
     *
     * @param node the node
     * @return an iterator over the edges
     */
    public Iterator<Edge> getEdgeIterator(Node node) {
	return new SingleNodeEdgeIterator(node);
    }


    /**
     * Add the nodes and edges of another graph to this one. Edges or
     * nodes which already exist in this graph are ignored.
     *
     * @param graph graph to add
     * @throws GraphModificationException if the graph is not in the
     * modification state
     */
    public void addGraph(Graph other) 
	throws GraphModificationException 
    {
	assertInModification();
	for (Iterator<Node> i = other.getNodeIterator(); i.hasNext(); ) {
	    Node node = i.next();
	    if (!containsNode(node)) 
		addNode(node);
	}
	for (Iterator<Edge> i = other.getEdgeIterator(); i.hasNext(); ) {
	    Edge edge = i.next();
	    if (!containsEdge(edge)) 
		addEdge(edge);
	}
    }

    /**
     * Get the modification count. When nodes or edges are added or removed,
     * this count changes.
     *
     * @return the modification count
     */
    public int getModificationCount() { return modCount; }

    /**
     * Begin graph modification.
     */
    public void beginModification() {
	if (isInModification)
	    return;
	isInModification = true;
	if (listenerList.getListenerCount() > 0) {
	    Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i += 2) {
		if (listeners[i] == GraphModificationListener.class) 
		    ((GraphModificationListener) listeners[i+1])
			.graphModificationStarted(this);
	    }
	}
    }

    /**
     * Finish graph modification.
     */
    public void endModification() {
	if (!isInModification)
	    return;
	if (listenerList.getListenerCount() > 0) {
	    Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i += 2) {
		if (listeners[i] == GraphModificationListener.class) 
		    ((GraphModificationListener) listeners[i+1])
			.graphModificationFinished(this);
	    }
	}
	isInModification = false;
    }

    /**
     * Is the graph being modified?
     *
     * @return <code>true</code> if the graph is being modified,
     * <code>false</code> otherwise
     */
    public boolean isInModification() { return isInModification; }

    /**
     * Add a graph modification listener.
     *
     * @param listener the listener
     */
    public void addGraphModificationListener(GraphModificationListener
					     listener) {
	listenerList.add(GraphModificationListener.class, listener);
    }


    /**
     * Remove a graph modification listener.
     *
     * @param listener the listener to remove
     */
    public void removeGraphModificationListener(GraphModificationListener
						listener) {
	listenerList.remove(GraphModificationListener.class, listener);
    }

    // utility
    protected void assertInModification() throws GraphModificationException {
	if (!isInModification)
	    throw new GraphModificationException();
    }

    public void dumpInfo(PrintStream out) {
        int nodeCount = getNodeCount();
        out.println("nodes: " + nodeCount);
        out.println("edges: " + getEdgeCount());
        for (int i = 0; i < nodeCount; i++) {
            Node node = getNode(i);
            out.print("n" + (i+1) + ":");
            int nodeDegree = getNodeDegree(i);
            for (int j = 0; j < nodeDegree; j++) {
                Node other = getConnectedNode(node, j);
                out.print(" " + (getNodeIndex(other)+1));
            }
            out.println();
        }
    }
}
