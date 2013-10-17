package com.nikhilpb.tetris;

import com.nikhilpb.adp.Action;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/17/13
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class TetrisAction implements Action {
    public static final ArrayList<TetrisAction> ALLACTIONS;

    static {
        ALLACTIONS = new ArrayList<TetrisAction>();
        for (int i = -2; i < TetrisState.kRows; ++i) {
            for (int j = -2; j < TetrisState.kColumns; ++j) {
                for (int k = 0; k < 4; ++k) {
                    ALLACTIONS.add(new TetrisAction(i, j, k));
                }
            }

        }
    }

    public final int rowInd;
    public final int colInd;
    public final int rotation;

    private TetrisAction(int rowInd, int colInd, int rotation) {
        this.rowInd = rowInd;
        this.colInd = colInd;
        this.rotation = rotation;
    }
}
