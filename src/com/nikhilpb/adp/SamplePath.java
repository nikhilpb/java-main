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
    public ArrayList<State> stateList;
    public ArrayList<Action> actionList;
    public Double reward;

    public SamplePath() {
        stateList = new ArrayList<State>();
        actionList = new ArrayList<Action>();
        reward = 0.0;
    }
}
