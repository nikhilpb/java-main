package com.moallemi.math.graph;

/**
 * A function from the nodes of a graph to a real vector space.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public interface NodeFunction {
    
    /**
     * The getDimension of this node function.
     *
     * @return the getDimension
     */
    public int getDimension();

    /**
     * Get the value of the function at a node.
     *
     * @param graph the graph
     * @param node the node at which to evaluate the function
     * @param index the getDimension of interest
     * @return the value of the function at the specified node and
     * getDimension
     */
    public double getValue(Graph graph, Node node, int index);

}
