package com.moallemi.univlearn;

import java.util.Random;

public interface Strategy {

    public int nextPlay();
    public void setNextOpponentPlay(int play);
    public void reset(Random random);
}
