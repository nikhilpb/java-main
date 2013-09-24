package com.moallemi.adp;

public class StateInfo {
    // available actions
    private Action[] actions;
    // distributions under each action
    private StateDistribution[] distributions;

    public StateInfo(Action[] actions, 
                     StateDistribution[] distributions) 
    {
        this.actions = actions;
        this.distributions = distributions;
    }

    public int getActionCount() { return actions.length; }
    public Action getAction(int a) { return actions[a]; }
    public StateDistribution getDistribution(int a) {
        return distributions[a];
    }
}