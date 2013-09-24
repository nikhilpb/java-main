package com.moallemi.avg;

import java.util.*;

import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.data.Pair;

public class ConsensusPropagationMeanAverager 
    extends ConsensusPropagationAverager 
{
    public ConsensusPropagationMeanAverager(Graph graph, 
					    double[] yValues,
					    double beta) 
    {
	super(graph, yValues, beta);
    }

    public static final double TOLERANCE = 1.0e-10;

    public void reset() {
	super.reset();

	boolean converged = false;
	while (!converged) {
	    converged = true;

// 	    for (Iterator i = edgeMap.entrySet().iterator(); i.hasNext(); ) {
// 		Map.Entry e = (Map.Entry) i.next();
// 		Pair p = (Pair) e.getKey();
// 		EdgeInfo info = (EdgeInfo) e.getValue();
// 		Node source = (Node) p.getFirst();
// 		Node dest = (Node) p.getSecond();

// 		// update outgoing K message
// 		int sourceDegree = graph.getNodeDegree(source);
// 		double norm = 1.0;
// 		for (int c = 0; c < sourceDegree; c++) {
// 		    Node other = graph.getConnectedNode(source, c);
// 		    if (other == dest)
// 			continue;
// 		    EdgeInfo thisInfo = 
// 			(EdgeInfo) edgeMap.get(new Pair(other, source));
// 		    norm += thisInfo.K;
// 		}
// 		double newK = (1.0 / ((norm/beta) + 1.0)) * norm;
// 		if (Math.abs(info.K - newK) > TOLERANCE) 
// 		    converged = false;
// 		info.K = newK;
// 	    }
// 	}

//   	for (int i = 0; i < yValues.length; i++) {
//   	    Node source = graph.getNode(i);
//   	    int sourceDegree = graph.getNodeDegree(source);
//   	    for (int j = 0; j < sourceDegree; j++) {
//   		Node dest = graph.getConnectedNode(source, j);
//   		EdgeInfo info = (EdgeInfo) edgeMap.get(new Pair(source, dest));
// 		info.mu = yValues[i];
// 	    }
// 	}

// 	for (int i = 0; i < yValues.length; i++)
// 	    updateEstimate(i);
        }
    }
}
