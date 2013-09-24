package com.moallemi.matching;

import java.util.*;

import com.moallemi.util.PropertySet;
import com.moallemi.math.Distributions;

/**
 * General bipartite matching model with arrivals and departures. Apart from the online matching model
 * it allows for arrivals and departures, and at each time period more than one pairs can be matched.
 *
 * @author Nikhil Bhat
 * @version $Revision: 0.1 $, $Date: 2012-09-20 $
 */

public class GeneralMatchingModel extends MatchingModel {

  private double supplyDepartureRate, demandDepartureRate, meanArrivalCount, sodBias;

  /**
   * Constructor.
   *
   * @param props - set of properties
   */
  public GeneralMatchingModel(PropertySet props) {
    init(props);
    modelType = "general";
    supplyDepartureRate = props.getDoubleDefault("supply_departure_rate", 0.1);
    System.out.println("supply departure rate is " + supplyDepartureRate);
    demandDepartureRate = props.getDoubleDefault("demand_departure_rate", 0.1);
    System.out.println("demand departure rate is " + demandDepartureRate);
    meanArrivalCount = props.getDoubleDefault("mean_arrival_count", 5);
    System.out.println("mean arrival count " + meanArrivalCount);
    sodBias = props.getDoubleDefault("sod_bias", 0.5);
    System.out.println("each arrival is supply type with probability " + sodBias);

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
    int arrivalCount = Distributions.nextGeometric(random, 1.0 - 1.0 / meanArrivalCount);
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
      sod = Distributions.nextBinomial(random, 1, sodBias);
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
   *         indicates departure.
   */
  public ArrayList<Integer> sampleDepartures(ArrayList<Item> currentList) {
    ArrayList<Integer> remaining = new ArrayList<Integer>(currentList.size());
    for (int i = 0; i < currentList.size(); i++) {
      if (currentList.get(i).isSod() == 0) {
        remaining.add(Distributions.nextBinomial(random, 1, 1 - demandDepartureRate));
      } else if (currentList.get(i).isSod() == 1) {
        remaining.add(Distributions.nextBinomial(random, 1, 1 - supplyDepartureRate));
      } else {
        System.out.println("supply or demand not specified");
        remaining.set(i, 1);
      }
    }
    return remaining;
  }
}