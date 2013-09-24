package com.moallemi.queueing;

import java.util.Set;

import com.moallemi.adp.*;

public class QueueStateSetFunction implements StateFunction {
    private Set<QueueState> set;
    private String label;

    public QueueStateSetFunction(Set<QueueState> set, String label) {
        this.set = set;
        this.label = label;
    }

    public double getValue(State state) {
        QueueState qState = (QueueState) state;
        return set.contains(qState) ? 1.0 : 0.0;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("qset[");
        sb.append(label);
        sb.append("]");
        return sb.toString();
    }
}
