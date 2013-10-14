package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/14/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class StoppingState implements State {

    public static final class Nil extends StoppingState {
        public StateType getStateType() {
            return StateType.NIL;
        }
    }

    public static final class Vector extends StoppingState {
        public StateType getStateType() {
            return StateType.VECTOR;
        }
        public double[] vector;
        public Vector(double[] vector) { this.vector = vector; }
    }

    public abstract StateType getStateType();

    public static enum StateType {
        NIL, VECTOR;
    }
}
