package com.moallemi.matching;

public class ConstantItemFunction implements ItemFunction {

  private double value;

  public ConstantItemFunction(double value) {
    this.value = value;
  }

  public double evaluate(Item type) {
    return value;
  }

  public String getName() {
    return ("constant function that returns " + value);
  }
}
