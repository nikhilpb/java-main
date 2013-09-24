package com.moallemi.bloomhash;

import java.io.*;
import java.util.*;

import com.moallemi.math.Shuffle;

public class BloomHashFixedOrder extends BloomHash {

    public BloomHashFixedOrder(int n) { super(n);  }

    public void build(Random random) {
        buildSignatures(random);

        // make the assignments
        assignmentTable = new int [n][];
        for (int j = 0; j < n; j++) {
            assignmentTable[j] = new int [size];
            int zeroCount = 0;
            int oneCount = 0;
            Arrays.fill(assignmentTable[j], -1);

            for (int i = 0; i < size; i++) {
                int i2 = swapWithFirstBit(i, j);
                int position = signatures[i2][j];
                if (assignmentTable[j][position] < 0) {
                    int assignment = 0;  // by default, assign zero
                    if (zeroCount > oneCount)
                        assignment = 1;
                    
                    assignmentTable[j][position] = assignment;
                    if (assignment == 0)
                        zeroCount += counts[j][position];
                    else
                        oneCount += counts[j][position];
                }
            }
                
            // assign remaining bits to zero
            for (int i = 0; i < size; i++) {
                if (assignmentTable[j][i] < 0) {
                    if (counts[j][i] != 0)
                        throw new RuntimeException("unassigned bit");
                    assignmentTable[j][i] = 0;
                }
            }

            // fix so zeros and ones match
            while (zeroCount != oneCount) {
                int diff = zeroCount - oneCount;
                int absdiff = diff > 0 ? diff : -diff;
                int position = random.nextInt(size);
                int count = counts[j][position];
                if (diff > 0) {
                    if (assignmentTable[j][position] == 0 
                        && count == 1) {
                        assignmentTable[j][position] = 1;
                        zeroCount -= count;
                        oneCount += count;
                    }
                }
                else {
                    if (assignmentTable[j][position] == 1 
                        && count == 1) {
                        assignmentTable[j][position] = 0;
                        zeroCount += count;
                        oneCount -= count;
                    }
                }
            }
        }
    }

    private int swapWithFirstBit(int x, int position) {
        int y = x;
        int posMask = 1 << position;

        if ((x & 1) == 0) {
            // first bit in x is not set
            y &= (~posMask);
        }
        else {
            // first bit in x is set
            y |= posMask;
        }

        if ((x & posMask) == 0) {
            // position bit in x is not set
            y &= (~1);
        }
        else {
            // position bit in x is set
            y |= 1;
        }

        return y;
    }
     
}
 