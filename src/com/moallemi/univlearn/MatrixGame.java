package com.moallemi.univlearn;

public interface MatrixGame {
    public int getNumPlays();
    public double getCost(int ourPlay, int otherPlay);
    public String getSymbol(int play);
}