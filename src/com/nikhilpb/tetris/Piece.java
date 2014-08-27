package com.nikhilpb.tetris;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/17/13
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class Piece {
  public static final int kPieceSize = 4;
  public static final Piece SQR, ELL, REVELL, TEE, ESS, ZEE, LINE;
  public static final ArrayList<Piece> PIECES;

  static {
    PIECES = new ArrayList<Piece>();

    int[][] sqrArray = {{1, 1, 0, 0},
                               {1, 1, 0, 0},
                               {0, 0, 0, 0},
                               {0, 0, 0, 0}};
    SQR = new Piece(sqrArray, 1);
    PIECES.add(SQR);

    int[][] ellArray = {{1, 0, 0, 0},
                               {1, 0, 0, 0},
                               {1, 1, 0, 0},
                               {0, 0, 0, 0}};
    ELL = new Piece(ellArray, 4);
    PIECES.add(ELL);

    int[][] revellArray = {{0, 1, 0, 0},
                                  {0, 1, 0, 0},
                                  {1, 1, 0, 0},
                                  {0, 0, 0, 0}};
    REVELL = new Piece(revellArray, 4);
    PIECES.add(REVELL);

    int[][] teeArray = {{1, 0, 0, 0},
                               {1, 1, 0, 0},
                               {1, 0, 0, 0},
                               {0, 0, 0, 0}};
    TEE = new Piece(teeArray, 4);
    PIECES.add(TEE);

    int[][] essArray = {{1, 0, 0, 0},
                               {1, 1, 0, 0},
                               {0, 1, 0, 0},
                               {0, 0, 0, 0}};
    ESS = new Piece(essArray, 4);
    PIECES.add(ESS);

    int[][] zeeArray = {{0, 1, 0, 0},
                               {1, 1, 0, 0},
                               {1, 0, 0, 0},
                               {0, 0, 0, 0}};
    ZEE = new Piece(zeeArray, 4);
    PIECES.add(ZEE);

    int[][] lineArray = {{1, 0, 0, 0},
                                {1, 0, 0, 0},
                                {1, 0, 0, 0},
                                {1, 0, 0, 0}};
    LINE = new Piece(lineArray, 2);
    PIECES.add(LINE);

  }

  private int[][][] config;
  private int noOfConfigs;

  private Piece(int[][] baseConfig, int noOfConfigs) {
    this.noOfConfigs = noOfConfigs;
    config = new int[noOfConfigs][kPieceSize][kPieceSize];
    config[0] = baseConfig;
    for (int i = 1; i < noOfConfigs; ++ i) {
      rotateCW(config[i - 1], config[i]);
    }
  }

  private static void rotateCW(int[][] startConfig, int[][] endConfig) {
    for (int i = 0; i < kPieceSize; ++ i) {
      for (int j = 0; j < kPieceSize; ++ j) {
        endConfig[i][j] = startConfig[kPieceSize - j - 1][i];
      }
    }
  }

  public int configCount() {
    return noOfConfigs;
  }

  public int[][] config(int i) {
    return config[i % noOfConfigs];
  }

  @Override
  public String toString() {
    String string = "piece with number of configs: " + noOfConfigs + "\n";
    for (int i = 0; i < config.length; ++ i) {
      string += "config: " + i + "\n";
      for (int j = 0; j < kPieceSize; ++ j) {
        for (int k = 0; k < kPieceSize; ++ k) {
          string += " " + config[i][j][k];
        }
        string += "\n";
      }
      string += "\n";
    }
    return string;
  }
}
