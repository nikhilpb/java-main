package com.moallemi.queueing;

import java.util.*;

import com.moallemi.adp.State;

public class QueueStateComparator implements Comparator<State> {
    public int compare(State sa, State sb) {
	QueueState a = (QueueState) sa;
	QueueState b = (QueueState) sb;
	int aLen = a.getQueueCount();
	int bLen = b.getQueueCount();
	if (aLen != bLen)
	    throw new IllegalArgumentException("cannot compare queues "
					       + "of different sizes");
        for (int i = 0; i < aLen; i++) {
	    int aV = a.getQueueLength(i);
	    int bV = b.getQueueLength(i);
            if (aV < bV)
                return -1;
            if (aV > bV)
                return 1;
        }
        return 0;
    }
}
