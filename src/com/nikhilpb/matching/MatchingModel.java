package com.nikhilpb.matching;

import java.util.ArrayList;
import java.util.Random;

import com.nikhilpb.util.math.DistributionsCCM;
import com.nikhilpb.util.PropertySet;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */

public class MatchingModel {
  protected int supplyTypeDim, demandTypeDim, timePeriods;
  protected int[] supplyTypes, demandTypes;
  protected MultiIndependentDist supplyDistribution, demandDistribution;
  protected RewardFunction rewardFunction;
  protected String modelType;
  protected Random random;
  protected double supplyDepartureRate, demandDepartureRate, meanArrivalCount, sodBias, initPopParam;

  public MatchingModel(PropertySet props) {
    init(props);
  }

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

  public double getInitPopParam() {
    return initPopParam;
  }

  public int getTimePeriods() {
    return timePeriods;
  }

  public ArrayList<Item> sampleSupplyTypes(int n) {
    ArrayList<Item> supplyTypesSampled = new ArrayList<Item>(n);
    for (int i = 0; i < n; i++) {
      Item type = new Item(supplyDistribution.nextSample());
      type.specifySod(1);
      supplyTypesSampled.add(type);
    }
    return supplyTypesSampled;
  }

  public ArrayList<Item> sampleDemandTypes(int n) {
    ArrayList<Item> demandTypesSampled = new ArrayList<Item>(n);
    for (int i = 0; i < n; i++) {
      Item type = new Item(demandDistribution.nextSample());
      type.specifySod(0);
      demandTypesSampled.add(type);
    }
    return demandTypesSampled;
  }

  protected void init(PropertySet props) {
    double[][] supProbs;
    double[][] demProbs;
    if (props.getBooleanDefault("supply_demand_equal", false)) {
      supplyTypeDim = props.getInt("supply_type_dimentions");
      demandTypeDim = supplyTypeDim;
      supplyTypes = new int[supplyTypeDim];
      demandTypes = new int[demandTypeDim];
      supProbs = new double[supplyTypeDim][];
      demProbs = new double[demandTypeDim][];
      for (int i = 0; i < supplyTypeDim; i++) {
        supplyTypes[i] = props.getIntDefault("st[" + (i + 1) + "]", 0);
        demandTypes[i] = props.getIntDefault("st[" + (i + 1) + "]", 0);
        supProbs[i] = new double[supplyTypes[i]];
        demProbs[i] = new double[demandTypes[i]];
        for (int j = 0; j < supplyTypes[i]; j++) {
          supProbs[i][j] = props.getDoubleDefault("sp[" + (i + 1) + "][" + (j + 1) + "]", 0.0);
          demProbs[i][j] = supProbs[i][j];
        }
      }
      Random supRandom = new Random(props.getLongDefault("supply_seed", 123L));
      Random demRandom = new Random(props.getLongDefault("demand_seed", 456L));
      supplyDistribution = new MultiIndependentDist(supProbs, supRandom);
      demandDistribution = new MultiIndependentDist(demProbs, demRandom);
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
      supplyDistribution = new MultiIndependentDist(supProbs, supRandom);
      demandDistribution = new MultiIndependentDist(demProbs, demRandom);
      if (props.getStringDefault("reward_function", "separable").equals("separable")) {
        rewardFunction = new SeparableRewardFunction();
      }
    }

    modelType = "general";
    supplyDepartureRate = props.getDoubleDefault("supply_departure_rate", 0.1);
    System.out.println("supply departure rate is " + supplyDepartureRate);
    demandDepartureRate = props.getDoubleDefault("demand_departure_rate", 0.1);
    System.out.println("demand departure rate is " + demandDepartureRate);
    meanArrivalCount = props.getDoubleDefault("mean_arrival_count", 5);
    System.out.println("mean arrival count " + meanArrivalCount);
    sodBias = props.getDoubleDefault("sod_bias", 0.5);
    System.out.println("each arrival is supply type with probability " + sodBias);
    timePeriods = props.getIntDefault("time_periods", 50);
    System.out.println("time periods " + timePeriods);
    initPopParam = props.getDoubleDefault("init_pop_param", 0.2);
  }

  public double getSupplyDepartureRate() {
    return supplyDepartureRate;
  }

  public double getDemandDepartureRate() {
    return demandDepartureRate;
  }

  public double getMeanArrivalCount() {
    return meanArrivalCount;
  }

  public double getSodBias() {
    return sodBias;
  }

  /**
   * Samples arrivals for a particular time period. The number of arrivals is geometric with
   * the specified parameter.
   *
   * @return ArrayList of sampled types
   */
  public ArrayList<Item> sampleArrivals() {
    int arrivalCount = DistributionsCCM.nextGeometric(random, 1.0 - 1.0 / meanArrivalCount);
    return this.sampleArrivals(arrivalCount);
  }

  /**
   * Samples arrivals for a particular time period. The number of arrivals is explicitly specified.
   *
   * @return ArrayList of sampled types
   */
  public ArrayList<Item> sampleArrivals(int arrivalCount) {
    int sod = 0;
    int sodCount = 0;
    for (int i = 0; i < arrivalCount; i++) {
      sod = DistributionsCCM.nextBinomial(random, 1, sodBias);
      sodCount += sod;
    }
    ArrayList<Item> sampledDemandArrivals = this.sampleDemandTypes(sodCount);
    ArrayList<Item> sampledSupplyArrivals = this.sampleSupplyTypes(arrivalCount - sodCount);
    for (int i = 0; i < sampledDemandArrivals.size(); i++) {
      sampledDemandArrivals.get(i).specifySod(0);
    }
    for (int i = 0; i < sampledSupplyArrivals.size(); i++) {
      sampledSupplyArrivals.get(i).specifySod(1);
      sampledDemandArrivals.add(sampledSupplyArrivals.get(i));
    }
    return sampledDemandArrivals;
  }

  /**
   * Samples departures for a particular time period.
   *
   * @param currentList - current list of items
   * @return ArrayList of integers of the same size as the input. Zero at a particular position
   * indicates departure.
   */
  public ArrayList<Integer> sampleDepartures(ArrayList<Item> currentList) {
    ArrayList<Integer> remaining = new ArrayList<Integer>(currentList.size());
    for (int i = 0; i < currentList.size(); i++) {
      if (currentList.get(i).isSod() == 0) {
        remaining.add(DistributionsCCM.nextBinomial(random, 1, 1 - demandDepartureRate));
      } else if (currentList.get(i).isSod() == 1) {
        remaining.add(DistributionsCCM.nextBinomial(random, 1, 1 - supplyDepartureRate));
      } else {
        System.out.println("supply or demand not specified");
        remaining.set(i, 1);
      }
    }
    return remaining;
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
