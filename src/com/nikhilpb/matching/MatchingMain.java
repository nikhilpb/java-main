package com.nikhilpb.matching;

import com.moallemi.util.PropertySet;
import com.nikhilpb.util.XmlParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/24/13
 * Time: 10:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class MatchingMain extends XmlParser {
    private static MatchingModel model;
    private static ItemFunctionSet basisSetSupply, basisSetDemand;
    private static ArrayList<MatchingSolver> solvers;

    public static void main(String[] args) {
        HashMap<String, CommandProcessor> cmdMap = new HashMap<String, CommandProcessor>();

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

        CommandProcessor evaluateProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return evaluateCommand(props);
            }
        };
        cmdMap.put("evaluate", evaluateProcessor);

        parseCommandLine(args, handler);
        executeCommands(cmdMap);
    }

    private static boolean modelCommand(Properties props) throws Exception {
        String fileName = getPropertyOrDie(props, "file");
        PropertySet modelParams = new PropertySet(new File(fileName));

        String modelType = getPropertyOrDie(props, "type");

        System.out.println("reading model from file " + fileName + ", with type " + modelType);
        if (modelType.equals("general")) {
            model = new MatchingModel(modelParams);
        } else {
            throw new RuntimeException("improper model type");
        }
        return true;
    }

    private static boolean basisCommand(Properties props) throws Exception {
        if (model == null) {
            throw new RuntimeException("model must be initialized");
        }
        String type = getPropertyOrDie(props, "type");
        if (type.equals("separable")) {
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
        } else {
            throw new RuntimeException("unknown basis type");

        }
        return true;
    }

    private static boolean solveCommand(Properties props) throws Exception {
        if (model == null) {
            throw new RuntimeException("model must be initialized");
        }
        final MatchingSolver.SolverType solverType =
                MatchingSolver.solverTypeFromString(getPropertyOrDie(props, "method"));
        final int problemCount = Integer.parseInt(getPropertyOrDie(props, "problem_count"));
        if (problemCount < 0) {
            System.err.print("problem count has to be non-negative");
        }

        final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
        MatchingSolver.SamplingPolicy samplingPolicy =
                MatchingSolver.samplingPolicyFromString(getPropertyOrDie(props, "sampling_policy"));

        Random random = new Random(seed);
        solvers = new ArrayList<MatchingSolver>();
        boolean success = true;
        for (int p = 0; p < problemCount; ++p) {
            System.out.println("solving problem no: " + p);
            switch (solverType) {
                case SALP_SSGD:
                    if (basisSetSupply == null || basisSetDemand == null) {
                        throw new RuntimeException("basis must be initialized");
                    }
                    final double eps = Double.parseDouble(getPropertyOrDie(props, "eps"));
                    final double a = Double.parseDouble(getPropertyOrDie(props, "a"));
                    final double b = Double.parseDouble(getPropertyOrDie(props, "b"));
                    final int stepCount = Integer.parseInt(getPropertyOrDie(props, "step_count"));
                    final int checkPerSteps = Integer.parseInt(getPropertyOrDie(props, "check_per_steps"));
                    final int simSteps = Integer.parseInt(getPropertyOrDie(props, "sim_steps"));
                    final long simSeed = Long.parseLong(getPropertyOrDie(props, "sim_seed"));
                    SsgdSolver.Config config = new SsgdSolver.Config();
                    config.aConfig = a;
                    config.bConfig = b;
                    config.epsConfig = eps;
                    config.stepCountConfig = stepCount;
                    config.checkPerStepsConfig = checkPerSteps;
                    config.simSteps = simSteps;
                    config.simSeed = simSeed;
                    solvers.add(new SsgdSolver(model, basisSetSupply, basisSetDemand,
                                               random.nextLong(), samplingPolicy, config));
                    success = success && solvers.get(p).solve();
                    break;
                case GREEDY:
                    solvers.add(new GreedySolver(model, random.nextLong(), samplingPolicy));
                    break;
                case SALP_BATCHLP:
                    break;
            }
        }
        return success;
    }

    private static boolean evaluateCommand(Properties props) throws Exception {
        if (model == null) {
            throw new RuntimeException("model must be non-null");
        }
        final String evalType = props.getProperty("type");
        final int sampleCount = Integer.parseInt(getPropertyOrDie(props, "sample_count"));
        final long seed = Long.parseLong(getPropertyOrDie(props, "sample_seed"));
        Random random = new Random(seed);
        long sampleSeed;
        double value, valueStd;
        MatchingSamplePath samplePath;
        if (evalType != null && evalType.equals("offline")) {
            value = 0.0;
            valueStd = 0.0;
            for (int ss = 0; ss < sampleCount; ++ss) {
                sampleSeed = random.nextLong();
                samplePath = new MatchingSamplePath(model, sampleSeed);
                samplePath.sample();
                double thisValue = samplePath.offlineMatch();
                value += thisValue;
                valueStd += thisValue * thisValue;
            }
            value = value / ((double)sampleCount);
            valueStd = valueStd / ((double)sampleCount);
            valueStd = valueStd - value * value;
            System.out.printf("\nvalue: %f, std dev:%f\n\n", value, valueStd);

        } else {
            for (int s = 0; s < solvers.size(); s++) {
                System.out.println("evaluating solver no: " + s);
                Evaluator evaluator = new Evaluator(solvers.get(0), System.out, sampleCount, seed);
                evaluator.evaluate("solver no: " + s);
            }
        }
        return true;
    }


}
