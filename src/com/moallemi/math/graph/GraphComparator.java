package com.moallemi.math.graph;

/**
 * An interface for comparing single nodes or edges between 2 mapped graphs.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public interface GraphComparator {

    /**
     * Determine if a node and it's mapped counterpart are equivalent.
     *
     * @param map the graph map
     * @param node the source node
     */
    public boolean nodesEqual(GraphMap map, Node node);


    /**
     * Determine if a edge and it's mapped counterpart are equivalent.
     *
     * @param map the graph map
     * @param edge the source edge
     */
    public boolean edgesEqual(GraphMap map, Edge edge);
}
