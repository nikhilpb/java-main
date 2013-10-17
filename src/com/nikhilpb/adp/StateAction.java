package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/16/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class StateAction {
    private final State state;
    private final Action action;

    public StateAction(State state, Action action) {
        this.state = state;
        this.action = action;
    }

    public State getState() {
        return state;
    }

    public Action getAction() {
        return action;
    }
}
