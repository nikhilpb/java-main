package com.moallemi.matching;


public class FirstOrderItemFunction implements ItemFunction {
  private int dim;
  private int level;

  public FirstOrderItemFunction(int dim, int level) {
    this.dim = dim;
    this.level = level;
  }

  public double evaluate(Item type) {
    if (dim >= type.getDimensions()) {
      System.out.println("invalid function for this type");
      return -1.0;
    } else if (level == type.getTypeAtDimension(dim)) {
      return 1.0;
    }
    return 0.0;
  }

  public String getName() {
    return ("indicator-dimention:" + dim + "-level:" + level);
  }
}
