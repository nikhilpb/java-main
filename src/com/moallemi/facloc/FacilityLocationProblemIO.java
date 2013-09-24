package com.moallemi.facloc;

import java.io.*;
import java.util.regex.*;
import java.text.DecimalFormat;

public class FacilityLocationProblemIO {

    private static final double BIG = 999999.0;

    public static void writeModel(FacilityLocationProblem problem, 
                                  PrintWriter out) {
        DecimalFormat df = new DecimalFormat("0.000000");
        int cityCount = problem.getCityCount();
        int facilityCount = problem.getFacilityCount();
        out.println(facilityCount + " " + cityCount + " 0");
        for (int j = 0; j < facilityCount; j++) {
            out.print((j+1) + " " + df.format(problem.getConstructionCost(j)));
            for (int i = 0; i < cityCount; i++) {
                double d = problem.getDistance(i, j);
                if (d >= Double.MAX_VALUE)
                    d = BIG;
                out.print(" " + df.format(d));
            }
            out.println();
        }
    }

    private static final Pattern p1 
        = Pattern.compile("^(\\d+)\\s+(\\d+)\\s+0\\s*$");
    private static final Pattern p2
        = Pattern.compile("^(\\d+)\\s+(\\S+)\\s+(.*)$");

    public static FacilityLocationProblem readModel(BufferedReader in) 
        throws IOException {
        String line;
        Matcher m;

        line = in.readLine();
        m = p1.matcher(line);
        if (!m.matches())
            throw new IllegalArgumentException("unable to parse: " + line);
        int facilityCount = Integer.parseInt(m.group(1));
        int cityCount = Integer.parseInt(m.group(2));
        double[][] distance = new double [cityCount][facilityCount];
        double[] cost = new double [facilityCount];

        while ((line = in.readLine()) != null) {
            m = p2.matcher(line);
            if (!m.matches())
                throw new IllegalArgumentException("unable to parse: " + line);
            int j = Integer.parseInt(m.group(1)) - 1;
            cost[j] = Double.parseDouble(m.group(2));
            String[] split = m.group(3).split("\\s");
            if (split.length != cityCount)
                throw new IllegalArgumentException("unable to parse: " + line);
            for (int i = 0; i < cityCount; i++) {
                double d = Double.parseDouble(split[i]);
                if (d >= BIG)
                    d = Double.MAX_VALUE;
                distance[i][j] = d;
            }
        }

        return new GeneralFacilityLocationProblem(cityCount,
                                                  facilityCount,
                                                  0,
                                                  distance,
                                                  cost);
    }
        

}
