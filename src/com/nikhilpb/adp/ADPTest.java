package com.nikhilpb.adp;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/16/13
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ADPTest {
    public static enum DummyState implements State {
        A, B;

        private static final ArrayList<Action> actions;

        static {
            actions = new ArrayList<Action>();
            actions.add(DummyAction.STAY);
            actions.add(DummyAction.SWITCH);
        }
        public ArrayList<Action> getActions() {
            return actions;
        }
    }

    public static enum DummyAction implements Action {
        STAY, SWITCH;
    }

    public static class DummyRewardFunction implements RewardFunction {
        public double value(State state, Action action) { return 0.0; }
    }


}
