package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/21/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Solver {
    public boolean solve();
    public StateFunction getValueEstimate();
}
