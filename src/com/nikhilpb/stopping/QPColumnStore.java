package com.nikhilpb.stopping;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/24/13
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface QPColumnStore {
    public QPColumn getColumn(int t, int stateInd, StoppingAction stoppingAction);
    public void initialize(ColumnStoreArguments args);
}
