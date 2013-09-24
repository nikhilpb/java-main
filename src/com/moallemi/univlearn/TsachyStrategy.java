package com.moallemi.univlearn;

import java.util.Random;

public class TsachyStrategy implements Strategy 
{
    private RockPaperScissors game = new RockPaperScissors();
    private Random random; 
    private int lastPlay;
    private int rockCount;

    public int nextPlay() {
        if (rockCount > 0) {
            rockCount--;
            lastPlay = RockPaperScissors.PLAY_ROCK;
        }
        else
            lastPlay = random.nextInt(game.getNumPlays());
        return lastPlay;
    }

    public void setNextOpponentPlay(int otherPlay) {
        if (otherPlay == RockPaperScissors.PLAY_SCISSORS 
            && lastPlay == RockPaperScissors.PLAY_ROCK)
            rockCount = 1;
    }

    public void reset(Random random) {
        this.random = random;
        rockCount = 0;
    }
}

