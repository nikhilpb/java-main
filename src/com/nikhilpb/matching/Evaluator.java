package com.nikhilpb.matching;

import java.io.PrintStream;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/1/13
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator {
    private MatchingSolver solver;
    private PrintStream out;
    private int sampleCount;
    Random random;
    public Evaluator(MatchingSolver solver, PrintStream out, int sampleCount, Random random) {
        this.solver = solver;
        this.out = out;
        this.sampleCount = sampleCount;
        this.random = random;
    }

    public double evaluate() throws Exception {
        double value = 0.0;
        double valueStd = 0.0;
        long sampleSeed;
        for (int ss = 0; ss < sampleCount; ++ss) {
            sampleSeed = random.nextLong();
            double thisValue = solver.evaluate(sampleSeed);
            value += thisValue;
            valueStd += thisValue * thisValue;
        }
        value = value / ((double)sampleCount);
        valueStd = valueStd / ((double)sampleCount);
        valueStd = valueStd - value * value;
        out.printf("\nvalue: %f, std dev:%f\n\n", value, valueStd);
        return value;
    }
}

