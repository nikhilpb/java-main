package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/13/13
 * Time: 7:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class StateTime implements State {
    public final State state;
    public final int time;

    public StateTime(State state, int time) {
        this.state = state;
        this.time = time;
        if (time < 0) {
            throw new IllegalArgumentException("time should be non-negative");
        }
    }
}
