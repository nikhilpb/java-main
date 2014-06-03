package com.nikhilpb.doe;

import Jama.Matrix;
import com.nikhilpb.util.Experiment;
import com.nikhilpb.util.XmlParser;
import com.nikhilpb.util.math.IIDSeq;
import com.nikhilpb.util.math.PSDMatrix;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 4/15/14
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class DoeExperiment extends Experiment {
    private int dim;
    private int sampleCount;
    private double upper;
    private double printUpper;
    private int pointsCount;
    private long seed;
    private int timePeriods;
    private OneDFunction[] qFuns;
    private GaussianModel gaussianModel;


    private static Experiment instance = null;

    public static Experiment getInstance() {
        if(instance == null) {
            instance = new DoeExperiment();
        }
        return instance;
    }

    private DoeExperiment() {
        super();

        CommandProcessor modelProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return modelCommand(props);
            }
        };
        registerCommand("model", modelProcessor);

        CommandProcessor solveProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return solveCommand(props);
            }
        };
        registerCommand("solve", solveProcessor);

        CommandProcessor printProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return printCommand(props);
            }
        };
        registerCommand("print", printProcessor);

        CommandProcessor printMinProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return printMinCommand(props);
            }
        };
        registerCommand("print_mins", printMinProcessor);

        CommandProcessor gModelProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return gaussianModelCommand(props);
            }
        };
        registerCommand("gmodel", gModelProcessor);

        CommandProcessor evaluatePolicyProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return evaluatePolicyCommand(props);
            }
        };
        registerCommand("evaluate_policy", evaluatePolicyProcessor);

    }

    public boolean modelCommand(Properties props) {
        dim = Integer.parseInt(getPropertyOrDie(props, "dim"));
        timePeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
        return true;
    }

    public boolean solveCommand(Properties props) {
        sampleCount = Integer.parseInt(getPropertyOrDie(props, "sample_count"));
        upper = Double.parseDouble(getPropertyOrDie(props, "upper"));
        pointsCount = Integer.parseInt(getPropertyOrDie(props, "points_count"));
        seed = Long.parseLong(getPropertyOrDie(props, "seed"));
        QFunctionRecursion.PolicyType policyType;
        String ptString = getPropertyOrDie(props, "policy_type");
        if (ptString.equals("myopic")) {
            policyType = QFunctionRecursion.PolicyType.MYOPIC;
        } else {
            policyType = QFunctionRecursion.PolicyType.OPTIMAL;
        }

        qFuns = new OneDFunction[timePeriods];
        qFuns[0] = new IdentityFunction();

        for (int i = 1; i < timePeriods; ++i) {
            System.out.println("Time Period no: " + i);
            qFuns[i] = QFunctionRecursion.recurse(qFuns[i-1], dim, upper, pointsCount, seed, sampleCount, policyType);
        }
        return true;
    }

    public boolean printCommand(Properties props) {
        printUpper = Double.parseDouble(getPropertyOrDie(props, "print_upper"));
        String baseName = getPropertyOrDie(props, "base_name");
        try {
            for (int i = 1; i < timePeriods; ++i) {
                qFuns[i].printFn(
                        new PrintStream(new FileOutputStream("results/doe/" + baseName + "-dim-" + dim + "-tp-" + i + ".csv")),
                        0.,
                        printUpper);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    public boolean printMinCommand(Properties props) {
        String baseName = getPropertyOrDie(props, "base_name");
        double searchUpper = Double.parseDouble(getPropertyOrDie(props, "search_upper"));
        String fileName = "results/doe/" + baseName + "-dim-" + dim + ".csv";
        try {
            PrintStream stream = new PrintStream(new FileOutputStream(fileName));
            for (int t = 0; t < timePeriods; ++t) {
                stream.println(t + "," + qFuns[t].minAt(0., searchUpper));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean gaussianModelCommand(Properties props) {
        final int dim = Integer.parseInt(getPropertyOrDie(props, "dim"));
        final double sigma = Double.parseDouble(getPropertyOrDie(props, "sigma"));
        final double rho = Double.parseDouble(getPropertyOrDie(props, "rho"));
        final int timePeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
        final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
        double[][] muArray = new double[dim-1][1];
        Matrix mu = new Matrix(muArray);
        double[][] sigmaArray = new double[dim-1][dim-1];
        for (int i = 0; i < dim-1; ++i) {
            for (int j = 0; j < dim-1; ++j) {
                if (i == j) {
                    sigmaArray[i][j] = sigma * sigma;
                } else {
                    sigmaArray[i][j] = sigma * sigma * rho;
                }

            }
        }
        PSDMatrix sigmaMatrix = new PSDMatrix(sigmaArray);
        gaussianModel = new GaussianModel(mu, sigmaMatrix, timePeriods, seed);
        return true;
    }

    public boolean evaluatePolicyCommand(Properties props) {
        if (gaussianModel == null) {
            throw new RuntimeException("Gaussian Model not initialized");
        }
        String policyType = getPropertyOrDie(props, "policy_type");

        Policy policy;
        if (policyType.equals("myopic")) {
            policy = new MyopicPolicy(gaussianModel.getCovarMatrix());
        } else {
            long randSeed = Long.parseLong(getPropertyOrDie(props, "seed"));
            policy = new RandomPolicy(randSeed);
        }

        int trialCount = Integer.parseInt(getPropertyOrDie(props, "trial_count"));
        PSDMatrix sigmaInv = gaussianModel.getCovarMatrix().inverse();
        double[] values = new double[trialCount];
        IIDSeq neSeq = new IIDSeq(), effSeq = new IIDSeq(), perEffSeq = new IIDSeq(), aneSeq = new IIDSeq();
        for (int r = 0; r < trialCount; ++r) {
            SequentialProblemStats stats = new SequentialProblemStats(gaussianModel.getDim() + 1,
                    gaussianModel.getCovarMatrix().mat());
            double[] state = new double[gaussianModel.getDim()];
            int diff = 0;
            for (int t = 0; t < gaussianModel.getTimePeriods(); ++t) {
                DataPoint dp = gaussianModel.next();
                int action = policy.getAction(state, diff, dp);
                diff += action;
                stats.addPoint(dp, action);
                for (int i = 0; i < gaussianModel.getDim(); ++i) {
                    state[i] += action * dp.get(i);
                }
            }
            stats.aggregate();

            neSeq.add(stats.getNormErr());
            effSeq.add(stats.getEfficiency());
            perEffSeq.add(stats.getPerEfficiency());
            aneSeq.add(stats.getApproxNormErr());

            values[r] = valueAt(state, sigmaInv);
        }
        System.out.println("Approx Norm Err: " + aneSeq.getMean());
        System.out.println("Norm Err: " + neSeq.getMean() + ", Std err: " + neSeq.getStdDev());
        System.out.println("Efficiency: " + effSeq.getMean() + ", Std err: " + effSeq.getStdDev());
        System.out.println("PerEfficiency: " + perEffSeq.getMean() + ", Std err: " + perEffSeq.getStdDev());

        double mean = 0.;

        for (int i = 0; i < values.length; ++i) {
            mean += values[i];
        }
        mean = mean / values.length;

        double std = 0.;
        for (int i = 0; i < values.length; ++i) {
            std += (values[i] - mean) * (values[i] - mean);
        }
        std = Math.sqrt(std / values.length);

        double stdErr = std / Math.sqrt(values.length);

        System.out.println("Mean: " + mean + ", Standard Error: " + stdErr);
        return true;


    }

    public static interface Policy {
        public int getAction(double[] state, int diff, DataPoint nextPoint);
    }

    public static class MyopicPolicy implements Policy {
        private PSDMatrix sigmaInv;

        public MyopicPolicy(PSDMatrix sigmaMatrix) {
            sigmaInv = sigmaMatrix.inverse();
        }

        @Override
        public int getAction(double[] state, int diff, DataPoint nextPoint) {
            int action = 1;
            double addValue = 0., subValue = 0.;
            int dim = state.length;
            double[] addVector = new double[dim], subVector = new double[dim];
            double[] dataPointVec = nextPoint.getDataVector();
            for (int i = 0; i < dim; ++i) {
                addVector[i] = state[i] + dataPointVec[i];
                subVector[i] = state[i] - dataPointVec[i];
            }

            for (int i = 0; i < dim; ++i) {
                for (int j = 0; j < dim; ++j) {
                    addValue += addVector[i] * addVector[j] * sigmaInv.get(i, j);
                    subValue += subVector[i] * subVector[j] * sigmaInv.get(i, j);
                }
            }
            addValue += (diff + 1) * (diff + 1);
            subValue += (diff - 1) * (diff - 1);
            if (subValue < addValue) {
                action = -1;
            } else {
                action = 1;
            }
            return action;
        }
    }

    public static class RandomPolicy implements Policy {
        private Random random;

        public RandomPolicy(long seed) {
            random = new Random(seed);
        }

        @Override
        public int getAction(double[] state, int diff, DataPoint nextPoint) {
            boolean rndBool = random.nextBoolean();
            if (rndBool) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private double valueAt(double[] state, PSDMatrix sigmaInv) {
        double value = 0.;
        for (int i = 0; i < state.length; ++i) {
            for (int j = 0; j < state.length; ++j) {
                value += state[i] * state[j] * sigmaInv.get(i, j);
            }
        }
        return value;
    }
}
