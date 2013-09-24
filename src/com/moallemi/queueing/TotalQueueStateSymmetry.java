package com.moallemi.queueing;

import java.util.*;

public class TotalQueueStateSymmetry implements QueueStateSymmetry {

    public void canonicalForm(int[] queueLengths) {
        Arrays.sort(queueLengths);
    }
}