package com.nikhilpb.tetris;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/17/13
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PieceTest {
    @Test
    public void testPiece() throws Exception {
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
}
