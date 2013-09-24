package com.moallemi.iqswitch;

public class SwitchAction {
    private int[] sourceWorkedOn;

    public SwitchAction(int[] sourceWorkedOn) {
        this.sourceWorkedOn = sourceWorkedOn;

        // sanity check
        int[] used = new int [sourceWorkedOn.length];
        for (int dest = 0; dest < sourceWorkedOn.length; dest++) {
            if (sourceWorkedOn[dest] == -1)
                continue;
            if (used[sourceWorkedOn[dest]] == 1)
                throw new IllegalStateException("action is not a matching");
            used[sourceWorkedOn[dest]] = 1;
        }
    }

    public int getSourceWorkedOn(int dest) {
        return sourceWorkedOn[dest];
    }

    public int hashCode() {
	int h = 0;
        for (int dest = 0; dest < sourceWorkedOn.length; dest++) 
	    h = h*137 + sourceWorkedOn[dest];
	return h;
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
	if (other instanceof SwitchAction) {
	    SwitchAction o = (SwitchAction) other;
	    if (sourceWorkedOn.length != o.sourceWorkedOn.length)
		return false;
            for (int dest = 0; dest < sourceWorkedOn.length; dest++) {
                if (sourceWorkedOn[dest] != o.sourceWorkedOn[dest])
                    return false;
            }
	    return true;
	}
	return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int dest = 0; dest < sourceWorkedOn.length; dest++) {
            if (dest > 0)
                sb.append(",");
            sb.append(sourceWorkedOn[dest]+1);
        }
        return sb.toString();
    }
}