package com.nikhilpb.abtesting;

import Jama.Matrix;
import com.nikhilpb.doe.*;
import com.nikhilpb.util.Experiment;
import com.nikhilpb.util.math.PSDMatrix;
import com.nikhilpb.util.math.Series;

import java.util.Properties;

/**
 * Created by nikhilpb on 8/27/14.
 *
 *
 */
public class ABTestingExperiment extends Experiment {
  private static Experiment instance = null;
  private static String name = "abtesting";

  private DataModel dataModel;
  private ABPolicy policy;

  public static Experiment getInstance() {
    if (instance == null) {
      instance = new ABTestingExperiment();
    }
    return instance;
  }

  public static void register() {
    registerExperiment(name, getInstance());
  }

  private ABTestingExperiment() {
    super();

    CommandProcessor modelProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return modelCommand(props);
      }
    };
    registerCommand("model", modelProcessor);

    CommandProcessor policyProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return policyCommand(props);
      }
    };
    registerCommand("policy", policyProcessor);

    CommandProcessor evalProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return evalCommand(props);
      }
    };
    registerCommand("eval", evalProcessor);
  }

  protected boolean modelCommand(Properties props) {
    final String type = getPropertyOrDie(props, "type");
    if (type.equals("gaussian")) {
      dataModel = getGaussianModel(props);
    } else if (type.equals("yahoo")) {
      dataModel = null; //TODO(nikhilpb): finish this.
    } else {
      throw new RuntimeException("No model type: " + type + " found.");
    }
    return true;
  }

  protected boolean policyCommand(Properties props) {
    final String type = getPropertyOrDie(props, "type");
    if (type.equals("random")) {
      final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
      policy = new RandomPolicy(seed);
    } else {
      policy = null; //TODO(nikhilpb): finish this
    }
    return true;
  }

  protected boolean evalCommand(Properties props) {
    assert dataModel != null;
    assert policy != null;

    final int trialCount = Integer.parseInt(getPropertyOrDie(props, "trial_count"));
    final int timePeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
    SequentialProblemStats stats;
    Series neSeq = new Series(),
            effSeq = new Series(),
            perEffSeq = new Series(),
            aneSeq = new Series(),
            reSeq = new Series();
    ABTestingDP abTestingDP = new ABTestingDP(dataModel);
    ABState state = abTestingDP.getBase();
    for (int i = 0; i < trialCount; ++i) {
      stats = new SequentialProblemStats(dataModel);
      for (int t = 0; t < timePeriods; ++t) {
        ABAction action = policy.getAction(state);
        stats.addPoint(state.getDp(), action);
        state = abTestingDP.next(state, action);
      }
      stats.aggregate();

      neSeq.add(stats.getNormErr());
      effSeq.add(stats.getEfficiency());
      perEffSeq.add(stats.getPerEfficiency());
      aneSeq.add(stats.getApproxNormErr());
      reSeq.add(stats.getRandEf());
    }

    System.out.println("Approx Norm Err: " + aneSeq.getMean());
    System.out.println("Norm Err: " + neSeq.getMean() + ", Std err: " + neSeq.getStdDev());
    System.out.println("Efficiency: " + effSeq.getMean() + ", Std err: " + effSeq.getStdDev());
    System.out.println("PerEfficiency: " + perEffSeq.getMean() + ", Std err: " + perEffSeq.getStdDev());
    System.out.println("RandEff: " + reSeq.getMean() + ", Std err: " + reSeq.getStdDev());
    return true;
  }

  /**
   * From the PropertySet get the relevant parameters and return a Gaussian model.
   * @param props
   * @return
   */
  private GaussianModel getGaussianModel(Properties props) {
    final int dim = Integer.parseInt(getPropertyOrDie(props, "dim"));
    final double sigma = Double.parseDouble(getPropertyOrDie(props, "sigma"));
    final double rho = Double.parseDouble(getPropertyOrDie(props, "rho"));
    final int timePeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
    final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
    double[][] muArray = new double[dim - 1][1];
    Matrix mu = new Matrix(muArray);
    double[][] sigmaArray = new double[dim - 1][dim - 1];
    // Construct the covariance matrix.
    for (int i = 0; i < dim - 1; ++ i) {
      for (int j = 0; j < dim - 1; ++ j) {
        if (i == j) {
          sigmaArray[i][j] = sigma * sigma;
        } else {
          sigmaArray[i][j] = sigma * sigma * rho;
        }
      }
    }
    PSDMatrix sigmaMatrix = new PSDMatrix(sigmaArray);
    return new GaussianModel(mu, sigmaMatrix, timePeriods, seed);
  }
}
