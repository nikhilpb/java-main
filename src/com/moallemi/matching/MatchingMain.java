package com.moallemi.matching;

import java.io.*;
import java.util.*;

import com.moallemi.util.*;
import com.moallemi.util.data.*;

public class MatchingMain extends CommandLineMain {
  MatchingModel model;
  ArrayList<Item> sampledSupplyTypes, sampledDemandTypes;
  ItemFunctionSet basisSetSupply, basisSetDemand;
  MatchingSolver solver;
  ArrayList<Pair<Item, Item>> sampledPairs;
  OnlinePolicy onlinePolicy;
  ItemFunction supplyDualFunction, demandDualFunction;
  IoGMSet instancesOfGM;
  Regression regression;
  int x;

  protected boolean processCommand(final CommandLineIterator cmd)
      throws Exception {
    String base = cmd.next();
    if (base.equals("model")) {
      String modelType = cmd.next();
      String fname = cmd.next();
      System.out.println("loading online matching model: " + fname);
      PropertySet props = new PropertySet(new File(fname));
      if (modelType.equals("online")) {
        model = new OnlineMatchingModel(props);
      } else if (modelType.equals("general")) {
        model = new GeneralMatchingModel(props);
      }
    } else if (base.equals("regress")) {
      int sampleCount = cmd.nextInt();
      int timePeriods = cmd.nextInt();
      double ipParam = cmd.nextDouble();
      long seed = cmd.nextLong();
      System.out.println("sampling " + sampleCount
          + " number of instances with time periods " + timePeriods
          + " and mean no of initial items " + (1.0 / ipParam));
      instancesOfGM = new IoGMSet((GeneralMatchingModel) model, timePeriods,
          ipParam, seed);
      instancesOfGM.sample(sampleCount);
      regression = new Regression(instancesOfGM, basisSetSupply,
          basisSetDemand, getCplexFactory());
      supplyDualFunction = regression.getSupplyDualFunction();
      demandDualFunction = regression.getDemandDualFunction();
    } else if (base.equals("sample")) {
      String sod = cmd.next();
      if (sod.equals("supply")) {
        int n = Integer.parseInt(cmd.next());
        System.out.println("sampling " + n + " supply types.");
        long sampleSeed = Long.parseLong(cmd.next());
        model.setSupRandomSeed(sampleSeed);
        sampledSupplyTypes = model.sampleSupplyTypes(n);
      } else if (sod.equals("demand")) {
        int n = Integer.parseInt(cmd.next());
        System.out.println("sampling " + n + " demand types.");
        long sampleSeed = Long.parseLong(cmd.next());
        model.setDemRandomSeed(sampleSeed);
        sampledDemandTypes = model.sampleDemandTypes(n);
      } else if (sod.equals("both")) {
        int n = Integer.parseInt(cmd.next());
        System.out.println("sampling " + n + " supply and demand types.");
        sampledSupplyTypes = model.sampleSupplyTypes(n);
        sampledDemandTypes = model.sampleDemandTypes(n);
      } else if (sod.equals("matched-pairs-greedy")) {
        int runs = Integer.parseInt(cmd.next());
        System.out.println("no of runs is " + runs);
        // This is used to sample known side
        long sampSeed = Long.parseLong(cmd.next());
        System.out.println("seed for sampling is " + sampSeed);
        Random sampler = new Random(sampSeed);
        model.setDemRandomSeed(sampler.nextLong());
        sampledPairs = new ArrayList<Pair<Item, Item>>();
        for (int r = 0; r < runs; r++) {
          System.out.println("run no: " + (r + 1));
          long seedThisMatch = sampler.nextLong();
          model.setDemRandomSeed(seedThisMatch);
          ArrayList<Item> sampledDemandTypesTemp = model
              .sampleDemandTypes(sampledSupplyTypes.size());
          PHMatchGreedy greedyPairs = new PHMatchGreedy(sampledSupplyTypes,
              sampledDemandTypesTemp, model.getRewardFunction());
          ArrayList<Pair<Item, Item>> sampledPairsTemp = greedyPairs
              .getMatchedPairs();
          sampledPairs.addAll(sampledPairsTemp);
        }
      } else if (sod.equals("matched-pairs-salp")) {
        String matchingType = cmd.next();
        System.out.println("sampling type is: " + matchingType);
        int runs = Integer.parseInt(cmd.next());
        System.out.println("no of runs is " + runs);
        // This is used to sample known side
        long sampSeed = Long.parseLong(cmd.next());
        System.out.println("seed for sampling is " + sampSeed);
        Random sampler = new Random(sampSeed);
        model.setDemRandomSeed(sampler.nextLong());
        sampledPairs = new ArrayList<Pair<Item, Item>>();
        for (int r = 0; r < runs; r++) {
          System.out.println("run no: " + (r + 1));
          long seedThisMatch = sampler.nextLong();
          model.setDemRandomSeed(seedThisMatch);
          ArrayList<Item> sampledDemandTypesTemp = model
              .sampleDemandTypes(sampledSupplyTypes.size());
          ArrayList<Pair<Item, Item>> sampledPairsTemp;
          if (matchingType.equals("greedy")) {
            PHMatchGreedy greedyPairs = new PHMatchGreedy(sampledSupplyTypes,
                sampledDemandTypesTemp, model.getRewardFunction());
            sampledPairsTemp = greedyPairs.getSalpPairs();
          } else {
            PHMatchLawler lawlerPairs = new PHMatchLawler(sampledSupplyTypes,
                sampledDemandTypesTemp, model.getRewardFunction());
            sampledPairsTemp = lawlerPairs.getSalpPairs();
          }
          sampledPairs.addAll(sampledPairsTemp);
        }
      } else if (sod.equals("matched-pairs")) {
        int demandTypeCount = Integer.parseInt(cmd.next());
        System.out.println("size of each matching is " + demandTypeCount);
        // These many runs of offline matching will be performed to generate
        // constraints
        int runs = Integer.parseInt(cmd.next());
        System.out.println("no of runs is " + runs);
        // This is used to sample known side
        long sampSeed = Long.parseLong(cmd.next());
        System.out.println("seed for sampling is " + sampSeed);
        Random sampler = new Random(sampSeed);
        model.setDemRandomSeed(sampler.nextLong());
        sampledPairs = new ArrayList<Pair<Item, Item>>();
        if (demandTypeCount > sampledSupplyTypes.size()) {
          System.out
              .print("supply types should be greater than or equal to demand types");
        } else if (sampledSupplyTypes.size() == demandTypeCount) {
          for (int r = 0; r < runs; r++) {
            long seedThisMatch = sampler.nextLong();
            model.setDemRandomSeed(seedThisMatch);
            ArrayList<Item> sampledDemandTypesTemp = model
                .sampleDemandTypes(demandTypeCount);
            PHMatchLawler offlineL = new PHMatchLawler(sampledSupplyTypes,
                sampledDemandTypesTemp, model.getRewardFunction());
            offlineL.solve();
            ArrayList<Pair<Item, Item>> sampledPairsTemp = offlineL
                .getMatchedPairs();
            sampledPairs.addAll(sampledPairsTemp);

          }
        } else {
          ArrayList<Item> subSampledSupplyTypes;
          for (int r = 0; r < runs; r++) {
            long seedThisMatch = sampler.nextLong();
            model.setDemRandomSeed(seedThisMatch);
            ArrayList<Item> sampledDemandTypesTemp = model
                .sampleDemandTypes(demandTypeCount);
            subSampledSupplyTypes = subSampleTypes(sampledSupplyTypes,
                demandTypeCount, sampler.nextLong());
            PHMatchLawler offlineL = new PHMatchLawler(subSampledSupplyTypes,
                sampledDemandTypesTemp, model.getRewardFunction());
            offlineL.solve();
            sampledPairs.addAll(offlineL.getMatchedPairs());
          }
        }
      } else if (sod.equals("instances-of-general")) {
        int sampleCount = cmd.nextInt();
        int timePeriods = cmd.nextInt();
        double ipParam = cmd.nextDouble();
        long seed = cmd.nextLong();
        System.out.println("sampling " + sampleCount
            + " number of instances with time periods " + timePeriods
            + " and mean no of initial items " + (1.0 / ipParam));
        instancesOfGM = new IoGMSet((GeneralMatchingModel) model, timePeriods,
            ipParam, seed);
        instancesOfGM.sampleAndMatch(sampleCount);
      } else {
        return false;
      }
    } else if (base.equals("basis")) {
      String basisType = cmd.next();
      if (basisType.equals("separable")) {
        String sod = cmd.next();
        if (sod.equals("both")) {
          int dimS = model.getSupplyDim();
          int[] typesPerDimS = model.getTypesPerDimSup();
          basisSetSupply = new ItemFunctionSet();
          for (int i = 0; i < dimS; i++) {
            for (int j = 0; j < typesPerDimS[i]; j++) {
              FirstOrderItemFunction tf = new FirstOrderItemFunction(i, j);
              basisSetSupply.add(tf);
            }
          }
          basisSetSupply.add(new ConstantItemFunction(1.0));
          int dimD = model.getDemandDim();
          int[] typesPerDimD = model.getTypesPerDimDem();
          basisSetDemand = new ItemFunctionSet();
          for (int i = 0; i < dimD; i++) {
            for (int j = 0; j < typesPerDimD[i]; j++) {
              FirstOrderItemFunction tf = new FirstOrderItemFunction(i, j);
              basisSetDemand.add(tf);
            }
          }
          basisSetDemand.add(new ConstantItemFunction(1.0));
        } else if (sod.equals("supply")) {
          int dimS = model.getSupplyDim();
          int[] typesPerDimS = model.getTypesPerDimSup();
          basisSetSupply = new ItemFunctionSet();
          for (int i = 0; i < dimS; i++) {
            for (int j = 0; j < typesPerDimS[i]; j++) {
              FirstOrderItemFunction tf = new FirstOrderItemFunction(i, j);
              basisSetSupply.add(tf);
            }
          }
        } else if (sod.equals("demand")) {
          int dimD = model.getDemandDim();
          int[] typesPerDimD = model.getTypesPerDimDem();
          basisSetDemand = new ItemFunctionSet();
          for (int i = 0; i < dimD; i++) {
            for (int j = 0; j < typesPerDimD[i]; j++) {
              FirstOrderItemFunction tf = new FirstOrderItemFunction(i, j);
              basisSetDemand.add(tf);
            }
          }

        } else {
          throw new RuntimeException("input should be supply, demand or both");
        }
        return true;
      } else if (basisType.equals("indicator")) {
        basisSetSupply = new ItemFunctionSet();
        for (int i = 0; i < sampledSupplyTypes.size(); i++) {
          IndicatorItemFunction tf = new IndicatorItemFunction(
              sampledSupplyTypes.get(i));
          basisSetSupply.add(tf);
        }
      } else {
        return false;
      }
    } else if (base.equals("solve")) {
      String method = cmd.next();
      if (method.equals("alp")) {
        System.out.println("solving the matching model by alp");
        solver = new AlpSolverOnline(getCplexFactory(), sampledSupplyTypes,
            sampledDemandTypes, sampledPairs, basisSetSupply, basisSetDemand,
            model.getRewardFunction());
      } else if (method.equals("salp")) {
        System.out.println("solving the matching model by salp");
        double eps = cmd.nextDouble();
        System.out.println("epsilon = " + eps);
        solver = new SalpSolverOnline(getCplexFactory(), sampledPairs,
            basisSetSupply, basisSetDemand, model.getRewardFunction(), eps);
      } else if (method.equals("dual-average")) {
        int matchCount = Integer.parseInt(cmd.next());
        System.out.println("no of matchings to be performed - " + matchCount);
        long matchingSeed = Long.parseLong(cmd.next());
        System.out.println("seed for the matching is - " + matchingSeed);
        solver = new DualAverageSolverOnline(getCplexFactory(),
            ((OnlineMatchingModel) model), sampledSupplyTypes, matchCount,
            matchingSeed);
      } else if (method.equals("lp-general")) {
        System.out
            .println("solving the general matching model by linear programming");
        double eps = cmd.nextDouble();
        solver = (MatchingSolver) (new LPSolverGM(getCplexFactory(),
            instancesOfGM, basisSetSupply, basisSetDemand,
            ((GeneralMatchingModel) model), eps));
      } else if (method.equals("sgd-general")) {
        System.out
            .println("solving the general matching model by gradient descent");
        SGDSolverGM.Config config = new SGDSolverGM.Config(cmd.nextDouble(),
            cmd.nextDouble(),
            cmd.nextDouble());
        solver = (MatchingSolver) (new SGDSolverGM(instancesOfGM,
            basisSetSupply, basisSetDemand,
            ((GeneralMatchingModel) model), config));
      } else
        throw new Exception("unknown solver type");
      if (solver.solve()) {
        supplyDualFunction = solver.getSupplyValue();
        demandDualFunction = solver.getDemandValue();
        System.out.println("solution successful.");
        System.out.println("supply function = " + supplyDualFunction.getName());
        System.out.println("demand function = " + demandDualFunction.getName());
      } else {
        throw new IllegalStateException("solution unsuccessful");
      }
    } else if (base.equals("offline-match")) {
      System.out.println("solving an instance of the offline match");
      PHMatchLP offline = new PHMatchLP(getCplexFactory(), sampledSupplyTypes,
          sampledDemandTypes, model.getRewardFunction());
      if (offline.solve()) {

        System.out.println("solution successful");
        offline.getMatchedPairs();
      } else {
        throw new IllegalStateException("solution unsuccessful");
      }
    } else if (base.equals("policy")) {
      String policyType = cmd.next();
      if (policyType.equals("dualfunction")) {
        onlinePolicy = new DualFunctionPolicy(supplyDualFunction,
            model.getRewardFunction());
      } else if (policyType.equals("greedy")) {
        onlinePolicy = new DualFunctionPolicy(new ConstantItemFunction(0.0),
            model.getRewardFunction());
      } else {
        return false;
      }
    } else if (base.equals("simvalue")) {
      String simType = cmd.next();
      if (simType.equals("dualpolicy-online")) {
        int runCount = Integer.parseInt(cmd.next());
        System.out.println("the total no of sim runs is " + runCount);
        long simValueSeed = Long.parseLong(cmd.next());
        System.out.println("simulation with the seed " + simValueSeed);
        model.setDemRandomSeed(simValueSeed);
        ArrayList<Item> remSupplyTypes, sampledDemandTypesSim;
        int n = sampledSupplyTypes.size();
        RewardFunction rewardFun = model.getRewardFunction();
        double meanReward = 0.0;
        IIDObservations iidObs = new IIDObservations();
        for (int r = 0; r < runCount; r++) {
          System.out.print("simulation run " + r + " - ");
          remSupplyTypes = new ArrayList<Item>(sampledSupplyTypes);
          sampledDemandTypesSim = model.sampleDemandTypes(n);
          double thisReward = 0.0;
          for (int t = 0; t < n; t++) {
            int selected = onlinePolicy.match(remSupplyTypes,
                sampledDemandTypesSim.get(t));
            Item selectedType = remSupplyTypes.remove(selected);
            thisReward += rewardFun.evaluate(selectedType,
                sampledDemandTypesSim.get(t));
          }
          iidObs.add(thisReward);
          meanReward = (meanReward * r + thisReward) / (r + 1);
          System.out.println("this reward: " + thisReward + ", mean reward: "
              + meanReward);
        }
        System.out.println("mean reward: " + iidObs.getMean()
            + ", variance of the reward: " + iidObs.getVariance());
      } else if (simType.equals("offline-match")) {
        int runCount = Integer.parseInt(cmd.next());
        System.out.println("the total no of sim runs is " + runCount);
        long simValueSeed = Long.parseLong(cmd.next());
        System.out.println("simulation with the seed " + simValueSeed);
        model.setDemRandomSeed(simValueSeed);
        int n = sampledSupplyTypes.size();
        RewardFunction rewardFun = model.getRewardFunction();
        double meanReward = 0.0;
        ArrayList<Item> sampledDemandTypesSim;
        IIDObservations iidObs = new IIDObservations();
        for (int r = 0; r < runCount; r++) {
          System.out.print("simulation run " + r + " - ");
          sampledDemandTypesSim = model.sampleDemandTypes(n);
          PHMatchLawler offMatch = new PHMatchLawler(sampledSupplyTypes,
              sampledDemandTypesSim, rewardFun);
          double thisReward = offMatch.solve();
          iidObs.add(thisReward);
          meanReward = (meanReward * r + thisReward) / (r + 1);
          System.out.println("this reward: " + thisReward + ", mean reward: "
              + meanReward + ", memory used: "
              + (Runtime.getRuntime().totalMemory() / (1024 * 1024)));

        }
        System.out.println("mean reward: " + iidObs.getMean()
            + ", variance of the reward is: " + iidObs.getVariance());
      } else if (simType.equals("dualpolicy-general")) {
        int runCount = cmd.nextInt();
        int timePeriods = cmd.nextInt();
        double ipParam = cmd.nextDouble();
        System.out.println("the total no of sim runs is " + runCount);
        System.out.println("time periods are " + timePeriods);
        System.out.println("initial population parameter is " + ipParam);
        long simValueSeed = Long.parseLong(cmd.next());
        System.out.println("simulation with the seed " + simValueSeed);
        IoGMSet testSet = new IoGMSet((GeneralMatchingModel) model,
            timePeriods, ipParam, simValueSeed);
        testSet.sample(runCount);
        double[] value = testSet.dualPolicyTest(supplyDualFunction,
            demandDualFunction, getCplexFactory());
        IIDObservations iidObs = new IIDObservations(value, runCount);
        System.out.println("mean reward: " + iidObs.getMean()
            + ", variance of the reward is: " + iidObs.getVariance());
      } else if (simType.equals("greedy-general")) {
        int runCount = cmd.nextInt();
        int timePeriods = cmd.nextInt();
        double ipParam = cmd.nextDouble();
        System.out.println("the total no of sim runs is " + runCount);
        System.out.println("time periods are " + timePeriods);
        System.out.println("initial population parameter is " + ipParam);
        long simValueSeed = Long.parseLong(cmd.next());
        System.out.println("simulation with the seed " + simValueSeed);
        IoGMSet testSet = new IoGMSet((GeneralMatchingModel) model,
            timePeriods, ipParam, simValueSeed);
        testSet.sample(runCount);
        double[] value = testSet.dualPolicyTest(new ConstantItemFunction(0.0),
            new ConstantItemFunction(0.0), getCplexFactory());
        IIDObservations iidObs = new IIDObservations(value, runCount);
        System.out.println("mean reward: " + iidObs.getMean()
            + ", variance of the reward is: " + iidObs.getVariance());
      } else {
        return false;
      }
    } else if (base.equals("upperbound")) {
      int outerSampleCount = Integer.parseInt(cmd.next());
      System.out.println("no of outer loops to be performed is - "
          + outerSampleCount);
      int innerSampleCount = Integer.parseInt(cmd.next());
      System.out.println("no of inner loops to be performed is - "
          + innerSampleCount);
      long simSeed = Long.parseLong(cmd.next());
      System.out.println("seed for the simulation is - " + simSeed);
      String surrValueF = cmd.next();
      int n = sampledSupplyTypes.size();
      Random sampler = new Random(simSeed);
      ArrayList<Item> outerSampledDemandTypes;
      IIDObservations iidObs = new IIDObservations();
      InnerLoop innerLoop;
      if (surrValueF.equals("rollout")) {
        innerLoop = new InnerLoopRollout(getCplexFactory(), sampledSupplyTypes,
            sampler, innerSampleCount, (OnlineMatchingModel) model,
            supplyDualFunction);

      } else if (surrValueF.equals("offline")) {
        innerLoop = new InnerLoopOffline(getCplexFactory(), sampledSupplyTypes,
            sampler, innerSampleCount, (OnlineMatchingModel) model);

      } else {
        return false;
      }
      for (int outer = 0; outer < outerSampleCount; outer++) {
        long outerSeed = sampler.nextLong();
        model.setDemRandomSeed(outerSeed);
        outerSampledDemandTypes = model.sampleDemandTypes(n);
        innerLoop.setOmega(outerSampledDemandTypes);
        double thisUpperBound = innerLoop.run();
        iidObs.add(thisUpperBound);
        innerLoop.clear();
        System.out.println("inner problem no " + outer + "." + " value - "
            + thisUpperBound + "," + " mean value - " + iidObs.getMean() + ".");
      }

      System.out.println("upper bound is - " + iidObs.getMean()
          + ", variance is - " + iidObs.getVariance());
    } else {
      return false;
    }
    return true;
  }

  public static void main(String[] argv) throws Exception {
    (new MatchingMain()).run(argv);
  }

  protected ArrayList<Item> subSampleTypes(ArrayList<Item> sampledTypes,
                                           int sampleSize, long seed) {
    Random random = new Random(seed);
    ArrayList<Item> out = new ArrayList<Item>();
    for (int s = 0; s < sampleSize; s++) {
      out.add(sampledTypes.get(random.nextInt(sampledTypes.size())));
    }
    return out;
  }

  protected class IIDObservations {
    ArrayList<Double> obs;

    public IIDObservations() {
      obs = new ArrayList<Double>();
    }

    public IIDObservations(double[] inobs, int obsCount) {
      obs = new ArrayList<Double>();
      for (int i = 0; i < obsCount; i++) {
        obs.add(inobs[i]);
      }
    }

    public void add(double newObs) {
      obs.add(newObs);
    }

    public double getMean() {
      if (obs.size() == 0) {
        return 0.0;
      } else {
        double mean = 0.0;
        for (int i = 0; i < obs.size(); i++) {
          mean = (mean * i + obs.get(i)) / (i + 1);
        }
        return mean;
      }
    }

    public double getVariance() {
      if (obs.size() == 0) {
        return 0.0;
      } else {
        double mean = this.getMean();
        double variance = 0.0;
        for (int i = 0; i < obs.size(); i++) {
          variance = (variance * i + (obs.get(i) - mean) * (obs.get(i) - mean))
              / (i + 1);
        }
        return variance;
      }

    }
  }
}
