package com.nikhilpb.abtesting;

import Jama.Matrix;

import com.nikhilpb.util.Experiment;
import com.nikhilpb.util.math.PSDMatrix;
import com.nikhilpb.util.math.Series;

import java.util.Properties;

/**
 * Created by nikhilpb on 8/27/14.
 *
 */
public class ABTestingExperiment extends Experiment {
  private static Experiment instance = null;
  private static String name = "abtesting";

  private DataModel dataModel;
  private ABPolicy policy;
  private ContinuationValueCollection continuationValue;

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

    CommandProcessor continuationValueProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return continuationValueProcessor(props);
      }
    };
    registerCommand("cont_value", continuationValueProcessor);

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
    } else if (type.equals("click")) {
      final String trainFile = getPropertyOrDie(props, "train_file");
      final String testFile = getPropertyOrDie(props, "test_file");
      final int userCount = Integer.parseInt(getPropertyOrDie(props, "user_count"));
      final int dim = Integer.parseInt(getPropertyOrDie(props, "dim"));
      final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
      try {
        dataModel = new ClickDataModel(trainFile, testFile, userCount, dim, seed);
      } catch (Exception e) {
        e.printStackTrace();
      }
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
    } else if (type.equals("myopic")) {
      policy = new MyopicPolicy(dataModel.getSigma().inverse());
    } else {
      policy = null;
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
    ABModel abModel = new ABModel(dataModel);
    ABState state = abModel.getBase();
    int count = 0, failCount = 0;
    while (count < trialCount) {
      stats = new SequentialProblemStats(dataModel);
      for (int t = 0; t < timePeriods; ++t) {
        ABAction action = policy.getAction(state);
        stats.addPoint(state.getDp(), action);
        state = abModel.next(state, action);
      }
      if (!stats.aggregate()) {
        failCount++;
        continue;
      }

      neSeq.add(stats.getNormErr());
      effSeq.add(stats.getEfficiency());
      perEffSeq.add(stats.getPerEfficiency());
      aneSeq.add(stats.getApproxNormErr());
      reSeq.add(stats.getRandEf());
      count++;
    }
    System.out.println(failCount + " no of failed attempts");

    System.out.println("Approx Norm Err: " + aneSeq.getMean());
    System.out.println("Norm Err: " + neSeq.getMean() + ", Std err: " + neSeq.getStdDev());
    System.out.println("Efficiency: " + effSeq.getMean() + ", Std err: " + effSeq.getStdDev());
    System.out.println("PerEfficiency: " + perEffSeq.getMean() + ", Std err: " + perEffSeq.getStdDev());
    System.out.println("RandEff: " + reSeq.getMean() + ", Std err: " + reSeq.getStdDev());
    return true;
  }

  private boolean continuationValueProcessor(Properties props) {
    final int dimension = Integer.parseInt(getPropertyOrDie(props, "dim"));
    final int timePeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
    final double lambdaMax = Double.parseDouble(getPropertyOrDie(props, "lambda_max"));
    final int discretePointsCount = Integer.parseInt(getPropertyOrDie(props, "discrete_points_count"));
    final int simPointsCount = Integer.parseInt(getPropertyOrDie(props, "sim_points_count"));
    final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
    continuationValue =
            new ContinuationValueCollection(dimension, timePeriods, lambdaMax,
                                            discretePointsCount, simPointsCount, seed);
    continuationValue.evaluate();
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
    final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
    double[][] muArray = new double[dim - 1][1];
    Matrix mu = new Matrix(muArray);
    double[][] sigmaArray = new double[dim - 1][dim - 1];
    // Construct the covariance matrix.
    for (int i = 0; i < dim - 1; ++i) {
      for (int j = 0; j < dim - 1; ++j) {
        if (i == j) {
          sigmaArray[i][j] = sigma * sigma;
        } else {
          sigmaArray[i][j] = sigma * sigma * rho;
        }
      }
    }
    PSDMatrix sigmaMatrix = new PSDMatrix(sigmaArray);
    return new GaussianModel(mu, sigmaMatrix, seed);
  }

}
