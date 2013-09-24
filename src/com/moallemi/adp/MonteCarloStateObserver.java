package com.moallemi.adp;

public interface MonteCarloStateObserver {

    public void observe(int path, int time, State state);

}