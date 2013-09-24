package com.moallemi.iqswitch;

public interface MonteCarloStateObserver {

    public void observe(int path, int time, SwitchState state);

}