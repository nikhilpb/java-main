package com.moallemi.univlearn;

import java.util.Random;

public class TitForTatStrategy implements Strategy 
{
    private int lastPlay;

    public int nextPlay() { return lastPlay; }
    public void setNextOpponentPlay(int play) { lastPlay = play; }
    public void reset(Random random) { lastPlay = 0; }
}

