package com.moallemi.maxcut;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.Distributions;
import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;

public class MaxCutMain {
    private String[] argv;
    private int index;
    private MaxCutProblem[] problemList;
    private long seed = -1L;
    private boolean debug = false;

    public MaxCutMain(String[] argv) { this.argv = argv; index = 0; }

    public boolean hasNext() {
        return index < argv.length;
    }

    public Random getRandom() {
	return new Random(seed >= 0L ? seed : System.currentTimeMillis());
    }

    private DecimalFormat df = new DecimalFormat("0.00");
    private DecimalFormat df2 = new DecimalFormat("0.00E0");
    public String format(double x) {
        return  x >= Double.MAX_VALUE ? "+inf" : df.format(x);
    }

    public void processNext() throws Exception {
        int lastIndex = index;

	if (argv[index].equals("debug"))
	    debug = true;
        else if (argv[index].equals("seed")) {
	    seed = Long.parseLong(argv[++index]);
        }
        else if (argv[index].equals("loadmodels")) {
            int cnt = Integer.parseInt(argv[++index]);
            problemList = new MaxCutProblem [cnt];
            for (int i = 0; i < cnt; i++) {
                String fname = argv[++index];
                BufferedReader in = new BufferedReader(new FileReader(fname));
                problemList[i] = MaxCutProblemIO.readModel(in);
                in.close();
            }
        }
        else if (argv[index].equals("dumpmodel")) {
            String prefix = argv[++index];
            for (int i = 0; i < problemList.length; i++) {
                PrintWriter out;
                if (prefix.equals("-")) 
                    out = new PrintWriter(System.out, true);
                else 
                    out = 
                        new PrintWriter(new 
                                        BufferedWriter(new 
                                                       FileWriter(prefix + "-" + (i+1) + ".txt")));
                MaxCutProblemIO.writeModel(problemList[i], out);
                if (prefix.equals("-"))
                    out.flush();
                else
                    out.close();

            }
        }
        else if (argv[index].equals("problem")) {
            int problemCount = Integer.parseInt(argv[++index]);
            int nodeCount = Integer.parseInt(argv[++index]);
            double p = Double.parseDouble(argv[++index]);
            double costMean = Double.parseDouble(argv[++index]);

            problemList = new MaxCutProblem [problemCount];
            Random baseRandom = getRandom();
            for (int i = 0; i < problemCount; i++) { 
                Random r = new Random(baseRandom.nextLong());
                Graph graph = GraphFactory.buildBernoulli(nodeCount,
                                                          p,
                                                          r,
                                                          true);

                Random r2 = new Random(baseRandom.nextLong());
                int edgeCount = graph.getEdgeCount();
                double[] costByEdge = new double [edgeCount];
                for (int e = 0; e < edgeCount; e++)
                    costByEdge[e] = 
                        Distributions.nextExponential(r2, 1.0/costMean);
                problemList[i] = new MaxCutProblem(graph, costByEdge);
            }            
        }
        else if (argv[index].equals("modelstats")) {
            for (int i = 0; i < problemList.length; i++) {
                Graph graph = problemList[i].getGraph();
                AllPairsShortestPaths apsp = new AllPairsShortestPaths(graph);
                System.out.println("problem: " + (i+1)
                                   + " deg: " + format(graph.getAverageDegree())
                                   + " conn: " + apsp.isConnected()
                                   + " diam: " + apsp.getMaximumPathLength());
                if (debug)
                    problemList[i].getGraph().dumpInfo(System.out);
            }
        }
        else if (argv[index].equals("solve")) {
            double damp = Double.parseDouble(argv[++index]);
            int maxIterCount = Integer.parseInt(argv[++index]);
            double tolerance = Double.parseDouble(argv[++index]);
            Random baseRandom = getRandom();

            TicTocTimer t = new TicTocTimer();
            for (int i = 0; i < problemList.length; i++) {
                Random r = new Random(baseRandom.nextLong());
                MCMinSumSolver solver = new MCMinSumSolver(problemList[i],
                                                           damp,
                                                           r);
                t.tic();
                int iter;
                double max = Double.NEGATIVE_INFINITY;
                double[] time = new double [maxIterCount];
                double[] obj = new double [maxIterCount];
                for (iter = 0; iter < maxIterCount; iter++) {
                    solver.iterate();                    
                    time[iter] = t.toc();
                    obj[iter] = solver.getObjectiveValue();
                    if (obj[iter] > max)
                        max = obj[iter];
                    
                    if (debug) {
                        System.out.println((i+1) + "-" + (iter+1) + ": "
                                           + format(solver.getObjectiveValue())
                                           + " " 
                                           + df2
                                           .format(solver.getBellmanError())
                                           + " "
                                           + solver.getOptimalStats());
                    }

                    if (solver.getBellmanError() < tolerance)
                        break;
                }
                int end = iter >= maxIterCount ? iter - 1 : iter;
                int last;
                for (last = 0; last <= end && obj[last] < max; last++)
                    ;

                System.out.print("MINSUM problem: " + (i+1));
                System.out.print(" obj: " + format(max));
                System.out.print(" t2: " + format(time[end]) + " (s)");
                System.out.print(" i2: " + (end+1));
                System.out.print(" t1: " + format(time[last]) + " (s)");
                System.out.print(" i1: " + (last+1));
                System.out.print(" err: " + df2.format(solver.getBellmanError()));
                System.out.println();                
            }
        }
        else 
            throw new Exception("unknown command: " + argv[index]);
       

        ++index;
        System.out.print(">>>");
        for (int i = lastIndex; i < index; i++)
            System.out.print(" " + argv[i]);
        System.out.println();
    }

    public static void main(String[] argv) throws Exception {
        MaxCutMain m = new MaxCutMain(argv);

        TicTocTimer t = new TicTocTimer();
        while (m.hasNext()) {
            t.tic();
            m.processNext();
            t.toc();
            t.tocPrint();
        }
        
        System.exit(0);
    }
            
}
