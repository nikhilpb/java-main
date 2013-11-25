package com.moallemi.qpminsum;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.*;
import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;
import com.moallemi.minsum.*;

public class QPMain extends CommandLineMain {
    private QuadraticPairwiseProblem[] problemList;
    private double[][] solutions;
    private boolean printOpt = false;
    private boolean noTimes = false;
    
    private String solverType;
    private QPIterativeSolver solver;

    private DecimalFormat df = new DecimalFormat("0.00");
    private DecimalFormat df2 = new DecimalFormat("0.000000");

    protected boolean processCommand(CommandLineIterator cmd) 
        throws Exception
    {
        String base = cmd.next();

        if (base.equals("printopt"))
            printOpt = true;

        else if (base.equals("notimes"))
            noTimes = true;

        else if (base.equals("problem")) {
            int problemCount = cmd.nextInt();
            Random baseRandom0 = getRandom();

            // construct the graphs
            Graph[] graphList = new Graph [problemCount];
            String graphType = cmd.next();
            Random baseRandom1 = getChildRandom(baseRandom0);
            if (graphType.equals("bernoulli")
                || graphType.equals("bernoullif")) {
                int nodeCount = cmd.nextInt();
                double avgDegree = cmd.nextDouble();

                // probability to force the average degree
                double p = avgDegree / ((double) (nodeCount - 1));
                if (p < 0.0 || p > 1.0)
                    throw new IllegalArgumentException("bad average degree");

                for (int l = 0; l < problemCount; l++) {
                    Random r = getChildRandom(baseRandom1);
                    graphList[l] = graphType.equals("bernoulli")
                        ? GraphFactory.buildBernoulli(nodeCount,
                                                      p,
                                                      r,
                                                      false)
                        : GraphFactory.buildBernoulliFast(nodeCount,
                                                          p,
                                                          r,
                                                          false);
                    
                }
            }
	    else if (graphType.equals("lattice")) {
		int nodesPerAxis = cmd.nextInt();
		int dimension = cmd.nextInt();
		int connectivity = cmd.nextInt();
		boolean isTorus = cmd.nextBoolean();
		boolean isL1Norm = cmd.nextBoolean();
                for (int l = 0; l < problemCount; l++)
                    graphList[l] = GraphFactory.buildLattice(nodesPerAxis, 
                                                             dimension,
                                                             connectivity,
                                                             isTorus,
                                                             isL1Norm);
            }
	    else if (graphType.equals("lollipop")) {
		int nodeCount = cmd.nextInt();
                for (int l = 0; l < problemCount; l++)
                    graphList[l] = GraphFactory.buildLollipop(nodeCount);
	    }
	    else if (graphType.equals("squarediagonal")) {
                for (int l = 0; l < problemCount; l++)
                    graphList[l] = GraphFactory.buildSquareWithDiagonal();
	    }
	    else if (graphType.equals("complete")) {
		int nodeCount = cmd.nextInt();
                for (int l = 0; l < problemCount; l++)
                    graphList[l] = GraphFactory.buildComplete(nodeCount);
	    }
            else 
                throw new IllegalArgumentException("unknown graph type: "
                                                   + graphType);

            problemList = new QuadraticPairwiseProblem [problemCount];
            for (int l = 0; l < problemCount; l++)
                problemList[l] = new QuadraticPairwiseProblem(graphList[l]);

            String objType = cmd.next();
            Random baseRandom2 = getChildRandom(baseRandom0);
            if (objType.equals("normalexp")) {
                double singleStdDev = cmd.nextDouble();
                double expMean = cmd.nextDouble();
                
                for (int l = 0; l < problemCount; l++) {
                    QuadraticPairwiseProblem p = problemList[l];
                    int nodeCount = p.getNodeCount();
                    Random r1 = getChildRandom(baseRandom2);
                    Random r2 = getChildRandom(baseRandom2);
                    for (int i = 0; i < nodeCount; i++) {
                        p.setSingleK(i, 1.0);
                        p.setSingleH(i, singleStdDev * r1.nextGaussian());
                        int degree = p.getNodeDegree(i);
                        for (int idx = 0; idx < degree; idx++) {
                            int j = p.getNodeNeighbor(i, idx);
                            if (i < j) {
                                int offset = 
                                    p.getNodeNeighborOffset(i, idx);
                                double z = 
                                    Distributions
                                    .nextExponential(r2, 1.0 / expMean);
                                p.setPairwiseK(i, idx, z);
                                p.setPairwiseGamma(i, idx, -z);
                                p.setPairwiseK(j, offset, z);
                                p.setPairwiseH(i, idx, 0.0);
                                p.setPairwiseH(j, offset, 0.0);
                            }
                        }
                    }
                }
            }
            else if (objType.equals("normalfixed")) {
                double singleStdDev = cmd.nextDouble();
                double beta = cmd.nextDouble();
                
                for (int l = 0; l < problemCount; l++) {
                    QuadraticPairwiseProblem p = problemList[l];
                    int nodeCount = p.getNodeCount();
                    Random r1 = getChildRandom(baseRandom2);
                    Random r2 = getChildRandom(baseRandom2);
                    for (int i = 0; i < nodeCount; i++) {
                        p.setSingleK(i, 1.0);
                        p.setSingleH(i, singleStdDev * r1.nextGaussian());
                        int degree = p.getNodeDegree(i);
                        for (int idx = 0; idx < degree; idx++) {
                            int j = p.getNodeNeighbor(i, idx);
                            if (i < j) {
                                int offset = 
                                    p.getNodeNeighborOffset(i, idx);
                                p.setPairwiseK(i, idx, beta);
                                p.setPairwiseGamma(i, idx, -beta);
                                p.setPairwiseK(j, offset, beta);
                                p.setPairwiseH(i, idx, 0.0);
                                p.setPairwiseH(j, offset, 0.0);
                            }
                        }
                    }
                }
            }
            else 
                throw new Exception("unknown objective type: " 
                                    + objType);

            // solve the problems
            solutions = new double [problemCount][];
            QPOptSolver optSolver = new QPOptSolver();
            for (int l = 0; l < problemCount; l++) {
                optSolver.setProblem(problemList[l]);
                optSolver.solve();
                solutions[l] = optSolver.getSolution();
            }
                
        }

        else if (base.equals("optsolve")) {
            TicTocTimer t = new TicTocTimer();
            QPOptSolver optSolver = new QPOptSolver();
            for (int l = 0; l < problemList.length; l++) {
                QuadraticPairwiseProblem problem = problemList[l];
                optSolver.setProblem(problem);
                t.tic();
                boolean status = optSolver.solve();
                double time = t.toc();
                double[] x = optSolver.getSolution();
                System.out.print("optsolve problem: " + (l+1));
                System.out.print(" obj: " 
                                 + df2.format(problem.evaluate(x)));
                if (!noTimes)
                    System.out.print(" t2: " + df.format(time) + " (s)");
                System.out.println();
                
                if (printOpt)
                    System.out.println(">>>> " + 
                                       x.toString());
                
            }
        }

        else if (base.equals("solver")) {
            solverType = cmd.next();
            
            if (solverType.equals("minsum")) {
                double damp = cmd.nextDouble();
                solver = new QPMinSumSolver(damp);
            }
            else if (solverType.equals("coord")) {
                double damp = cmd.nextDouble();
                solver = new QPCoordinateDescentSolver(damp);
            }
            else 
                throw new IllegalArgumentException("unknown solver: " 
                                                   + solverType);
        }

        else if (base.equals("run") || base.equals("runbatch")) {
            boolean batchMode = base.equals("runbatch");
            double tolerance = cmd.nextDouble();
            int maxIterCount = cmd.nextInt();
            int printInterval = -1;
            if (!batchMode) 
                printInterval = cmd.nextInt();


            SampleStatistics iterStats 
                = new SampleStatistics(problemList.length);
            SampleStatistics timeStats
                = new SampleStatistics(problemList.length);
            int failureCount = 0;
            TicTocTimer t = new TicTocTimer();
            for (int l = 0; l < problemList.length; l++) {
                QuadraticPairwiseProblem problem = problemList[l];
                solver.setProblem(problem);

                int iter;
                double min = Double.POSITIVE_INFINITY;
                double gNorm = -1.0;
                double lastTime = 0.0;
                double bestTime = 0.0;
                int bestIter = -1;
                boolean success = false;
                for (iter = 0; iter < maxIterCount && !success; iter++) {
                    t.tic();
                    solver.iterate();            
                    lastTime += t.toc();

                    double[] x = solver.getSolution();
                    double obj = problem.evaluate(x);
                    gNorm = problem.evaluateGradientNorm(x);

                    if (obj < min) {
                        min = obj;
                        bestTime = lastTime;
                        bestIter = iter;
                    }

                    if (printInterval > 0 && iter % printInterval == 0) {
                        System.out
                            .print((l+1) + "-" + (iter+1) + ": "
                                   + df2.format(obj)
                                   + " "
                                   + df2.format(gNorm));

                        if (solver instanceof QPMinSumSolver)
                            System.out
                                .print(" "
                                       + df2.format(((QPMinSumSolver) solver)
                                                    .getBellmanErrorK())
                                       + " "
                                       + df2.format(((QPMinSumSolver) solver)
                                                    .getBellmanErrorH())
                                       );
                        else
                            System.out.print(" NA NA ");

                        System.out.println();
                        if(printOpt)
                            System.out.println(">>>> " + 
                                               x.toString());
                    }
                    
                    if (computeRMS(x, solutions[l]) < tolerance)
                        success = true;
                }

                if (success) {
                    iterStats.addSample(iter+1);
                    timeStats.addSample(lastTime);
                }
                else
                    failureCount++;

                if (batchMode)
                    continue;

                System.out.print(solverType + " problem: " + (l+1));
                System.out.print(" obj: " + df2.format(min));
                if (!noTimes)
                    System.out.print(" t2: " + df.format(lastTime) 
                                     + " (s)");
                System.out.print(" i2: " + (iter+1));
                if (!noTimes)
                    System.out.print(" t1: " + df.format(bestTime) 
                                     + " (s)");
                System.out.print(" i1: " + (bestIter+1));
                System.out.print(" err: " + df2.format(gNorm));
                if (solver instanceof QPMinSumSolver)
                    System.out
                        .print(" "
                               + df2.format(((QPMinSumSolver) solver)
                                            .getBellmanErrorK())
                               + " "
                               + df2.format(((QPMinSumSolver) solver)
                                            .getBellmanErrorH())
                               );
                else
                    System.out.print(" NA NA ");

                System.out.print(success ? " *" : " X");
                             
                System.out.println();                
            }
            
            System.out.print(solverType + " SUMMARY");
            System.out.print(" t2: " 
                             + df.format(timeStats.getMean())
                             + " +- " 
                             + df.format(timeStats.getStandardDeviation()));
            System.out.print(" i2: " 
                             + df.format(iterStats.getMean())
                             + " +- " 
                             + df.format(iterStats.getStandardDeviation()));
            if (failureCount > 0)
                System.out.print("FAILURES " + failureCount);
            System.out.println();
            
        }

        else 
            return false;

        return true;
    }

    private static double computeRMS(double[] x, double[] y) {
        int n = x.length;
        if (y.length != n)
            throw new IllegalArgumentException("getDimension mismatch");
        double v = 0.0;
        for (int i = 0; i < n; i++) {
            double d = x[i] - y[i];
            v += d*d;
        }
        return Math.sqrt(v / ((double) n));
    }
    
    public static void main(String[] argv) throws Exception {
        (new QPMain()).run(argv);
    }

}