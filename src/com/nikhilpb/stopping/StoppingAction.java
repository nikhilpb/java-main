package com.nikhilpb.stopping;

import com.moallemi.adp.Action;
import com.moallemi.adp.State;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/10/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public enum StoppingAction implements Action {
    STOP, CONTINUE;

    @Override
    public boolean isCompatible(State state) { return true; }
}
