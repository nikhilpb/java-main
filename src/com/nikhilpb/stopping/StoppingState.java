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

    // singleton class denoting the absorbing state
    public static final class Nil extends StoppingState {
        private static Nil NIL = new Nil();
        private Nil() { }

        public static Nil get() {
            return NIL;
        }

        public StateType getStateType() {
            return StateType.NIL;
        }

        public String toString() {
            return "nil";
        }
    }

    public static final class Vector extends StoppingState {

        public StateType getStateType() {
            return StateType.VECTOR;
        }

        public double[] vector;
        public int time;

        public Vector(double[] vector, int time) {
            this.vector = vector;
            this.time = time;
        }

        public String toString() {
            String str = "time: " + time + ", vector: [";
            for (int i = 0; i < vector.length; ++i) {
                str += vector[i];
                if (i < vector.length-1) {
                    str += " ";
                }
            }
            str += "]";
            return str;
        }
    }

    public abstract StateType getStateType();

    public static enum StateType {
        NIL, VECTOR;
    }
}
