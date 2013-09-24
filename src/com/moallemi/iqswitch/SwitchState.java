package com.moallemi.iqswitch;

public class SwitchState {
    private int[][] queueLength;

    public SwitchState(int[][] queueLength) {
        this.queueLength = queueLength;
    }

    public int getQueueLength(int src, int dest) {
        return queueLength[src][dest];
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
	if (other instanceof SwitchState) {
	    SwitchState o = (SwitchState) other;
	    if (queueLength.length != o.queueLength.length)
		return false;
	    for (int src = 0; src < queueLength.length; src++) {
                if (queueLength[src].length != o.queueLength[src].length)
                    return false;
                for (int dest = 0; dest < queueLength[src].length; dest++)
                    if (queueLength[src][dest] != o.queueLength[src][dest])
                        return false;
            }
	    return true;
	}
	return false;
    }

    public int hashCode() {
	int h = 0;
        for (int src = 0; src < queueLength.length; src++) 
            for (int dest = 0; dest < queueLength[src].length; dest++)
	    h = h*137 + queueLength[src][dest];
	return h;
    }

    public void copyQueueLengths(int[][] out) {
        for (int src = 0; src < queueLength.length; src++) 
            for (int dest = 0; dest < queueLength[src].length; dest++)
                out[src][dest] = queueLength[src][dest];
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int src = 0; src < queueLength.length; src++) {
            for (int dest = 0; dest < queueLength[src].length; dest++) {
                sb.append(queueLength[src][dest]);
                if (dest == queueLength[src].length - 1
                    && src < queueLength.length - 1)
                    sb.append(";");
                else if (dest == queueLength[src].length - 1
                         && src == queueLength.length - 1)
                    ;
                else
                    sb.append(",");
            }
        }
        return sb.toString();
    }
}