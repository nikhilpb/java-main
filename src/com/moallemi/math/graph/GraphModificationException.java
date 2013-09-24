package com.moallemi.math.graph;

/**
 * An exception generated when a graph that is not in the modification
 * state is being modified.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class GraphModificationException extends RuntimeException {
    public GraphModificationException() { super(); }
    public GraphModificationException(Throwable t) { super(t); }
    public GraphModificationException(String m, Throwable t) { super(m, t); }
    public GraphModificationException(String m) { super(m); }
}
