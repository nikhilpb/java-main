package com.moallemi.univlearn;

public class PrisonersDilemma implements MatrixGame {
    public static final int PLAY_COOPERATE = 0;
    public static final int PLAY_DEFECT = 1;

    private static final int[][] cost =
    { 
        { -1,  1 },
        { -2,  0 }
    };

    public int getNumPlays() { return 2; }

    public double getCost(int ourPlay, int otherPlay) {
        return cost[ourPlay][otherPlay];
    }

    private static String[] symbol = { "C", "D" };

    public String getSymbol(int play) { return symbol[play]; }

}