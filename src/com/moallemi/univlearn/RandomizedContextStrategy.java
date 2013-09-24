package com.moallemi.univlearn;

import java.util.*;

public class RandomizedContextStrategy extends ContextStrategy
{
    private Map playMap = new HashMap();
    private Random thisRandom;

    public RandomizedContextStrategy(MatrixGame game,
                                     boolean useBoth, 
                                     int memory, 
                                     double fracRandom,
                                     Random random)
    {
        super(game, useBoth, memory);
        int size = getContextCount();
        int n = game.getNumPlays();
        Integer playRandom = new Integer(-1);
        for (int i = 0; i < size; i++) {
            Context context = getContext(i);
            Integer play = random.nextDouble() < fracRandom
                ? playRandom
                : new Integer(random.nextInt(n));
            playMap.put(context, play);
        }
    }

    private int lastPlay;
    private Context current;

    public int nextPlay() {
        lastPlay = ((Integer) playMap.get(current)).intValue();
        if (lastPlay < 0)
            lastPlay = thisRandom.nextInt(game.getNumPlays());
        return lastPlay;
    }

    public void setNextOpponentPlay(int play) {
        current = current.getNextContext(lastPlay, play);
    }

    public void reset(Random random) {
        lastPlay = 0;
        current = getEmptyContext();
        thisRandom = random;
    }


}