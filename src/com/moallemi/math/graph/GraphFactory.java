package com.moallemi.math.graph;

import java.util.*;

import com.moallemi.math.Distributions;
import com.moallemi.util.data.IntArray;

/**
 * Construct common types of graphs.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.12 $, $Date: 2007-11-02 13:14:48 $
 */
public class GraphFactory {

    /**
     * Build a lattice.
     *
     * @param nodesPerAxis number of nodes per axis
     * @param dimension getDimension
     * @param connectivity distance to maintain connectivity
     * @param isTorus should the edges wrap?
     * @param isL1Norm should an L1 norm be used? (otherwise L-\infty)
     * @return the graph
     */
    public static Graph buildLattice(int nodesPerAxis, 
                                     int dimension,
                                     int connectivity,
                                     boolean isTorus,
                                     boolean isL1Norm) {
        Graph graph = new Graph();
        graph.beginModification();
        // create a list of points in the lattice
        int totalPoints = 1;
        for (int i = 0; i < dimension; i++)
            totalPoints *= nodesPerAxis;
        Map<IntArray,Node> coordMap 
            = new HashMap<IntArray,Node> (2*totalPoints + 1);

        IntArray[] points = new IntArray [totalPoints];
        Node[] nodes = new Node [totalPoints];
        int[] point = new int [dimension];
        int cnt = 0;
        while (true) {
            int[] copy = new int [dimension];
            System.arraycopy(point, 0, copy, 0, dimension);
            points[cnt] = new IntArray(copy);
            Node node = new Node();
            graph.addNode(node);
            nodes[cnt] = node;
            coordMap.put(points[cnt], node);

            cnt++;

            boolean hasNext = false;
            for (int i = 0; i < dimension && !hasNext; i++) {
                point[i]++;
                if (point[i] < nodesPerAxis)
                    hasNext = true;
                else
                    point[i] = 0;
            }
            if (!hasNext)
                break;
        }
        if (cnt != totalPoints)
            throw new IllegalStateException();

        // connect the points in the lattice
        int[] offset = new int [dimension];
        int[] copy = new int [dimension];
        for (int i = 0; i < totalPoints; i++) {
            IntArray p = points[i];
            Arrays.fill(offset, -connectivity);
            while (true) {
                for (int d = 0; d < dimension; d++) {
                    copy[d] = (p.get(d) + offset[d]) % nodesPerAxis;
                    if (copy[d] < 0)
                        copy[d] = nodesPerAxis + copy[d];
                }
                IntArray q = new IntArray(copy);
                Node dest = coordMap.get(q);
                if (dest == null)
                    throw new IllegalStateException("unknown coord: " 
                                                    + q.toString());

                int dist;
                if (isL1Norm)
                    dist = isTorus 
                        ? p.distanceL1Modular(q, nodesPerAxis)
                        : p.distanceL1(q);
                else
                    dist = isTorus 
                        ? p.distanceLInfModular(q, nodesPerAxis)
                        : p.distanceLInf(q);
                if (dist <= connectivity 
                    && graph.getConnectedNodeOffset(nodes[i], dest) < 0) 
                    graph.addEdge(new Edge(nodes[i], dest));

                boolean hasNext = false;
                for (int d = 0; d < dimension && !hasNext; d++) {
                    offset[d]++;
                    if (offset[d] <= connectivity)
                        hasNext = true;
                    else
                        offset[d] = -connectivity;
                }
                if (!hasNext)
                    break;
            }
                
        }

        graph.endModification();
        return graph;
    }

    /**
     * Build a "lollipop" graph.
     *
     * @param nodeCount total number of nodes (must be at least 2)
     * @return the graph
     */
    public static Graph buildLollipop(int nodeCount) {
	// construct a ring
	Graph graph = buildLattice(nodeCount - 1,
				   1,
				   1,
				   true,
				   true);
	// add a single node
	graph.beginModification();
	Node node = new Node();
	graph.addNode(node);
	graph.addEdge(new Edge(node, graph.getNode(0)));
	graph.endModification();
	return graph;
    }

    /*
     * Build a four-node square with a diagonal.
     *
     * @return the graph
     */
    public static Graph buildSquareWithDiagonal() {
	// construct a ring
	Graph graph = buildLattice(4,
				   1,
				   1,
				   true,
				   true);
	// add a diagonal edge
	graph.beginModification();
	graph.addEdge(new Edge(graph.getNode(0), graph.getNode(2)));
	graph.endModification();
	return graph;
    }


    /**
     * Build a complete graph.
     *
     * @param nodeCount total number of nodes
     * @return the graph
     */
    public static Graph buildComplete(int nodeCount) {
        Graph graph = new Graph();
        graph.beginModification();
        for (int i = 0; i < nodeCount; i++) 
            graph.addNode(new Node());
        for (int i = 0; i < nodeCount; i++) 
            for (int j = 0; j < i; j++) 
                graph.addEdge(new Edge(graph.getNode(i), graph.getNode(j)));
	graph.endModification();
	return graph;
    }

    /**
     * Build a random geometric graph, i.e. sample points in a unit
     * disk and connect them if the are within a certain radius.
     *
     * @param nodeCount number of nodes
     * @param radius the radius
     * @param random randomness source
     * @param forceConnected force the graph to be connected
     * @return the graph
     */
    public static Graph buildGeometric2D(int nodeCount,
                                         double radius,
                                         Random random,
                                         boolean forceConnected) 
    {
        double[] x = new double [nodeCount];
        double[] y = new double [nodeCount];
        double r2 = radius*radius;

        boolean done = false;
        Graph graph = new Graph();
        graph.beginModification();

        do {
            // sample the coordinates
            for (int i = 0; i < nodeCount; i++) {
                do {
                    x[i] = 2.0*random.nextDouble() - 1.0;
                    y[i] = 2.0*random.nextDouble() - 1.0;
                } while (x[i]*x[i] + y[i]*y[i] >= 1.0);
            }

            // build the graph
            for (int i = 0; i < nodeCount; i++) {
                graph.addNode(new Node());
                for (int j = 0; j < i; j++) {
                    double dx = x[i] - x[j];
                    double dy = y[i] - y[j];
                    if (dx*dx + dy*dy <= r2) 
                        graph.addEdge(new Edge(graph.getNode(i),
                                               graph.getNode(j)));
                }
            }

            // test connectivity
            if (forceConnected && 
                (new DFS(graph)).compute().getConnectedComponentCount()
                != 1) {
                done = false;
                graph.removeAllNodes();
            }
            else 
                done = true;
        } while (!done);

        graph.endModification();
        return graph;
    }

    /**
     * Build a random geometric graph with a target average
     * degree. Nodes sample points in a unit disk and connect them if
     * the are within a certain radius. The radius is decreased until
     * the average degree is less than the target.
     *
     * @param nodeCount number of nodes
     * @param avgDegree the average degree
     * @param random randomness source
     * @return the graph
     */
    public static Graph buildGeometric2DAvgDegree(int nodeCount,
                                                  double avgDegree,
                                                  Random random)
    {
        double[] x = new double [nodeCount];
        double[] y = new double [nodeCount];

        // sample the coordinates
        for (int i = 0; i < nodeCount; i++) {
            do {
                x[i] = 2.0*random.nextDouble() - 1.0;
                y[i] = 2.0*random.nextDouble() - 1.0;
            } while (x[i]*x[i] + y[i]*y[i] >= 1.0);
        }
        // compute distances between all pairs
        double[] distances = new double [nodeCount*(nodeCount - 1)/2];
        int cnt = 0;
        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < i; j++) {
                double dx = x[i] - x[j];
                double dy = y[i] - y[j];
                distances[cnt++] = dx*dx + dy*dy;
            }
        }

        // sort the array, figure out the proper threshold
        Arrays.sort(distances);
        int index = (int) (0.5 * ((double) nodeCount) * avgDegree);
        double threshold = distances[index];

        // build the graph
        Graph graph = new Graph();
        graph.beginModification();
        for (int i = 0; i < nodeCount; i++) {
            graph.addNode(new Node());
            for (int j = 0; j < i; j++) {
                double dx = x[i] - x[j];
                double dy = y[i] - y[j];
                if (dx*dx + dy*dy <= threshold)
                    graph.addEdge(new Edge(graph.getNode(i),
                                           graph.getNode(j)));
            }
        }
        graph.endModification();
        return graph;
    }


    /**
     * Build a Bernoulli random geometric graph, i.e. create edges
     * with IID probability.
     *
     * @param nodeCount number of nodes
     * @param p the edge probability
     * @param random randomness source
     * @param forceConnected force the graph to be connected
     * @return the graph
     */
    public static Graph buildBernoulli(int nodeCount,
                                       double p,
                                       Random random,
                                       boolean forceConnected) 
    {
        boolean done = false;
        Graph graph = new Graph();
        graph.beginModification();

        do {
            // build the graph
            for (int i = 0; i < nodeCount; i++) {
                graph.addNode(new Node());
                for (int j = 0; j < i; j++) {
                    if (random.nextDouble() < p)
                        graph.addEdge(new Edge(graph.getNode(i),
                                               graph.getNode(j)));
                }
            }

            // test connectivity
            if (forceConnected && 
                (new DFS(graph)).compute().getConnectedComponentCount()
                != 1) {
                done = false;
                graph.removeAllNodes();
            }
            else 
                done = true;
        } while (!done);

        graph.endModification();
        return graph;
    }

    /**
     * Build a Bernoulli random geometric graph, i.e. create edges
     * with IID probability. This code is faster when nodeCount is
     * large and nodeCount * p is constant.
     *
     * @param nodeCount number of nodes
     * @param p the edge probability
     * @param random randomness source
     * @param forceConnected force the graph to be connected
     * @return the graph
     */
    public static Graph buildBernoulliFast(int nodeCount,
                                           double p,
                                           Random random,
                                           boolean forceConnected) 
    {
        boolean done = false;
        Graph graph = new Graph();
        graph.beginModification();

        do {
            // build the graph
            for (int i = 0; i < nodeCount; i++) {
                Node node = new Node();
                graph.addNode(node);

                if (i == 0)
                    continue;

                // figure out how many edges by drawing from a
                // binomial distribution
                int count = Distributions.nextBinomial(random, i, p);

                // figure out which edges by sampling count things 
                // without replacement
                while (count > 0) {
                    int j = random.nextInt(i);
                    Node other = graph.getNode(j);
                    if (!graph.areConnected(other, node)) {
                        graph.addEdge(new Edge(node, other));
                        count--;
                    }
                }
            }

            // test connectivity
            if (forceConnected && 
                (new DFS(graph)).compute().getConnectedComponentCount()
                != 1) {
                done = false;
                graph.removeAllNodes();
            }
            else 
                done = true;
        } while (!done);

        graph.endModification();
        return graph;
    }

}
