package com.moallemi.minsum;

import com.moallemi.math.graph.*;

/**
 * A generic class for directed graph edges.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2006-08-31 08:40:14 $}
 */
public class DirectedEdgeInfo {
    private Node first, second;
    private int firstNodeIndex, secondNodeIndex;
    private int firstNodeOffset, secondNodeOffset;
    private int directedEdgeIndex;

    public DirectedEdgeInfo(Graph graph, 
                            Node first, 
                            Node second, 
                            int directedEdgeIndex)
    {
        this.first = first;
        this.second = second;
        firstNodeIndex = graph.getNodeIndex(first);
        firstNodeOffset = graph.getConnectedNodeOffset(second, first);
        secondNodeIndex = graph.getNodeIndex(second);
        secondNodeOffset = graph.getConnectedNodeOffset(first, second);
        this.directedEdgeIndex = directedEdgeIndex;
    }

    public Node getFirst() { return first; }
    public int getFirstNodeIndex() { return firstNodeIndex; }
    public int getFirstNodeOffset() { return firstNodeOffset; }
    public Node getSecond() { return second; }
    public int getSecondNodeIndex() { return secondNodeIndex; }
    public int getSecondNodeOffset() { return secondNodeOffset; }
    public int getDirectedEdgeIndex() { return directedEdgeIndex; }
    
}
