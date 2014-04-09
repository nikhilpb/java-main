package com.nikhilpb.doe;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by nikhilpb on 3/21/14.
 */
public class DOEMain {
    public static void main(String[] argv) throws Exception {
        int p = 10;
        int sampleCount = 10000;
        double upper = 3000.;
        int nop = 10001;
        long seed = 123l;
        int timePeriods = 10;

        OneDFunction[] qFuns = new OneDFunction[timePeriods];
        qFuns[0] = new IdentityFunction();

        for (int i = 1; i < timePeriods; ++i) {

            qFuns[i] = QFunctionRecursion.recurse(qFuns[i-1], p, upper, nop, seed, sampleCount);
            qFuns[i].printFn(new PrintStream(new FileOutputStream("/tmp/q" + i + ".csv")), 0., 10.);

        }
    }
}
