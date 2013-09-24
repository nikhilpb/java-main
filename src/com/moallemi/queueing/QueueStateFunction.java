package com.moallemi.queueing;

import com.moallemi.adp.*;

public class QueueStateFunction implements StateFunction {
    private QueueState thisState;

    public QueueStateFunction(QueueState thisState) {
        this.thisState = thisState;
    }

    public double getValue(State state) {
        QueueState qState = (QueueState) state;
        return qState.equals(thisState) ? 1.0 : 0.0;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("qstate[ ");
        int queueCount = thisState.getQueueCount();
        for (int i = 0; i < queueCount; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(thisState.getQueueLength(i));
        }
        sb.append(" ]");
        return sb.toString();
    }

    public QueueState getQueueState() { return thisState; }
}
