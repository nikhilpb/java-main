package com.nikhilpb.stopping;

import Jama.Matrix;
import com.nikhilpb.adp.*;
import com.nikhilpb.util.XmlParserMain;
import com.nikhilpb.util.math.PSDMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/24/13
 * Time: 3:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingMain extends XmlParserMain {
    private static StoppingModel model;
    private static RewardFunction rewardFunction;
    private static BasisSet basisSet;
    private static ArrayList<SamplePath> samplePath;
    private static Solver solver;
    private static Policy policy;

    public static void main(String[] args) {
        HashMap<String, CommandProcessor> cmdMap = new HashMap<String, CommandProcessor>();

        CommandProcessor modelProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return modelCommand(props);
            }
        };
        cmdMap.put("model", modelProcessor);

        CommandProcessor basisProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return basisCommand(props);
            }
        };
        cmdMap.put("basis", basisProcessor);

        CommandProcessor solveProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return solveCommand(props);
            }
        };
        cmdMap.put("solve", solveProcessor);

        CommandProcessor evalProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return evalCommand(props);
            }
        };
        cmdMap.put("eval", evalProcessor);

        parseCommandLine(args, null);
        executeCommands(cmdMap);
    }

    public static boolean modelCommand(Properties props) {
        double S = Math.log(Double.parseDouble(getPropertyOrDie(props, "S")));
        double delta = Double.parseDouble(getPropertyOrDie(props, "delta"));
        double sigma = Double.parseDouble(getPropertyOrDie(props, "sigma"));
        double rho = Double.parseDouble(getPropertyOrDie(props, "rho"));
        int n = Integer.parseInt(getPropertyOrDie(props, "n"));
        int timPeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
        double[][] muArray = new double[n][1];
        double[][] sigmaArray = new double[n][n];
        double[] initValue = new double[n];
        Arrays.fill(initValue, S);
        for (int i = 0; i < n; ++i) {
            muArray[i][0] = delta;
            Arrays.fill(sigmaArray[i], rho * sigma * sigma);
            sigmaArray[i][i] = sigma * sigma;
        }
        Matrix mu = new Matrix(muArray);
        PSDMatrix covar = new PSDMatrix(sigmaArray);
        String rewardType = getPropertyOrDie(props, "reward_type");
        if (rewardType.equals("max-call")) {
            rewardFunction =
                new MaxCallReward(Double.parseDouble(getPropertyOrDie(props, "K")));
        } else if (rewardType.equals("max-put")) {
            rewardFunction =
                new MaxPutReward(Double.parseDouble(getPropertyOrDie(props, "K")));
        } else {
            throw new RuntimeException("unkown reward function");
        }
        long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
        model = new StoppingModel(mu, covar, initValue, timPeriods, rewardFunction, seed);
        return true;
    }

    public static boolean basisCommand(Properties props) {
        if (model == null) {
            throw new RuntimeException("model must be initialized");
        }
        String basisType = getPropertyOrDie(props, "type");
        if (basisType.equals("polynomial")) {
            int degree = Integer.parseInt(getPropertyOrDie(props, "degree"));
            basisSet = new BasisSet();
            basisSet.add(new ConstantStateFunction(1.));
            int modelDim = model.dimension();
            for (int d = 1; d <= degree; ++d) {
                for (int i = 0; i < modelDim; ++i) {
                    basisSet.add(new PolyStateFunction(d, i));
                }
            }
        } else {
            throw new RuntimeException("unknown basis type" + basisType);
        }
        return true;
    }

    public static boolean solveCommand(Properties props) {
        if (model == null) {
            throw new RuntimeException("model must be initialized");
        }
        String solverType = getPropertyOrDie(props, "type");
        if (solverType.equals("ls")) {
            int sampleCount = Integer.parseInt(getPropertyOrDie(props, "sample_count"));
            long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
            solver = new LongstaffSchwartzSolver(model, basisSet, seed, sampleCount);
        } else if (solverType.equals("kernel")) {
            int sampleCount = Integer.parseInt(getPropertyOrDie(props, "sample_count"));
            long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
            double gamma = Double.parseDouble(getPropertyOrDie(props, "gamma"));
            double kappa = Double.parseDouble(getPropertyOrDie(props, "kappa"));
            double bandwidth = Double.parseDouble(getPropertyOrDie(props, "bandwidth"));
            try{
                solver = new KernelSolverCplex(model, kappa, gamma, bandwidth, sampleCount, seed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("unknown solver type " + solverType);
        }
        try {
            solver.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }
        policy =  solver.getPolicy();
        return true;
    }

    public static boolean evalCommand(Properties props) {
        if (model == null || policy == null) {
            throw new RuntimeException("model and policy must be initialized");
        }
        int sampleCount = Integer.parseInt(getPropertyOrDie(props, "sample_count"));
        long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
        System.out.println("sampling " + sampleCount + " sample paths");
        MonteCarloEval sampler = new MonteCarloEval(model, policy, seed);
        MonteCarloEval.MonteCarloResults mcEval = sampler.eval(sampleCount, model.getTimePeriods());
        System.out.println(mcEval.toString());
        return true;
    }
}
