package com.moallemi.math.graph;

import java.util.*;

/**
 * Iterator for all paths in a graph starting at a given node.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-08-31 08:40:14 $
 */
public class AllPathsIterator implements Iterator {
    private Graph graph;
    private Graph path;
    private int[] index;
    private int[] maxIndex;
    private int lastOffset;
    private int initialModCount;
    private boolean currentHasNext;

    /**
     * Constructor.
     *
     * @param graph the graph
     * @param start the starting node
     * @param maxLength the maximum length for any path returned
     */
    public AllPathsIterator(Graph graph, Node start, int maxLength) {
	this.graph = graph;
	initialModCount = graph.getModificationCount();

	if (maxLength < 1)
	    throw new IllegalArgumentException("length must be at least 1");

	path = new Graph();
	path.beginModification();
	path.addNode(start);
	path.endModification();

	index = new int [maxLength];
	maxIndex = new int [index.length];
	index[0] = 0;
	maxIndex[0] = 1;
	lastOffset = 0;
	if (index.length > 1) {
	    index[1] = -1;
	    maxIndex[1] = graph.getNodeDegree(start);
	    lastOffset = 1;
	}

	currentHasNext = true;
    }

    /**
     * Do we have additional paths remaining?
     *
     * @return <code>true</code> if additional paths remain,
     * <code>false</code> otherwise.
     * @throws ConcurrentModificationException if the graph has been modified
     */
     public boolean hasNext() {
	if (initialModCount != graph.getModificationCount())
	    throw new ConcurrentModificationException();
	fetchNext();
	return currentHasNext;
    }

    /**
     * Return the next path in the search. The path is returned as a
     * <code>Graph</code>, with an ordered list of nodes and
     * corresponding edges. Note that the same <code>Graph</code>
     * object is returned over different calls, it is modified in
     * place to reflect new paths.
     *
     * @return the next path
     * @throws ConcurrentModificationException if the graph has been modified
     * @throws NoSuchElementException if there are no additional nodes
     */
    public Object next() {
	if (initialModCount != graph.getModificationCount())
	    throw new ConcurrentModificationException();
	fetchNext();
	if (currentHasNext) {
	    currentHasNext = false;
	    return path;
	}
	else
	    throw new NoSuchElementException();
    }

    private void fetchNext() {
	if (currentHasNext || lastOffset <= 0)
	    return;

	path.beginModification();
	do {
	    // remove the last node added, if necessary
	    int pathLength = path.getNodeCount();
	    while (--pathLength >= lastOffset) {
		path.removeNode(pathLength);
		// edge implicitly removed
	    }

	    // increment the counter
	    index[lastOffset]++;

	    // see if a new last node is available
	    if (index[lastOffset] < maxIndex[lastOffset]) {
		Node prevNode = path.getNode(lastOffset-1);
		Node nextNode = graph.getConnectedNode(prevNode,
						       index[lastOffset]);
		
		// make sure node hasn't already been used
		if (!path.containsNode(nextNode)) {
		    Edge nextEdge = graph.getEdge(prevNode,
						  index[lastOffset]);
		    path.addNode(nextNode);
		    path.addEdge(nextEdge);

		    // make sure to investigate further nodes along this path
		    if (lastOffset + 1 < index.length) {
			lastOffset++;
			index[lastOffset] = -1;
			maxIndex[lastOffset] = graph.getNodeDegree(nextNode);
		    }
		    currentHasNext = true;
		}
	    }
	    else {
		// backtrack
		lastOffset--;
	    }

	} while (!currentHasNext && lastOffset > 0);
	path.endModification();
    }

    /**
     * Remove. Unsupported.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }


}
