package com.nikhilpb.doe;

import com.nikhilpb.util.Experiment;
import com.nikhilpb.util.XmlParser;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

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
}
