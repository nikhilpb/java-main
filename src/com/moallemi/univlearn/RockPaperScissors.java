package com.moallemi.univlearn;

public class RockPaperScissors implements MatrixGame {
    public static final int PLAY_ROCK = 0;
    public static final int PLAY_PAPER = 1;
    public static final int PLAY_SCISSORS = 2;

    private static final int[][] cost =
    { 
        {  0,  1, -1 },
        { -1,  0,  1 },
        {  1, -1,  0 }
    };

    public int getNumPlays() { return 3; }

    public double getCost(int ourPlay, int otherPlay) {
        return cost[ourPlay][otherPlay];
    }

    private static String[] symbol = { "R", "P", "S" };

    public String getSymbol(int play) { return symbol[play]; }

}