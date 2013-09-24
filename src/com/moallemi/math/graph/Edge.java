package com.moallemi.math.graph;

import com.moallemi.util.data.*;

/**
 * A generic class for graph edges.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $}
 */
public class Edge {
    // the nodes participating in this edge
    private Node first, second;
    // the attribute map
    private AttributeMap attributeMap = new AttributeMap();

    /**
     * Constructs an edge connecting two nodes
     *
     * @param first the first <code>Node</code> connected to this edge
     * @param second the second <code>Node</code> connected to this edge
     */
    public Edge(Node first, Node second)
    {
	if (first == null || second == null)
	    throw new IllegalArgumentException("cannot construct edge "
					       + "with null nodes");
	if (first == second)
	    throw new IllegalArgumentException("cannot construct edge "
					       + "between identical nodes");

	this.first = first;
	this.second = second;
    }

    /**
     * Get the first node connected to this edge
     *
     * @return the first <code>Node</code> connected to this edge
     */
    public Node getFirst() { return first; }

    /**
     * Get the second node connected to this edge
     *
     * @return the second <code>Node</code> connected to this edge
     */
    public Node getSecond() { return second; }

    /**
     * Get the node of this edge other than the one passed in
     *
     * @param node the <code>Node</code> to check against
     * @return the other <code>Node</code>
     */
    public Node getOther(Node node)
    {
	Node other = 
	    node == first ?
	    second :
	    node == second ?
	    first :
	    null;
	
	if (other == null)
	    throw new IllegalArgumentException("specified node not a member " +
					       "of this edge");

	return (other);
    }

    /**
     * Get the attribute map for this edge.
     *
     * @return the attribute map
     */
    public AttributeMap getAttributeMap() { return attributeMap; }

}
