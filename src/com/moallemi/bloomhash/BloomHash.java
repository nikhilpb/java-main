package com.moallemi.bloomhash;

import java.io.*;
import java.util.*;

import com.moallemi.math.Shuffle;

public class BloomHash {
    protected int n;
    protected int size;
    protected int[][] signatures;
    protected int[][] counts;
    protected int[][] assignmentTable;

    public BloomHash(int n) {
        this.n = n;
        this.size = 1 << n;
    }

    public int getSize() { return size; }

    protected void buildSignatures(Random random) {
        // build the table of signatures
        signatures = new int [size][];
        for (int i = 0; i < size; i++) {
            signatures[i] = new int [n];
            for (int j = 0; j < n; j++) 
                signatures[i][j] = random.nextInt(size);
        }
        // build the table of counts
        counts = new int [n][];
        for (int j = 0; j < n; j++) {
            counts[j] = new int [size];
            for (int i = 0; i < size; i++)
                counts[j][signatures[i][j]]++;
        }
    }

    public void build(Random random) {
        buildSignatures(random);

        // make the assignments
        assignmentTable = new int [n][];
        int[] iterationOrder = new int [size];
        for (int j = 0; j < n; j++) {
            assignmentTable[j] = new int [size];

            boolean goodOrderFound = false;
            while (!goodOrderFound) {
                // build a random iteration order
                for (int i = 0; i < size; i++)
                    iterationOrder[i] = i;
                Shuffle.shuffle(random, iterationOrder);
                
                int zeroCount = 0;
                int oneCount = 0;
                Arrays.fill(assignmentTable[j], -1);
                
                for (int i = 0; i < size; i++) {
                    int assignment = 0;  // by default, assign zero
                    if (zeroCount > oneCount)
                        assignment = 1;
                    
                    int position = iterationOrder[i];
                    assignmentTable[j][position] = assignment;
                    if (assignment == 0)
                        zeroCount += counts[j][position];
                    else
                        oneCount += counts[j][position];
                }
                
                // sanity check
                for (int i = 0; i < size; i++) {
                    if (assignmentTable[j][i] < 0)
                        throw new RuntimeException("failed to assign all bits");
                }

                if (zeroCount == oneCount)
                    goodOrderFound = true;
            }
        }
    }

    public int getHashCode(int i) {
        int hashCode = 0;
        for (int j = 0; j < n; j++) {
            hashCode <<= 1;
            hashCode |= assignmentTable[j][signatures[i][j]];
        }
        return hashCode;
    }

}
 