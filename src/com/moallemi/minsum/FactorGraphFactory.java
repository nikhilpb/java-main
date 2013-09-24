package com.moallemi.minsum;

import java.util.*;
import com.moallemi.math.Shuffle;

public class FactorGraphFactory {

    public static void buildCompleteGraph(FactorGraph graph)
    {
        int varCount = graph.getVariableCount();
        int factorCount = graph.getFactorCount();

        for (int v = 0; v < varCount; v++) 
            for (int f = 0; f < factorCount; f++)
                graph.addEdge(v, f);

        graph.compress();
    }

    public static void buildRandomRegularGraphByVariable(Random random,
                                                         FactorGraph graph, 
                                                         int vDegree) 
    {
        int varCount = graph.getVariableCount();
        int factorCount = graph.getFactorCount();

        // generate a random permutation
        int[] permutation = new int [varCount * vDegree];
        int cnt = 0;
        while (cnt < permutation.length) {
            for (int f = 0; f < factorCount && cnt < permutation.length; f++)
                permutation[cnt++] = f;
        }
        Shuffle.shuffle(random, permutation);
       
        // assign edges, ignores double edges
        cnt = 0;
        for (int v = 0; v < varCount; v++) 
            for (int i = 0; i < vDegree; i++) 
                graph.addEdge(v, permutation[cnt++]);

        graph.compress();
    }

    public static void buildRandomRegularGraphByFactor(Random random,
                                                       FactorGraph graph, 
                                                       int fDegree) 
    {
        int varCount = graph.getVariableCount();
        int factorCount = graph.getFactorCount();

        // generate a random permutation
        int[] permutation = new int [factorCount * fDegree];
        int cnt = 0;
        while (cnt < permutation.length) {
            for (int v = 0; v < varCount && cnt < permutation.length; v++)
                permutation[cnt++] = v;
        }
        Shuffle.shuffle(random, permutation);
       
        // assign edges, ignores double edges
        cnt = 0;
        for (int f = 0; f < factorCount; f++)
            for (int i = 0; i < fDegree; i++) 
                graph.addEdge(permutation[cnt++], f);

        graph.compress();
    }

    public static void buildExactRandomRegularGraphByVariable(Random random,
                                                              FactorGraph graph,
                                                              int vDegree,
                                                              int iterCount) {
        int varCount = graph.getVariableCount();
        int factorCount = graph.getFactorCount();

        int[] vDegreeArray = new int [varCount];
        Arrays.fill(vDegreeArray, vDegree);
        int[] fDegreeArray = new int [factorCount];
        int totalHalfEdges = varCount * vDegree;
        for (int cnt = 0; cnt < totalHalfEdges; ) {
            for (int f = 0; f < factorCount && cnt < totalHalfEdges; f++) {
                fDegreeArray[f]++;
                cnt++;
            }
        }

        for (int iter = 0; iter < iterCount; iter++) {
            if (sampleRandomGraphExact(random, graph, 
                                       vDegreeArray, fDegreeArray))
                return;
        }

        throw new IllegalStateException("unable to sample graph");
    }

    public static void buildExactRandomRegularGraphByFactor(Random random,
                                                            FactorGraph graph,
                                                            int fDegree,
                                                            int iterCount) {
        int varCount = graph.getVariableCount();
        int factorCount = graph.getFactorCount();

        int[] fDegreeArray = new int [factorCount];
        Arrays.fill(fDegreeArray, fDegree);
        int[] vDegreeArray = new int [varCount];
        int totalHalfEdges = factorCount * fDegree;
        for (int cnt = 0; cnt < totalHalfEdges; ) {
            for (int v = 0; v < varCount && cnt < totalHalfEdges; v++) {
                vDegreeArray[v]++;
                cnt++;
            }
        }

        for (int iter = 0; iter < iterCount; iter++) {
            if (sampleRandomGraphExact(random, graph, 
                                       vDegreeArray, fDegreeArray))
                return;
        }

        throw new IllegalStateException("unable to sample graph");
    }

    private static final int MAX_SAMPLE_ITER = 10;

    public static boolean sampleRandomGraphExact(Random random,
                                                 FactorGraph graph,
                                                 int[] vDegree,
                                                 int[] fDegree) 
    {
        graph.reset();
        int varCount = graph.getVariableCount();
        int factorCount = graph.getFactorCount();

        int halfEdgeCount = 0;
        for (int i = 0; i < varCount; i++)
            halfEdgeCount += vDegree[i];
        int remainingHalfEdgeCount = 0;
        for (int i = 0; i < factorCount; i++)
            remainingHalfEdgeCount += fDegree[i];
        if (halfEdgeCount != remainingHalfEdgeCount)
            throw new IllegalArgumentException("degrees do not sum");

        int[] remainingVarDegree = new int [varCount];
        System.arraycopy(vDegree, 0, remainingVarDegree, 0, varCount);
        int[] remainingFactorDegree = new int [factorCount];
        System.arraycopy(fDegree, 0, remainingFactorDegree, 0, factorCount);

        // attempt to add edges by randomly sampling available half edges
        while (remainingHalfEdgeCount > 0) {
            int v = -1;
            int f = -1;
            boolean found = false;
            for (int iter = 0; iter < MAX_SAMPLE_ITER && !found; iter++) {
                v = sampleHalfEdge(remainingVarDegree,
                                   remainingHalfEdgeCount,
                                   random);
                f = sampleHalfEdge(remainingFactorDegree,
                                   remainingHalfEdgeCount,
                                   random);
                if (!graph.isConnected(v, f)) 
                    found = true;
            }

            if (found) {
                if (!graph.addEdge(v, f))
                    throw new IllegalStateException("unable to add edge");
                remainingVarDegree[v]--;
                remainingFactorDegree[f]--;
                remainingHalfEdgeCount--;
            }
            else
                break;
        }

        // if there are edges remaining, use exhaustive sampling
        // of only half-edge pairs that do not form self-edges
        while (remainingHalfEdgeCount > 0) {
            int pairCount = 0;
            int v = -1;
            int f = -1;

            for (v = 0; v < varCount; v++) {
                if (remainingVarDegree[v] > 0) {
                    for (f = 0; f < factorCount; f++) {
                        if (remainingFactorDegree[f] > 0) {
                            if (!graph.isConnected(v, f))
                                pairCount += remainingVarDegree[v] 
                                    * remainingFactorDegree[f];
                        }
                        else if (remainingFactorDegree[f] < 0)
                            throw new IllegalStateException("degree "
                                                            + "counts "
                                                            + "inaccurate");
                    }
                }
                else if (remainingVarDegree[v] < 0)
                    throw new IllegalStateException("degree counts"
                                                    + " inaccurate");
            }


            if (pairCount <= 0)
                return false;

            int index = random.nextInt(pairCount);

            int sum = 0;
            boolean found = false;
            outer:
            for (v = 0; v < varCount; v++) {
                if (remainingVarDegree[v] > 0) {
                    for (f = 0; f < factorCount; f++) {
                        if (remainingFactorDegree[f] > 0) {
                            if (!graph.isConnected(v, f)) {
                                sum += remainingVarDegree[v] 
                                    * remainingFactorDegree[f];
                                if (sum > index) {
                                    found = true;
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
            
            if (!found)
                throw new IllegalStateException("unable to sample a half edge");

            if (!graph.addEdge(v, f))
                throw new IllegalStateException("unable to add edge");
            remainingVarDegree[v]--;
            remainingFactorDegree[f]--;
            remainingHalfEdgeCount--;
        }

        graph.compress();
        return true;
    }

    private static int sampleHalfEdge(int[] remainingDegree,
                                      int remainingHalfEdgeCount,
                                      Random random) {
        int index = random.nextInt(remainingHalfEdgeCount);
        int sum = 0;
        for (int i = 0; i < remainingDegree.length; i++) {
            if (remainingDegree[i] > 0) {
                sum += remainingDegree[i];
                if (sum > index)
                    return i;
            }
            else if (remainingDegree[i] < 0)
                throw new IllegalStateException("degree counts inaccurate");
        }
        throw new IllegalStateException("unable to sample a half edge");
    }

}