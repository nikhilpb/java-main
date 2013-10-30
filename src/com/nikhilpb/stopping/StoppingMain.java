package com.nikhilpb.stopping;

import Jama.Matrix;
import com.nikhilpb.adp.RewardFunction;
import com.nikhilpb.adp.SamplePath;
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
    private static ArrayList<SamplePath> samplePath;

    public static void main(String[] args) {
        HashMap<String, CommandProcessor> cmdMap = new HashMap<String, CommandProcessor>();
        CommandProcessor modelProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return modelCommand(props);
            }
        };
        cmdMap.put("model", modelProcessor);

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
        } else {
            throw new RuntimeException("unkown reward function");
        }
        long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
        model = new StoppingModel(mu, covar, initValue, timPeriods, rewardFunction, seed);
        return true;
    }
}
