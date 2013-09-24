package com.moallemi.queueing;

import com.moallemi.adp.*;

public class QueueStateInfo extends StateInfo {
    // next states assuming various queues are serviced
    private QueueState[] nextStateIfServiced;

    public QueueStateInfo(Action[] actions, 
                          StateDistribution[] distributions,
                          QueueState[] nextStateIfServiced) 
    {
        super(actions, distributions);
        this.nextStateIfServiced = nextStateIfServiced;
    }

    public QueueState getNextStateIfServiced(int i) {
        return nextStateIfServiced[i];
    }
}