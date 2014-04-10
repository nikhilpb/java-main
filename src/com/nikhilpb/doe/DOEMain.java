package com.nikhilpb.doe;

import com.nikhilpb.util.XmlParser;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by nikhilpb on 3/21/14.
 */
public class DOEMain extends XmlParser {
    private static int dim;
    private static int sampleCount;
    private static double upper;
    private static double printUpper;
    private static int pointsCount;
    private static long seed;
    private static int timePeriods;
    private static String baseName;
    private static OneDFunction[] qFuns;


    public static void main(String[] args) throws Exception {
        HashMap<String, CommandProcessor> cmdMap = new HashMap<String, CommandProcessor>();

        CommandProcessor modelProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return modelCommand(props);
            }
        };
        cmdMap.put("model", modelProcessor);

        CommandProcessor solveProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return solveCommand(props);
            }
        };
        cmdMap.put("solve", solveProcessor);

        CommandProcessor printProcessor = new CommandProcessor() {
            @Override
            public boolean processCommand(Properties props) throws Exception {
                return printCommand(props);
            }
        };
        cmdMap.put("print", printProcessor);

        parseCommandLine(args, null);
        executeCommands(cmdMap);


    }

    public static boolean modelCommand(Properties props) {
        dim = Integer.parseInt(getPropertyOrDie(props, "dim"));
        timePeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
        return true;
    }

    public static boolean solveCommand(Properties props) {
        sampleCount = Integer.parseInt(getPropertyOrDie(props, "sample_count"));
        upper = Double.parseDouble(getPropertyOrDie(props, "upper"));
        pointsCount = Integer.parseInt(getPropertyOrDie(props, "points_count"));
        seed = Long.parseLong(getPropertyOrDie(props, "seed"));

        qFuns = new OneDFunction[timePeriods];
        qFuns[0] = new IdentityFunction();

        for (int i = 1; i < timePeriods; ++i) {
            System.out.println("Time Period no: " + i);
            qFuns[i] = QFunctionRecursion.recurse(qFuns[i-1], dim, upper, pointsCount, seed, sampleCount);
        }
        return true;
    }

    public static boolean printCommand(Properties props) {
        printUpper = Double.parseDouble(getPropertyOrDie(props, "print_upper"));
        baseName = getPropertyOrDie(props, "base_name");
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

}
