package com.moallemi.minsum;

import java.util.*;

import com.moallemi.math.graph.*;

/**
 * A generic class for keeping track of directed edges over an
 * undirected graph. Useful for implementing message passing
 * algorithms.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2006-08-31 08:40:14 $
 */
public class DirectedEdgeSet {
    
    // underlying graph
    private Graph graph;
    // collection of all directed edge info objects
    private DirectedEdgeInfo[] edgeList;
    // list of  "connected" edges (incoming min-sum messages) for each edge
    private DirectedEdgeInfo[][] connectedEdgeIndexList;
    // map associating a node and connected node offset to a directed edge
    private DirectedEdgeInfo[][] nodeIncomingMap;
    // map associating a node and connected node offset to a directed edge
    private DirectedEdgeInfo[][] nodeOutgoingMap;

    /**
     * Constructor.
     *
     * @param graph the underlying graph
     */
    public DirectedEdgeSet(Graph graph) {
        this.graph = graph;

        int edgeCount = graph.getEdgeCount();
        int directedEdgeCount = 2*edgeCount;
        edgeList = new DirectedEdgeInfo [directedEdgeCount];
        connectedEdgeIndexList = new DirectedEdgeInfo [directedEdgeCount][];
        int nodeCount = graph.getNodeCount();
        nodeIncomingMap = new DirectedEdgeInfo [nodeCount][];
        nodeOutgoingMap = new DirectedEdgeInfo [nodeCount][];

        int cnt = 0;
        for (int i = 0; i < nodeCount; i++) {
            Node node = graph.getNode(i);
            int degree = graph.getNodeDegree(node);
            nodeIncomingMap[i] = new DirectedEdgeInfo [degree];
            for (int ni = 0; ni < degree; ni++) {
                Node other = graph.getConnectedNode(node, ni);
                DirectedEdgeInfo edgeInfo = 
                    new DirectedEdgeInfo(graph,
                                         other,
                                         node,
                                         cnt);
                edgeList[cnt] = edgeInfo;
                nodeIncomingMap[i][ni] = edgeInfo;
                cnt++;
            }
        }
        if (directedEdgeCount != cnt)
            throw new IllegalStateException("edge counts do not match up");
        for (int i = 0; i < nodeCount; i++) {
            Node node = graph.getNode(i);
            int degree = graph.getNodeDegree(node);
            nodeOutgoingMap[i] = new DirectedEdgeInfo [degree];
            for (int ni = 0; ni < degree; ni++) {
                Node other = graph.getConnectedNode(node, ni);
                int j = graph.getNodeIndex(other);
                int nj = graph.getConnectedNodeOffset(other, node);
                nodeOutgoingMap[i][ni] = nodeIncomingMap[j][nj];
            }
        }

        for (int ij = 0; ij < directedEdgeCount; ij++) {
            DirectedEdgeInfo edgeInfo = edgeList[ij]; 	
            Node first = edgeInfo.getFirst();
            int firstIndex = edgeInfo.getFirstNodeIndex();
            Node second = edgeInfo.getSecond();
            int firstDegree = graph.getNodeDegree(first);
            connectedEdgeIndexList[ij] = new DirectedEdgeInfo [firstDegree-1];
            int ocnt = 0;
            for (int ni = 0; ni < firstDegree; ni++) {
                Node other = graph.getConnectedNode(first, ni);
                if (!other.equals(second)) 
                    connectedEdgeIndexList[ij][ocnt++]
                        = nodeIncomingMap[firstIndex][ni];
            }
            if (ocnt != connectedEdgeIndexList[ij].length)
                throw new IllegalStateException("connected edge "
                                                +"count incorrect");
        }
    }


        
    public int getDirectedEdgeCount() { return edgeList.length; }
    public DirectedEdgeInfo getDirectedEdge(int edgeIndex) {
	return edgeList[edgeIndex];
    }
    public DirectedEdgeInfo getIncomingEdge(int nodeIndex, int offset) {
	return nodeIncomingMap[nodeIndex][offset];
    }
    public int getIncomingEdgeIndex(int nodeIndex, int offset) {
	return nodeIncomingMap[nodeIndex][offset].getDirectedEdgeIndex();
    }
    public DirectedEdgeInfo getOutgoingEdge(int nodeIndex, int offset) {
	return nodeOutgoingMap[nodeIndex][offset];
    }    
    public int getOutgoingEdgeIndex(int nodeIndex, int offset) {
	return nodeOutgoingMap[nodeIndex][offset].getDirectedEdgeIndex();
    }
    public int getConnectedEdgeDegree(int directedEdgeIndex) {
        return connectedEdgeIndexList[directedEdgeIndex].length;
    }
    public DirectedEdgeInfo getConnectedEdge(int directedEdgeIndex, 
                                             int offset) 
    {
        return connectedEdgeIndexList[directedEdgeIndex][offset];
    }
    public int getConnectedEdgeIndex(int directedEdgeIndex, int offset) {
        return connectedEdgeIndexList[directedEdgeIndex][offset]
            .getDirectedEdgeIndex();
    }
}

    