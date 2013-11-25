package com.moallemi.matching;

import java.util.*;

import com.moallemi.util.PropertySet;

public abstract class MatchingModel {
  protected int supplyTypeDim;
  protected int demandTypeDim;
  protected int[] supplyTypes;
  protected int[] demandTypes;
  protected MultiDiscreteDistribution supplyDistribution;
  protected MultiDiscreteDistribution demandDistribution;
  protected RewardFunction rewardFunction;
  protected String modelType;
  protected Random random;

  public void initiateRandom(long seed) {
    random = new Random(seed);
    setSupRandomSeed(random.nextLong());
    setDemRandomSeed(random.nextLong());
  }

  public Random getRandom() {
    return random;
  }

  public RewardFunction getRewardFunction() {
    return rewardFunction;
  }

  public int getSupplyDim() {
    return supplyTypeDim;
  }

  public int getDemandDim() {
    return demandTypeDim;
  }

  public int[] getTypesPerDimSup() {
    return supplyTypes;
  }

  public int[] getTypesPerDimDem() {
    return demandTypes;
  }

  public void setSupRandom(Random rand) {
    supplyDistribution.setRandom(rand);
  }

  public void setSupRandomSeed(long seed) {
    supplyDistribution.setRandom(new Random(seed));
  }

  public void setDemRandom(Random rand) {
    demandDistribution.setRandom(rand);
  }

  public void setDemRandomSeed(long seed) {
    demandDistribution.setRandom(new Random(seed));
  }

  public ArrayList<Item> sampleSupplyTypes(int n) {
    ArrayList<Item> supplyTypesSampled = new ArrayList<Item>(n);
    for (int i = 0; i < n; i++) {
      Item type = new Item(supplyDistribution.nextSample());
      supplyTypesSampled.add(type);
    }
    return supplyTypesSampled;
  }

  public ArrayList<Item> sampleDemandTypes(int n) {
    ArrayList<Item> demandTypesSampled = new ArrayList<Item>(n);
    for (int i = 0; i < n; i++) {
      Item type = new Item(demandDistribution.nextSample());
      demandTypesSampled.add(type);
    }
    return demandTypesSampled;
  }

  protected void init(PropertySet props) {
    double[][] supProbs;
    double[][] demProbs;
    if (props.getBooleanDefault("supply_demand_equal", false)) {
      supplyTypeDim = props.getInt("supply_type_dimentions");
      System.out.println("supply and demand type getDimension is " + supplyTypeDim);
      demandTypeDim = supplyTypeDim;
      supplyTypes = new int[supplyTypeDim];
      demandTypes = new int[demandTypeDim];
      supProbs = new double[supplyTypeDim][];
      demProbs = new double[demandTypeDim][];
      for (int i = 0; i < supplyTypeDim; i++) {
        supplyTypes[i] = props.getIntDefault("st[" + (i + 1) + "]", 0);
        demandTypes[i] = props.getIntDefault("st[" + (i + 1) + "]", 0);
        System.out.println("no of supply types at getDimension " + i + " is " + supplyTypes[i]);
        supProbs[i] = new double[supplyTypes[i]];
        demProbs[i] = new double[demandTypes[i]];
        for (int j = 0; j < supplyTypes[i]; j++) {
          supProbs[i][j] = props.getDoubleDefault("sp[" + (i + 1) + "][" + (j + 1) + "]", 0.0);
          demProbs[i][j] = supProbs[i][j];
          System.out.println("prob of type (" + i + ", " + j + ") is " + supProbs[i][j]);
        }
      }
      Random supRandom = new Random(props.getLongDefault("supply_seed", 123L));
      Random demRandom = new Random(props.getLongDefault("demand_seed", 456L));
      supplyDistribution = new MultiDiscreteDistribution(supProbs, supRandom);
      demandDistribution = new MultiDiscreteDistribution(demProbs, demRandom);
      if (props.getStringDefault("reward_function", "separable").equals("separable")) {
        rewardFunction = new SeparableRewardFunction();
      }
    } else {
      supplyTypeDim = props.getInt("supply_type_dimentions");
      demandTypeDim = props.getInt("demand_type_dimentions");
      supplyTypes = new int[supplyTypeDim];
      demandTypes = new int[demandTypeDim];
      supProbs = new double[supplyTypeDim][];
      demProbs = new double[demandTypeDim][];
      for (int i = 0; i < supplyTypeDim; i++) {
        supplyTypes[i] = props.getIntDefault("st[" + (i + 1) + "]", 0);
        supProbs[i] = new double[supplyTypes[i]];
        for (int j = 0; j < supplyTypes[i]; j++) {
          supProbs[i][j] = props.getDoubleDefault("sp[" + (i + 1) + "][" + (j + 1) + "]", 0.0);
        }
      }
      for (int i = 0; i < demandTypeDim; i++) {
        demandTypes[i] = props.getIntDefault("dt[" + (i + 1) + "]", 0);
        demProbs[i] = new double[demandTypes[i]];
        for (int j = 0; j < demandTypes[i]; j++) {
          demProbs[i][j] = props.getDoubleDefault("dp[" + (i + 1) + "][" + (j + 1) + "]", 0.0);
        }
      }
      Random supRandom = new Random(props.getLongDefault("supply_seed", 123L));
      Random demRandom = new Random(props.getLongDefault("demand_seed", 456L));
      supplyDistribution = new MultiDiscreteDistribution(supProbs, supRandom);
      demandDistribution = new MultiDiscreteDistribution(demProbs, demRandom);
      if (props.getStringDefault("reward_function", "separable").equals("separable")) {
        rewardFunction = new SeparableRewardFunction();
      }
    }
  }

  public void printInfo() {
    System.out.println("supply type has getDimension: " + supplyTypeDim);
    System.out.println("demand type has getDimension: " + demandTypeDim);

    System.out.print("supply types along dimensions: ");
    for (int i = 0; i < supplyTypeDim; i++) {
      System.out.print(supplyTypes[i] + " ");
    }
    System.out.println(" ");

    System.out.print("demand types along dimensions: ");
    for (int i = 0; i < demandTypeDim; i++) {
      System.out.print(demandTypes[i] + " ");
    }
    System.out.println(" ");

    System.out.println("probabilities for the supply type -");
    for (int i = 0; i < supplyTypeDim; i++) {
      System.out.print("along dim " + (i + 1) + ": ");
      for (int j = 0; j < supplyTypes[i]; j++) {
        System.out.print(supplyDistribution.getProb(i, j) + " ");
      }
      System.out.println(" ");
    }

    System.out.println("probabilities for the demand type -");
    for (int i = 0; i < demandTypeDim; i++) {
      System.out.print("along dim " + (i + 1) + ": ");
      for (int j = 0; j < demandTypes[i]; j++) {
        System.out.print(demandDistribution.getProb(i, j) + " ");
      }
      System.out.println(" ");
    }

  }
}