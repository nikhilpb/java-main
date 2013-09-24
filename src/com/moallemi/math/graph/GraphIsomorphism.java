package com.moallemi.math.graph;

import java.util.*;
import com.moallemi.util.data.BiDirectionalMap;

/**
 * Compute isomorphisms between two maps using the graph potential
 * method.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class GraphIsomorphism {
    // the computed isomorphisms
    private GraphMap[] isomorphismMaps;
    // the source, target graphs
    private Graph sourceGraph, targetGraph;
    // the comparator
    private GraphComparator comparator;

    /**
     * Constructor.
     *
     * @param sourceGraph the source graph
     * @param targetGraph the target graph
     */
    public GraphIsomorphism(GraphPotential sourceGP,
			    GraphPotential targetGP,
			    GraphComparator comparator) 
    {
	this.sourceGraph = sourceGP.getGraph();
	this.targetGraph = targetGP.getGraph();
	this.comparator = comparator;
	isomorphismMaps = computeIsomorphisms(sourceGP, 
					      targetGP);
    }

    /**
     * Get the number of isomorphisms between the source and target.
     *
     * @return the number of isomorphisms
     */
    public int getIsomorphismCount() { 
	return isomorphismMaps != null ? isomorphismMaps.length : 0;
    }

    /**
     * Get an isomorphism.
     *
     * @param i index of the isomorphism
     * @return the isomorphism
     */
    public GraphMap getIsomorphism(int i) { return isomorphismMaps[i]; }

    private GraphMap[] computeIsomorphisms(GraphPotential sourceGP,
					   GraphPotential targetGP)
    {
	// if there are a different number of nodes or edges, clearly 
	// there are no isomorphisms
	if (sourceGraph.getNodeCount() != targetGraph.getNodeCount()
	    || sourceGraph.getEdgeCount() != targetGraph.getEdgeCount())
	    return null;

	int n = sourceGraph.getNodeCount();

	// determinants must match, otherwise no isomorphism possible
	if (Math.abs(1.0 - sourceGP.getDeterminant()/targetGP.getDeterminant())
	    > GraphPotential.TOLERANCE)
	    return null;

	// construct a node equivalence map
	Map equivMap = constructEquivalenceMap(sourceGP, targetGP);
	if (equivMap == null)
	    return null;

	// lay out the source graph nodes via depth first search
	// this should make things faster as nearby nodes will be assigned
	// at times close to each other, forcing conflicts to appear 
	// earlier than otherwise
	Node[] sourceNodes = new Node [n];
	int cnt = 0;
	for (Iterator i = new DFS(sourceGraph); i.hasNext(); ) 
	    sourceNodes[cnt++] = (Node) i.next();
	
	// try and go through all possibilities in the equivalence map
	BiDirectionalMap nodeMap = new BiDirectionalMap();
	// construct a stack to allow backtracing
	int[] stack = new int [n];
	Arrays.fill(stack, -1);
	int index = 0;
	ArrayList isomorphisms = new ArrayList();
    outer:
	while (index >= 0) {
	    if (index < stack.length) {
		// we have nodes left to assign
		// try and assign this node to a node in the target
		Node source = sourceNodes[index];
		nodeMap.removeKey(source); //remove existing node assignment (if any)
		Node[] targetChoices = (Node[]) equivMap.get(source);
		while (++stack[index] < targetChoices.length) {
		    Node target = targetChoices[stack[index]];
		    if (!hasConflict(nodeMap, source, target)) {
			// non-conflicting assignment found
			nodeMap.put(source, target);
			index++;
			continue outer;
		    }
		}
		
		// no non-conflicting assignment was found, must backtrace
		stack[index] = -1;
		index--;
	    }
	    else {
		// all nodes have been assigned successfully, we have a match
		GraphMap map = new GraphMap(sourceGraph, 
					    targetGraph,
					    nodeMap);
		if (isConsistent(map))
		    isomorphisms.add(map);
		index--;
	    }
	}

	return isomorphisms.isEmpty()
	    ? null
	    : (GraphMap[]) isomorphisms.toArray(new GraphMap [0]);
    }

    // build a map of equivalence classes using graph potentials
    // will return <code>null</code> if such a map can't be constructed
    private Map constructEquivalenceMap(GraphPotential sourceGP,
					GraphPotential targetGP) 
    {
	int n = sourceGraph.getNodeCount();

	// build a map of equivalence classes using the potentials
	Map equivMap = new HashMap();
	for (int i = 0; i < n; i++) {
	    ArrayList equivNodes = new ArrayList();
	    for (int j = 0; j < n; j++) {
		if (sourceGP.getPotential(i)
		    .distanceL1(targetGP.getPotential(j)) 
		    < GraphPotential.TOLERANCE
		    && sourceGP.getCurrent(i)
		    .distanceL1(targetGP.getCurrent(j)) 
		    < GraphPotential.TOLERANCE) 
		    equivNodes.add(targetGraph.getNode(j));
	    }

	    // make sure we have at least one equivalent
	    if (equivNodes.isEmpty())
		return null;

	    equivMap.put(sourceGraph.getNode(i),
			 equivNodes.toArray(new Node [0]));
	}

	return equivMap;
    }


    // Check that a new assignment of <code>source</code> to
    // <code>target</code> does not create conflicts with 
    // existing assignments in <code>nodeMap</code>.
    private boolean hasConflict(BiDirectionalMap nodeMap,
				Node source,
				Node target) 
    {
	// make sure neither node has been used before
	if (nodeMap.containsKey(source) || nodeMap.containsValue(target))
	    return true;

	// make sure that, given current assignments, every connected
	// edge in the target graph is connected in the source graph
	for (Iterator i = targetGraph.getConnectedNodeIterator(target);
	     i.hasNext(); ) {
	    Node targetNeighbor = (Node) i.next();
	    Node mapped = (Node) nodeMap.getByValue(targetNeighbor);
	    if (mapped != null && !sourceGraph.areConnected(source, mapped))
		return true;
	}

	// make sure that, given current assignments, every connected
	// edge in the source graph is connected in the target graph
	for (Iterator i = sourceGraph.getConnectedNodeIterator(source);
	     i.hasNext(); ) {
	    Node sourceNeighbor = (Node) i.next();
	    Node mapped = (Node) nodeMap.getByKey(sourceNeighbor);
	    if (mapped != null && !targetGraph.areConnected(target, mapped))
		return true;
	}
	return false;
    }


    // check if a map is consistent with the comparator
    private boolean isConsistent(GraphMap map) {
	for (Iterator i = sourceGraph.getNodeIterator(); i.hasNext(); ) {
	    Node node = (Node) i.next();
	    if (!comparator.nodesEqual(map, node))
		return false;
	}
	for (Iterator i = sourceGraph.getEdgeIterator(); i.hasNext(); ) {
	    Edge edge = (Edge) i.next();
	    if (!comparator.edgesEqual(map, edge))
		return false;
	}
	return true;
    }

}
