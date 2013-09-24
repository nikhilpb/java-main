package com.moallemi.univlearn;

import java.util.Random;

public class ConstantStrategy implements Strategy 
{
    private int play;
    
    public ConstantStrategy(int play) { this.play = play; }
    public int nextPlay() { return play; }
    public void setNextOpponentPlay(int play) {}
    public void reset(Random random) {}
}

