package com.nikhilpb.matching;

import com.nikhilpb.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/25/13
 * Time: 10:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class SalpConstraint {
  private double[] coeffKappaS, coeffKappaD;
  private double rhs;

  public SalpConstraint(MatchingModel model,
                        ItemFunctionSet basisSetSupply,
                        ItemFunctionSet basisSetDemand,
                        ArrayList<Item> items,
                        ArrayList<Pair<Item, Item>> matches,
                        boolean lastOne) {
    rhs = 0.0;
    for (int i = 0; i < matches.size(); i++) {
      rhs += model.getRewardFunction().evaluate(matches.get(i).getFirst(),
                                                       matches.get(i).getSecond());
    }
    coeffKappaS = new double[basisSetSupply.size()];
    coeffKappaD = new double[basisSetDemand.size()];
    Arrays.fill(coeffKappaS, 0.0);
    Arrays.fill(coeffKappaD, 0.0);

    double supplyDepRate, demandDepRate;
    if (! lastOne) {
      supplyDepRate = model.getSupplyDepartureRate();
      demandDepRate = model.getDemandDepartureRate();
    } else {
      supplyDepRate = 1;
      demandDepRate = 1;
    }

    for (int i = 0; i < items.size(); i++) {
      Item item = items.get(i);
      if (item.isSod() == 1) {
        double[] eval = basisSetSupply.evaluate(item);
        for (int j = 0; j < basisSetSupply.size(); j++) {
          coeffKappaS[j] += eval[j] * supplyDepRate;
        }
      } else if (item.isSod() == 0) {
        double[] eval = basisSetDemand.evaluate(item);
        for (int j = 0; j < basisSetDemand.size(); j++) {
          coeffKappaD[j] += eval[j] * demandDepRate;
        }
      } else {
        throw new RuntimeException("the item sod type not specified");
      }
    }

    for (int i = 0; i < matches.size(); i++) {
      Item firstItem = matches.get(i).getFirst();
      Item secondItem = matches.get(i).getSecond();
      double[] eval = basisSetSupply.evaluate(firstItem);
      double[] eval2 = basisSetDemand.evaluate(secondItem);
      for (int j = 0; j < basisSetSupply.size(); j++) {
        coeffKappaS[j] += eval[j] * (1 - supplyDepRate);
      }
      for (int j = 0; j < basisSetDemand.size(); j++) {
        coeffKappaD[j] += eval2[j] * (1 - demandDepRate);
      }
    }
  }


  /**
   * @return constant of the constraint
   */
  public double getRhs() {
    return rhs;
  }

  /**
   * @return an array
   */
  public double[] getKappa1Coeff() {
    return coeffKappaS;
  }

  public double[] getKappa2Coeff() {
    return coeffKappaD;
  }

  public boolean satisfied(double[] kappaS, double[] kappaD) {
    double value = 0.0;
    for (int i = 0; i < kappaS.length; ++ i) {
      value += kappaS[i] * coeffKappaS[i];
    }
    for (int i = 0; i < kappaD.length; ++ i) {
      value += kappaD[i] * coeffKappaD[i];
    }
    return value >= rhs;
  }

}
