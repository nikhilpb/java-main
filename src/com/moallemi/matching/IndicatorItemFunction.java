package com.moallemi.matching;

public class IndicatorItemFunction implements ItemFunction {
  private Item type;

  public IndicatorItemFunction(Item type) {
    this.type = type;
  }

  public double evaluate(Item eType) {
    if (Item.equals(type, eType)) {
      return 1.0;
    }
    return 0.0;
  }

  public String getName() {
    return ("indicator function at type: " + type.toString());
  }
}
