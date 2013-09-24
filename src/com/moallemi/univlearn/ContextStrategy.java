package com.moallemi.univlearn;

import java.util.*;

import com.moallemi.util.data.IntArray;

public abstract class ContextStrategy implements Strategy 
{
    protected MatrixGame game;
    private boolean useBoth;

    protected static abstract class Context {
        public abstract Context getNextContext(int action, int otherAction);
    }

    private static class OtherContext extends Context {
        private IntArray otherPlays;

        public OtherContext(int[] otherPlaysData) {
            otherPlays = new IntArray(otherPlaysData);
        }

        public boolean equals(Object other) {
            if (other instanceof OtherContext) 
                return otherPlays.equals(((OtherContext) other).otherPlays);
            return false;
        }

        public int hashCode() {
            return otherPlays.hashCode();
        }

        public Context getNextContext(int action, int otherAction) {
            int[] newOtherPlays = new int [otherPlays.size()];
            for (int i = 1; i < newOtherPlays.length; i++) 
                newOtherPlays[i-1] = otherPlays.get(i);
            newOtherPlays[newOtherPlays.length-1] = otherAction;
            return new OtherContext(newOtherPlays);
        }
    }

    private static class BothContext extends Context {
        private IntArray plays;
        private IntArray otherPlays;

        public BothContext(int[] playsData, int[] otherPlaysData) {
            plays = new IntArray(playsData);
            otherPlays = new IntArray(otherPlaysData);
        }

        public boolean equals(Object other) {
            if (other instanceof BothContext) 
                return plays.equals(((BothContext) other).plays)
                    && otherPlays.equals(((BothContext) other).otherPlays);
            return false;
        }

        public int hashCode() {
            return 11*plays.hashCode() + otherPlays.hashCode();
        }

        public Context getNextContext(int action, int otherAction) {
            int[] newPlays = new int [plays.size()];
            int[] newOtherPlays = new int [otherPlays.size()];
            for (int i = 1; i < newOtherPlays.length; i++) {
                newPlays[i-1] = plays.get(i);
                newOtherPlays[i-1] = otherPlays.get(i);
            }
            newPlays[newOtherPlays.length-1] = action;
            newOtherPlays[newOtherPlays.length-1] = otherAction;
            return new BothContext(newPlays, newOtherPlays);
        }
    }

    private Context[] contextList;

    public int getContextCount() { return contextList.length; }
    public Context getContext(int i) { return contextList[i]; }

    public ContextStrategy(MatrixGame game, boolean useBoth, int memory) {
        this.game = game;
        ArrayList list = new ArrayList();

        if (useBoth) {
            int[] plays = new int [memory];
            int[] otherPlays = new int [memory];
            boolean found = true;
            while (found) {
                int[] copyPlays = new int [memory];
                int[] copyOtherPlays = new int [memory];
                System.arraycopy(plays, 0, copyPlays, 0, memory);
                System.arraycopy(otherPlays, 0, copyOtherPlays, 0, memory);
                list.add(new BothContext(copyPlays, copyOtherPlays));

                found = false;
                for (int j = 0; j < memory && !found; j++) {
                    while (++plays[j] < game.getNumPlays()) {
                        break;
                    }
                    if (plays[j] >= game.getNumPlays())
                        plays[j] = 0;
                    else 
                        found = true;
                }
                for (int j = 0; j < memory && !found; j++) {
                    while (++otherPlays[j] < game.getNumPlays()) {
                        break;
                    }
                    if (otherPlays[j] >= game.getNumPlays())
                        otherPlays[j] = 0;
                    else 
                        found = true;
                }
            }
        }
        else {
            int[] otherPlays = new int [memory];
            boolean found = true;
            while (found) {
                int[] copyOtherPlays = new int [memory];
                System.arraycopy(otherPlays, 0, copyOtherPlays, 0, memory);
                list.add(new OtherContext(copyOtherPlays));

                found = false;
                for (int j = 0; j < memory && !found; j++) {
                    while (++otherPlays[j] < game.getNumPlays()) {
                        break;
                    }
                    if (otherPlays[j] >= game.getNumPlays())
                        otherPlays[j] = 0;
                    else 
                        found = true;
                }
            }
        }
            
        contextList = (Context[]) list.toArray(new Context [0]);
        
    }

    public Context getEmptyContext() {
        return contextList[0];
    }


}