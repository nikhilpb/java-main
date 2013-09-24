package com.moallemi.math.graph;

import java.util.*;

import Jama.Matrix;
import com.moallemi.math.VectorNd;

/**
 * A class for computing graph potential.
 * See Golender et.al., "Graph Potentials Method", J.Chem.Inf.Comput. Sci.
 * 1981, 21, 196-204.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class GraphPotential {
    // the graph
    private Graph graph;
    // the determinant
    private double determinant;
    // currents, potentials
    private VectorNd[] current;
    private VectorNd[] potential;


    /**
     * A tolerance to be used for potential/current/determinant 
     * comparison.
     */
    public static final double TOLERANCE = 1e-6;

    /**
     * Constructor.
     *
     * @param graph the graph to construct a potential for
     * @param function a node function defining current sources
     */
    public GraphPotential(Graph graph, NodeFunction function) {
	this.graph = graph;
	int n = graph.getNodeCount();
	int m = function.getDimension();

	// construct the G matrix
	Matrix G = new Matrix(n, n);
	for (int i = 0; i < n; i++) {
	    Node node = graph.getNode(i);
	    // diagonals set to node degree + 1
	    G.set(i, i, graph.getNodeDegree(node) + 1);
	    // set off-diagonal elements to -1 where there is an edge
	    for (Iterator j = graph.getConnectedNodeIterator(node);
		 j.hasNext(); )
		G.set(i, graph.getNodeIndex((Node) j.next()), -1.0);
	}

	// compute determinant
	determinant = G.det();

	// construct the current vectors
	current = new VectorNd [n];
	Matrix C = new Matrix(n, m);
	for (int i = 0; i < n; i++) {
	    Node node = graph.getNode(i);
	    current[i] = new VectorNd(m);
	    for (int j = 0; j < m; j++)
		current[i].set(j, function.getValue(graph, node, j));
	    current[i].toMatrixRow(C, i);
	}

	// solve for potentials
	Matrix U = G.solve(C);
	potential = new VectorNd [n];
	for (int i = 0; i < n; i++) {
	    potential[i] = new VectorNd(m);
	    potential[i].extractMatrixRow(U, i);
	}
    }

    /**
     * Get the current source at a particular node.
     *
     * @param node the node
     * @return the current source
     */
    public VectorNd getCurrent(Node node) {
	return current[graph.getNodeIndex(node)];
    }

    /**
     * Get the current source at a particular node.
     *
     * @param i the index of the node
     * @return the current source
     */
    public VectorNd getCurrent(int i) {
	return current[i];
    }

    /**
     * Get the potential at a particular node.
     *
     * @param node the node
     * @return the potential
     */
    public VectorNd getPotential(Node node) {
	return potential[graph.getNodeIndex(node)];
    }

    /**
     * Get the potential at a particular node.
     *
     * @param i the index of the node
     * @return the potential
     */
    public VectorNd getPotential(int i) {
	return potential[i];
    }

    /**
     * Get the determinant of the G-matrix, which is a topological 
     * invariant.
     *
     * @return the determinant
     */
    public double getDeterminant() { return determinant; }

    /**
     * Get the underlying graph.
     *
     * @return the graph
     */
    public Graph getGraph() { return graph; }
}
