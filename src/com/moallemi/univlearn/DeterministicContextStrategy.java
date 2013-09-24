package com.moallemi.univlearn;

import java.util.*;

public class DeterministicContextStrategy
    extends ContextStrategy
{
    private Map playMap = new HashMap();

    public DeterministicContextStrategy(MatrixGame game,
                                        boolean useBoth, 
                                        int memory, 
                                        Random random)
    {
        super(game, useBoth, memory);
        int size = getContextCount();
        for (int i = 0; i < size; i++) {
            Context context = getContext(i);
            playMap.put(context, 
                        new Integer(random
                                    .nextInt(game.getNumPlays())));
        }
    }

    private int lastPlay;
    private Context current;

    public int nextPlay() {
        lastPlay = ((Integer) playMap.get(current)).intValue();
        return lastPlay;
    }

    public void setNextOpponentPlay(int play) {
        current = current.getNextContext(lastPlay, play);
    }

    public void reset(Random random) {
        lastPlay = 0;
        current = getEmptyContext();
    }
}