package com.nikhilpb.tetris;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/17/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class TetrisTest {
    @Test
    public void pieceTest() throws Exception {
        assert Piece.ELL.configCount() == 4;
        assert Piece.TEE.configCount() == 4;
        assert Piece.REVELL.configCount() == 4;
        assert Piece.ESS.configCount() == 4;
        assert Piece.ZEE.configCount() == 4;
        assert Piece.SQR.configCount() == 1;
        assert Piece.LINE.configCount() == 2;

        for (Piece p : Piece.PIECES) {
            for (int i = 0; i < Piece.kPieceSize; ++i) {
                for (int j = 0; j < Piece.kPieceSize; ++j) {
                    if (!p.equals(Piece.SQR))
                        assert p.config(0)[Piece.kPieceSize - j - 1][i] == p.config(1)[i][j];
                    else
                        assert p.config(0)[i][j] == p.config(1)[i][j];
                }
            }
        }
    }

    @Test
    public void stateTest() throws Exception {
        int[][] config = new int[TetrisState.kRows][TetrisState.kColumns];
        TetrisState state = new TetrisState(config, Piece.SQR);

        TetrisAction action = TetrisAction.ALLACTIONS.get(102);
        assert action.rowInd == 0;
        assert action.colInd == -1;
        assert action.rotation == 2;
        assert !state.isCompatible(action);

        TetrisAction action2 = TetrisAction.ALLACTIONS.get(500);
        assert action2.rowInd == 8;
        assert action2.colInd == 3;
        assert action2.rotation == 0;
        assert state.isCompatible(action2);
    }
}
