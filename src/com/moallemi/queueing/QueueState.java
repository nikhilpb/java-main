package com.moallemi.queueing;

import com.moallemi.adp.*;

public class QueueState implements State {
    private int[] queueLength;
    
    public QueueState(int[] queueLength) {
	this.queueLength = queueLength;
    }

    public QueueState(QueueState other) {
	this.queueLength = new int [other.queueLength.length];
        System.arraycopy(other.queueLength, 0, queueLength, 0, 
                         queueLength.length);
    }

    public QueueState(String s) {
        String[] a = s.split(",");
        int len = a.length;
        queueLength = new int [len];
        for (int i = 0; i < len; i++)
            queueLength[i] = Integer.parseInt(a[i]);
    }

    public int getQueueLength(int i) {
	return queueLength[i];
    }

    public int getTotalQueueLength() {
        int sum = 0;
        for (int i = 0; i < queueLength.length; i++)
            sum += queueLength[i];
	return sum;
    }

    public int getQueueCount() { return queueLength.length; }

    public void copyQueueLengths(int[] queueLengthCopy) {
        if (queueLengthCopy.length != queueLength.length)
            throw new IllegalArgumentException("array length mismatch");
        System.arraycopy(queueLength, 0, queueLengthCopy, 0, 
                         queueLength.length);
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
	if (other instanceof QueueState) {
	    QueueState q = (QueueState) other;
	    if (queueLength.length != q.queueLength.length)
		return false;
	    for (int i = 0; i < queueLength.length; i++) {
		if (queueLength[i] != q.queueLength[i])
		    return false;
	    }
	    return true;
	}
	return false;
    }

    public int hashCode() {
	int h = 0;
	for (int i = 0; i < queueLength.length; i++)
	    h = h*137 + queueLength[i];
	return h;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < queueLength.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(queueLength[i]);
        }
        return sb.toString();
    }
}
