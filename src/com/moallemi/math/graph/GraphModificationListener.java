package com.moallemi.math.graph;

/**
 * A listener for graph modification operations.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public interface GraphModificationListener extends java.util.EventListener
{
    /**
     * Called when modification of a graph has started.
     * Graph will be in modifiable state when this method is called.
     *
     * @param graph graph being modified
     */
    public void graphModificationStarted(Graph graph);

    /**
     * Called when modification of a graph has finished/.
     * Graph will be in modifiable state when this method is called.
     *
     * @param graph graph being modified
     */
    public void graphModificationFinished(Graph graph);
}
