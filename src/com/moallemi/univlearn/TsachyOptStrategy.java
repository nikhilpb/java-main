package com.moallemi.univlearn;

import java.util.Random;

public class TsachyOptStrategy implements Strategy 
{

    private Random random; 
    private int lastPlay;
    private int rockCount;


    public int nextPlay() {
        if (rockCount > 0) {
            rockCount--;
            lastPlay = RockPaperScissors.PLAY_PAPER;
        }
        else
            lastPlay = RockPaperScissors.PLAY_SCISSORS;
        return lastPlay;
    }

    public void setNextOpponentPlay(int otherPlay) {
        if (otherPlay == RockPaperScissors.PLAY_ROCK 
            && lastPlay == RockPaperScissors.PLAY_SCISSORS)
            rockCount = 1;
    }

    public void reset(Random random) {
        this.random = random;
        rockCount = 0;
    }
}

