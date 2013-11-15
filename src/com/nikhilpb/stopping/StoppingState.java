package com.nikhilpb.stopping;

import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.State;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/14/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingState implements State {
    public double[] vector;
    public int time;
    private final static ArrayList<Action> actionList;

    static {
        actionList = new ArrayList<Action>();
        actionList.add((Action) StoppingAction.CONTINUE);
        actionList.add((Action) StoppingAction.STOP);
    }

    public ArrayList<Action> getActions() { return actionList; }

    public StoppingState(double[] vector, int time) {
        this.vector = vector;
        this.time = time;
    }

    public String toString() {
        String str = "time: " + time + ", vector: [";
        for (int i = 0; i < vector.length; ++i) {
            str += vector[i];
            if (i < vector.length-1) {
                str += " ";
            }
        }
        str += "]";
        return str;
    }

    public double[] getDifference(StoppingState oState) {
        if (oState.vector.length != this.vector.length) {
            throw new RuntimeException("vector lengths don't match");
        }
        double[] diff = new double[oState.vector.length];
        for (int i = 0; i < diff.length; ++i) {
            diff[i] = oState.vector[i] - this.vector[i];
        }
        return diff;
    }
}
