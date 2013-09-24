package com.moallemi.math.graph;

import java.util.*;
import com.moallemi.util.data.BiDirectionalMap;

/**
 * A mapping between 2 graphs.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-08-31 08:40:14 $
 */
public class GraphMap {
    private Graph source, target;
    private BiDirectionalMap nodeMap = new BiDirectionalMap();

    /**
     * Constructor. 
     *
     * @param source the source graph
     * @param target the target graph
     * @param map a map of source nodes to target nodes
     * @throws IllegalArgumentException is the map is not injective
     */
    public GraphMap(Graph source, 
		    Graph target,
		    Map map) 
	throws IllegalArgumentException
    {
	this.source = source;
	this.target = target;
	for (Iterator i = map.entrySet().iterator(); 
             i.hasNext(); ) {
	    Map.Entry e = (Map.Entry) i.next();
	    if (nodeMap.putPair(e.getKey(), e.getValue()) != null)
		throw new IllegalArgumentException("map is not injective");
	}
    }

    /**
     * Get the target node associated with a particular source node.
     *
     * @param node the source node
     * @return the target node, or <code>null</code> if there is no mapping
     */
    public Node getTargetNode(Node node) {
	return (Node) nodeMap.getByKey(node);
    }

    /**
     * Get the source node associated with a particular target node.
     *
     * @param node the target node
     * @return the source node, or <code>null</code> if there is no mapping
     */
    public Node getSourceNode(Node node) {
	return (Node) nodeMap.getByValue(node);
    }

    /**
     * Get the target edge associated with a particular source edge.
     *
     * @param edge the source edge
     * @return the target edge, or <code>null</code> if there is no mapping
     */
    public Edge getTargetEdge(Edge edge) {
	Node first = getTargetNode(edge.getFirst());
	Node second = getTargetNode(edge.getSecond());
	if (first == null || second == null)
	    return null;
	return target.getEdge(first, second);
    }

    /**
     * Get the source edge associated with a particular target edge.
     *
     * @param edge the target edge
     * @return the source edge, or <code>null</code> if there is no mapping
     */
    public Edge getSourceEdge(Edge edge) {
	Node first = getSourceNode(edge.getFirst());
	Node second = getSourceNode(edge.getSecond());
	if (first == null || second == null)
	    return null;
	return source.getEdge(first, second);
    }

    /**
     * Get the source graph.
     *
     * @return the source graph
     */
    public Graph getSourceGraph() { return source; }

    /**
     * Get the target graph.
     *
     * @return the target graph
     */
    public Graph getTargetGraph() { return target; }

}
