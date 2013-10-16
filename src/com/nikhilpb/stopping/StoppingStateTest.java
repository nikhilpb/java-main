package com.nikhilpb.stopping;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/15/13
 * Time: 2:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingStateTest {
    @Test
    public void testBase() throws Exception {
        StoppingState nil = StoppingState.Nil.get();
        assert nil.getStateType() == StoppingState.StateType.NIL;
        double[] vecArray = {1., 0.};
        StoppingState vec = new StoppingState.Vector(vecArray, 1);
        assert vec.getStateType() == StoppingState.StateType.VECTOR;
    }
}
