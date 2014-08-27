package com.nikhilpb.tetris;

import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.State;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/17/13
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class TetrisState implements State {
  public static final int kRows = 20;
  public static final int kColumns = 10;

  private int[][] board;
  private Piece piece;

  public TetrisState(int[][] board, Piece piece) {
    this.board = board;
    this.piece = piece;
  }


  public ArrayList<Action> getActions() {
    ArrayList<Action> actions = new ArrayList<Action>();
    for (TetrisAction act : TetrisAction.ALLACTIONS) {
      if (isCompatible(act))
        actions.add(act);
    }
    return actions;
  }

  public boolean isCompatible(TetrisAction action) {
    if (action.rotation >= piece.configCount()) {
      return false;
    }
    if (! checkOverflow(action)) {
      return false;
    }
    return true;
  }

  public boolean checkOverflow(TetrisAction action) {
    int[][] pieceConfig = piece.config(action.rotation);
    for (int i = 0; i < Piece.kPieceSize; ++ i) {
      for (int j = 0; j < Piece.kPieceSize; ++ j) {
        if (pieceConfig[i][j] > 0) {
          int xInd = i + action.rowInd;
          int yInd = j + action.colInd;
          if (0 > xInd || xInd >= kRows || 0 > yInd || yInd >= kColumns)
            return false;
        }
      }
    }
    return true;
  }
}
