package com.moallemi.adp;

public class StateProbability {
    private State nextState;
    private double probability;

    public StateProbability(State nextState, double probability) {
        this.nextState = nextState;
        this.probability = probability;
        if (probability < 0.0 || probability > 1.0)
            throw new RuntimeException("bad probability");
    }

    public State getNextState() { return nextState; }
    public double getProbability() { return probability; }

}