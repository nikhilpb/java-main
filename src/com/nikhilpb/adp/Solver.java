package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/3/13
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Solver {
    public boolean solve() throws Exception;
    public Policy getPolicy();
}
