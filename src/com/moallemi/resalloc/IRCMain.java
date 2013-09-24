package com.moallemi.resalloc;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;
import com.moallemi.minsum.*;

public class IRCMain extends CommandLineMain {
    private InelasticRateControlProblem[] problemList;
    private boolean printOpt = false;
    private boolean noTimes = false;

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

            String graphType = cmd.next();
            Random baseRandom1 = getChildRandom(baseRandom0);
            if (graphType.equals("regular")) {
                int userCount = cmd.nextInt();
                double alpha = cmd.nextDouble();
                int lDegree = cmd.nextInt();
                int linkCount = 
                    (int) Math.round(((double)userCount * alpha));
                
                problemList = new InelasticRateControlProblem [problemCount];
                for (int i = 0; i < problemCount; i++) {
                    Random r = getChildRandom(baseRandom1);
                    problemList[i] = 
                        new InelasticRateControlProblem(userCount,
                                                        linkCount);
                    FactorGraphFactory
                        .buildRandomRegularGraphByFactor(r,
                                                         problemList[i],
                                                         lDegree);
                }
            }
            else if (graphType.equals("single")) {
                int userCount = cmd.nextInt();
                problemList = new InelasticRateControlProblem [problemCount];
                for (int i = 0; i < problemCount; i++) {
                    problemList[i] = 
                        new InelasticRateControlProblem(userCount,
                                                        1);
                    FactorGraphFactory
                        .buildCompleteGraph(problemList[i]);
                }
            }
            else
                throw new Exception("unknown graph type: " 
                                    + graphType);

            String utilType = cmd.next();
            Random baseRandom2 = getChildRandom(baseRandom0);
            if (utilType.equals("exp")) {
                double utilMean = cmd.nextDouble();
                for (int i = 0; i < problemCount; i++) {
                    InelasticRateControlProblem p = problemList[i];
                    int userCount = p.getVariableCount();
                    Random r = getChildRandom(baseRandom2);
                    for (int u = 0; u < userCount; u++)
                        p.setUserUtility(u, 
                                         Distributions
                                         .nextExponential(r,
                                                          1.0/utilMean));
                }
            }
            else 
                throw new Exception("unknown utility type: " 
                                    + utilType);
                
            String bwType = cmd.next();
            Random baseRandom3 = getChildRandom(baseRandom0);
            if (bwType.equals("fixed")) {
                double minBW = cmd.nextDouble();
                for (int i = 0; i < problemCount; i++) {
                    InelasticRateControlProblem p = problemList[i];
                    int userCount = p.getVariableCount();
                    for (int u = 0; u < userCount; u++)
                        p.setUserMinBandwidth(u, minBW);
                }
            }
            else if (bwType.equals("exp")) {
                double bwMean = cmd.nextDouble();
                for (int i = 0; i < problemCount; i++) {
                    InelasticRateControlProblem p = problemList[i];
                    int userCount = p.getVariableCount();
                    Random r = getChildRandom(baseRandom3);
                    for (int u = 0; u < userCount; u++)
                        p.setUserMinBandwidth(u, 
                                              Distributions
                                              .nextExponential(r,
                                                               1.0/bwMean));
                }
            }
            else if (bwType.equals("bwutil")) {
                for (int i = 0; i < problemCount; i++) {
                    InelasticRateControlProblem p = problemList[i];
                    int userCount = p.getVariableCount();
                    Random r = getChildRandom(baseRandom3);
                    for (int u = 0; u < userCount; u++)
                        p.setUserMinBandwidth(u, 
                                              p.getUserUtility(u));
                }
            }
            else
                throw new Exception("unknown minimum bandwidth type: " 
                                    + bwType);
            
            String capacityType = cmd.next();
            if (capacityType.equals("fixed")) {
                double capacity = cmd.nextDouble();
                for (int i = 0; i < problemCount; i++) {
                    InelasticRateControlProblem p = problemList[i];
                    int linkCount = p.getFactorCount();
                    for (int l = 0; l < linkCount; l++)
                        p.setLinkCapacity(l, capacity);
                }
            }
            else
                throw new Exception("unknown capacity type: " 
                                    + capacityType);
        }

        else if (base.equals("cplexsolve")) {
            String type = cmd.next();
            TicTocTimer t = new TicTocTimer();
            Random baseRandom = getRandom();
            for (int i = 0; i < problemList.length; i++) {
                InelasticRateControlSolver solver;
                if (type.equals("ip"))
                    solver = new IRCIPSolver(problemList[i],
                                             getCplexFactory());
                else if (type.equals("lpgreedy")) 
                    solver = new IRCLPGreedySolver(problemList[i],
                                                   getCplexFactory());
                else if (type.equals("lpiter")) {
                    int totalIterations = cmd.nextInt();
                    double epsilon = cmd.nextDouble();
                    solver = new IRCLPIteratedSolver(problemList[i],
                                                     totalIterations,
                                                     epsilon,
                                                     getCplexFactory());
                }
                else if (type.equals("greedy")) 
                    solver = new IRCGreedySolver(problemList[i],
                                                 getChildRandom(baseRandom));
                else
                    throw new Exception("unknown solver: " + type);

                t.tic();
                boolean status = solver.solve();
                double time = t.toc();
                InelasticRateControlSolution solution =
                    solver.getSolution();
                System.out.print(type + " problem: " + (i+1));
                System.out.print(" obj: " 
                                 + df.format(solution.getObjectiveValue()));
                if (!noTimes)
                    System.out.print(" t2: " + df.format(time) + " (s)");
                System.out.print(solution.isFeasible() ? " feas" : " nfeas");
                System.out.print((solver.isOptimal() ? " opt" : " nopt"));
                System.out.println();
                
                if (printOpt)
                    System.out.println(">>>> " + 
                                       solution.toString());
                
            }
        }

        else if (base.equals("solve")) {
            double damp = cmd.nextDouble();
            int maxIterCount = cmd.nextInt();
            double tolerance = cmd.nextDouble();

            double[] obj = new double [maxIterCount];
            double[] cumTime = new double [maxIterCount];
            TicTocTimer t = new TicTocTimer();
            for (int i = 0; i < problemList.length; i++) {
                IRCMinSumSolver solver = new IRCMinSumSolver(problemList[i],
                                                             damp);

                int iter;
                double max = -1.0;
                String maxStr = "";
                double lastTime = 0.0;
                for (iter = 0; iter < maxIterCount; iter++) {
                    t.tic();
                    solver.iterate();            
                    lastTime += t.toc();
                    cumTime[iter] = lastTime;

                    solver.computeSolution();
                    InelasticRateControlSolution solution 
                        = solver.getSolution();
                    obj[iter] = solution.getObjectiveValue();
                    double error = solver.getBellmanError();

                    if (obj[iter] > max) {
                        max = obj[iter];
                        maxStr = solution.toString();
                    }

                    if (isDebug()) {
                        System.out.print((i+1) + "-" + (iter+1) + ": "
                                         + df.format(obj[iter])
                                         + " "
                                         + df2.format(error));
                        System.out.println();
                        
                        if (printOpt)
                            System.out.println(">>>> " + 
                                               solution.toString());
                    }

                    if (error < tolerance) 
                        break;
                }
                int max2 = iter >= maxIterCount ? iter - 1 : iter;
                int last;
                for (last = 0; last <= max2 && obj[last] < max; last++)
                    ;

                System.out.print("MP problem: " + (i+1));
                System.out.print(" obj: " + df.format(max));
                if (!noTimes)
                    System.out.print(" t2: " + df.format(cumTime[max2]) 
                                     + " (s)");
                System.out.print(" i2: " + (max2+1));
                if (!noTimes)
                    System.out.print(" t1: " + df.format(cumTime[last]) 
                                     + " (s)");
                System.out.print(" i1: " + (last+1));
                System.out.print(" err: " 
                                 + df2.format(solver.getBellmanError()));
                System.out.println();                

                if (printOpt)
                    System.out.println(">>>> " + maxStr);
            }
        }

        else 
            return false;

        return true;
    }

    public static void main(String[] argv) throws Exception {
        (new IRCMain()).run(argv);
    }

}