package com.nikhilpb.stopping;

import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.State;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/16/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class NilState implements State {
    private NilState NILSTATE = new NilState();
    private NilState() { }
    public ArrayList<Action> getActions() { return null; }
    public NilState get() { return NILSTATE; }
}
