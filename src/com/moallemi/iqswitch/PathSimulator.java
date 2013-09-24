package com.moallemi.iqswitch;

import java.util.*;

public class PathSimulator implements Iterator<SwitchState> {
    private Random random;
    private SwitchState currentState;
    private SwitchModel model;
    private MatchingPolicy policy;

    public PathSimulator(SwitchModel model,
                         MatchingPolicy policy,
                         Random random) {
        this.model = model;
        this.policy = policy;
        reset(random);
    }

    /**
     * Reset the sample path, and initialize with a new source of
     * randomness.
     *
     * param random randomness source
     */
    public void reset(Random random) {
        this.random = random;
        currentState = null;
    }

    // Iterator interface
    public boolean hasNext() { return true; }
    public void remove() { throw new UnsupportedOperationException(); }

    /**
     * Fetch the next state in the path.
     *
     * @return the next state
     */
    public SwitchState next() {
        // always start in the base state
        if (currentState == null) {
            currentState = model.getBaseState();
            return currentState;
        }

        // select action
        policy.setState(currentState);
        SwitchAction action = policy.getAction();

        currentState = model.sampleNextState(currentState,
                                             action,
                                             random);
        return currentState;
    }

}