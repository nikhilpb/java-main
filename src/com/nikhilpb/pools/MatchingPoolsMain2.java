package com.nikhilpb.pools;

import com.nikhilpb.util.CplexFactory;
import com.nikhilpb.util.PropertySet;
import com.nikhilpb.util.XmlParser;
import ilog.cplex.IloCplex;

import java.io.File;
import java.util.Properties;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/10/13
 * Time: 6:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchingPoolsMain2 extends XmlParser {

  private static MatchingPoolsModel model;
  private static InstanceSet instances;
  private static NodeFunctionSet basisSet;
  private static LpSolver solver;
  private static NodeFunction valueFunction;

  public static void main(String[] args) {
    CommandLineHandler handler = new CommandLineHandler() {
      @Override
      public boolean handleCommandLine(String[] args) throws Exception {
        return true;
      }
    };

    CommandProcessor modelProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return modelCommand(props);
      }
    };
    registerCommand("model", modelProcessor);

    CommandProcessor sampleProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return sampleCommand(props);
      }
    };
    registerCommand("sample", sampleProcessor);

    CommandProcessor basisProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return basisCommand(props);
      }
    };
    registerCommand("basis", basisProcessor);

    CommandProcessor solverProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return solverCommand(props);
      }
    };
    registerCommand("solve", solverProcessor);

    CommandProcessor evaluateProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return evaluateCommand(props);
      }
    };
    registerCommand("evaluate", evaluateProcessor);

    parseCommandLine(args, handler);
    executeCommands();
  }

  private static boolean modelCommand(Properties props) throws Exception {
    String fname = getPropertyOrDie(props, "file");
    System.out.println("loading online matching model: " + fname);
    PropertySet modelProps = new PropertySet(new File(fname));
    model = new MatchingPoolsModel(modelProps);
    return true;
  }

  private static boolean sampleCommand(Properties props) throws Exception {
    if (model == null) {
      throw new RuntimeException("model is null");
    }
    System.out.println("sampling kidney pools");
    int sampleSize = Integer.parseInt(getPropertyOrDie(props, "sample_size"));
    System.out.println("sample size: " + sampleSize);
    long sampleSeed = Long.parseLong(getPropertyOrDie(props, "sample_seed"));
    System.out.println("sample seed is: " + sampleSeed);
    instances = new InstanceSet(model, sampleSeed);
    instances.sample(sampleSize);
    instances.match("greedy", new CplexFactory());
    return true;
  }

  private static boolean basisCommand(Properties props) {
    if (model == null) {
      throw new RuntimeException("model is null");
    }
    String basisType = getPropertyOrDie(props, "type");
    if (basisType.equals("separable")) {
      int dim = model.getDimentsion();
      int[] typesPerDim = model.getTissues();
      basisSet = new NodeFunctionSet();
      for (int fos = 0; fos < 2; fos++) {
        for (int i = 0; i < dim; i++) {
          for (int j = 0; j < typesPerDim[i]; j++) {
            FirstOrderNodeFunction nf = new FirstOrderNodeFunction(i, j, fos);
            basisSet.add(nf);
          }
        }
      }
      basisSet.add(new ConstantNodeFunction(1.0));
    }
    return true;
  }

  private static boolean solverCommand(Properties props) throws Exception {
    if (model == null || instances == null || basisSet == null) {
      throw new RuntimeException("model, sample and basis commands must be used");
    }
    String solverType = getPropertyOrDie(props, "type");
    if (solverType.equals("salp")) {
      System.out.println("solving salp");
      double eps = Double.parseDouble(getPropertyOrDie(props, "eps"));
      System.out.println("epsilon = " + eps);
      solver = new LpSolver(new CplexFactory(), instances, basisSet, model, eps);
      boolean status = solver.solve();
      if (status) {
        System.out.println("successfully solved model");
      }
      valueFunction = solver.getValue();
    } else {
      System.err.println("incorrect solver type");
    }
    return true;
  }

  private static boolean evaluateCommand(Properties props) throws Exception {
    String policyType = getPropertyOrDie(props, "type");
    NodeRewardFunction nrf = null;
    if (policyType.equals("greedy")) {
      System.out.println("matching using greedy policy");
      nrf = model.getNodeRewardFunction();
    } else if (policyType.equals("vf")) {
      System.out.println("matching using value function policy");
      nrf = new NodeRewardFunction(model.getNodeRewardFunction().getRf(),
                                          valueFunction,
                                          1 - model.getDepartureRate());
    } else if (policyType.equals("offline")) {
      System.out.println("matching using the offline policy");
    }
    int runCount = Integer.getInteger(getPropertyOrDie(props, "run_count"));
    System.out.println("number of sample runs is " + runCount);
    long simValueSeed = Long.parseLong(getPropertyOrDie(props, "seed"));
    System.out.println("using seed " + simValueSeed);
    Random svRandom = new Random(simValueSeed);
    double[] value = new double[runCount];
    SampleInstance inst;
    CplexFactory factory = new CplexFactory();
    IloCplex cplex = factory.getCplex();
    double mean = 0.0, std = 0.0, sterr = 0.0;
    for (int s = 0; s < runCount; s++) {
      inst = new SampleInstance(model, svRandom.nextLong());
      inst.sample();
      if (policyType.equals("offline")) {
        value[s] = inst.offlineMatch(cplex);
      } else {
        value[s] = inst.greedyMatch(factory, nrf);
      }
      mean += value[s];
    }
    mean = mean / runCount;
    for (int s = 0; s < runCount; s++) {
      std += Math.pow((value[s] - mean), 2);
    }
    std = Math.pow(std / runCount, 0.5);
    sterr = std / Math.pow(runCount, 0.5);
    System.out.println("mean reward: " + mean
                               + ", standard deviation: " + std
                               + ", standard error: " + sterr);
    return true;
  }
}
