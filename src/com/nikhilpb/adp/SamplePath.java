package com.nikhilpb.adp;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/16/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePath {
    public ArrayList<StateAction> stateActions;
    public double reward;

    public SamplePath() {
        stateActions = new ArrayList<StateAction>();
        reward = 0.0;
    }

    @Override
    public String toString() {
        String string = "";
        for (StateAction sa : stateActions) {
            string += "state : " + sa.getState().toString()
                    + "\naction : " + sa.getAction().toString() + "\n";
        }
        string += "reward: " + reward + "\n";
        return string;
    }
}
