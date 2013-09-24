package com.moallemi.queueing;

import java.util.*;

import com.moallemi.util.PermutationIterator;

public class SwitchQueueStateSymmetry implements QueueStateSymmetry {
    private SwitchModel model;
    private int[][] permutations;

    public SwitchQueueStateSymmetry(OpenQueueingNetworkModel qModel) {
        this.model = (SwitchModel) qModel;
        int n = model.getSwitchSize();

        // enumerate permutations of {0 .. n-1}
        PermutationIterator i = new PermutationIterator(n, n);
        int baseCnt = i.getSize();
        int[][] basePermutations = new int [baseCnt][n];
        int cnt = 0;
        while (i.hasNext()) {
            int[] p = i.next();
            System.arraycopy(p, 0, basePermutations[cnt++], 0, n);
        }
        if (cnt != baseCnt)
            throw new IllegalStateException("failed to enumerate permuations");

        // now, generate permutations of the underlying ports
        int queueCount = model.getQueueCount();
        permutations = new int [baseCnt*baseCnt][queueCount];
        cnt = 0;
        for (int s = 0; s < baseCnt; s++) {
            for (int d = 0; d < baseCnt; d++) {
                Arrays.fill(permutations[cnt], -1);
                int[] sourcePerm = basePermutations[s];
                int[] destPerm = basePermutations[d];

                for (int j = 0; j < queueCount; j++) {
                    int dest = 
                        model.getQueueIndex(sourcePerm[model.getSourcePort(j)],
                                            destPerm[model.getDestPort(j)]);
                    if (permutations[cnt][dest] != -1) 
                        throw new IllegalStateException("bad permutation");
                    permutations[cnt][dest] = j;
                }

                cnt++;
            }
        }
        if (cnt != permutations.length)
            throw new IllegalStateException("error enumerating permutations");

    }

    public void canonicalForm(int[] queueLengths) {
        int len = queueLengths.length;
        int[] min = new int [len];
        System.arraycopy(queueLengths, 0, min, 0, queueLengths.length);
        int[] cur = new int [len];

        for (int p = 0; p < permutations.length; p++) {
            for (int j = 0; j < len; j++) 
                cur[permutations[p][j]] = queueLengths[j];

            if (compare(cur, min) < 0) {
                int[] tmp = min;
                min = cur;
                cur = tmp;
            }
        }

        System.arraycopy(min, 0, queueLengths, 0, len);
    }

    private int compare(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] < b[i])
                return -1;
            if (a[i] > b[i])
                return 1;
        }
        return 0;
    }


        

}