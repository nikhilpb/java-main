package com.nikhilpb.abtesting;

/**
 * Created by nikhilpb on 9/8/14.
 */
public enum ABAction {
  PLUS, MINUS;

  public int toInt() {
    switch (this) {
      case PLUS:
        return 1;
      case MINUS:
        return -1;
    }
    return 0;
  }
}
