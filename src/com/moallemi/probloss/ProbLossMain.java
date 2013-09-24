package com.moallemi.probloss;

import java.util.*;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.regression.SimpleRegression;

import com.moallemi.util.*;
import com.moallemi.math.stats.*;

/**
 * Command line interface for loss probability algorithms.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2009-02-17 15:57:55 $
 */
public class ProbLossMain extends CommandLineMain {
    private ProbLossModel model;
    private ProbLossAlgorithm algorithm;
    
    protected boolean processCommand(CommandLineIterator cmd) 
        throws Exception
    {
        String base = cmd.next();

        if (base.equals("model")) {
            double sigma1 = cmd.nextDouble();
            double sigma2 = cmd.nextDouble();
            double threshold = cmd.nextDouble();

            model = new ProbLossModel(sigma1, sigma2, threshold);
            System.out.printf("loss_prob: %.20f\n", model.getProbLossTrue());
        }

        else if (base.equals("algorithm")) {
            String algType = cmd.next();
            if (algType.equals("cexp")) {
                double constant = cmd.nextDouble();
                double exponent = cmd.nextDouble();
                boolean stage2 = cmd.hasNext() ? cmd.nextBoolean() : false;
                algorithm = new ConstantExponentAlgorithm(constant,
                                                          exponent,
                                                          stage2);
            }
            else if (algType.equals("opt")) {
                boolean stage2 = cmd.hasNext() ? cmd.nextBoolean() : false;
                algorithm = new OptimalConstantExponentAlgorithm(stage2);
            }
            else if (algType.equals("totalbv")) {
                boolean stage2 = cmd.nextBoolean();
                algorithm = new TotalBiasVarianceAlgorithm(stage2);
            }
            else if (algType.equals("stage1")) {
                algorithm = new Stage1OnlyAlgorithm();
            }
            else
                throw new Exception("unknown algorithm: " + algType);
        }

        else if (base.equals("run")) {
            // how many sample paths
            int pathCount = cmd.nextInt();
            // total work per sample path
            int totalWork = cmd.nextInt();
            // observation interval
            int observationInterval = cmd.nextInt();
            // initial stage 1 samples
            int initialStage1Count = cmd.nextInt();
            // initial stage 2 samples;
            int initialStage2Count = cmd.nextInt();

            if (model == null)
                throw new Exception("model not set");
            if (algorithm == null)
                throw new Exception("algorithm not set");

            runSimulation(pathCount, 
                          totalWork, 
                          observationInterval, 
                          initialStage1Count, 
                          initialStage2Count);

        }

        else
            return false;

        return true;
    }

    // for keeping track of statistics 
    private class SimulationObservation {
        protected int work;
        
        protected SampleStatistics statsStage1Count 
            = new SampleStatistics();
        protected SampleStatistics statsAvgStage2Count 
            = new SampleStatistics();
        protected SampleStatistics statsMinStage2Count 
            = new SampleStatistics();
        protected SampleStatistics statsMaxStage2Count 
            = new SampleStatistics();
        protected SampleStatistics statsError 
            = new SampleStatistics();
        protected SampleStatistics statsBias2Estimate 
            = new SampleStatistics();
        protected SampleStatistics statsVarianceEstimate 
            = new SampleStatistics();
        protected SampleStatistics statsErrorSquared
            = new SampleStatistics();

        public SimulationObservation(final int work) {
            this.work = work; 
        }
    }
        

    protected void runSimulation(final int pathCount, 
                                 final int totalWork, 
                                 final int observationInterval,
                                 final int initialStage1Count,
                                 final int initialStage2Count) 
        throws MathException
    {
        Random baseRandom = getRandom();
        Map<Integer,SimulationObservation> statsMap 
            = new HashMap<Integer,SimulationObservation>();
        double probLossTrue = model.getProbLossTrue();
        SampleStatistics stage2Stats = new SampleStatistics();

        // run the simulation
        for (int path = 0; path < pathCount; path++) {
            ProbLoss p = new ProbLoss(model,
                                      getChildRandom(baseRandom),
                                      initialStage1Count,
                                      initialStage2Count);
            algorithm.initialize(p);

            while (true) {
                int work = p.getTotalStage2Count();

                // observe outcome ...
                if (work % observationInterval == 0) {
                    Integer key = new Integer(work);
                    SimulationObservation o = statsMap.get(key);
                    if (o == null) {
                        o = new SimulationObservation(work);
                        statsMap.put(key, o);
                    }

                    double estimate = p.getProbLossEstimate();
                    double error = estimate - probLossTrue;
                    int n = p.getStage1Count();
                    o.statsStage1Count.addSample(n);
                    o.statsAvgStage2Count.addSample(p.getAverageStage2Count());
                    o.statsError.addSample(error);
                    o.statsErrorSquared.addSample(error*error);
                    
                    stage2Stats.clear();
                    for (int i = 0; i < n; i++) 
                        stage2Stats.addSample(p.getStage2Count(i));
                    o.statsMinStage2Count
                        .addSample(stage2Stats.getQuantile(0.05));
                    o.statsMaxStage2Count
                        .addSample(stage2Stats.getQuantile(0.95));

                    double bias2 = p.getBiasEstimate();
                    bias2 *= bias2;
                    o.statsBias2Estimate.addSample(bias2);
                    double variance = p.getVarianceEstimate();
                    o.statsVarianceEstimate.addSample(variance);

                    if (isDebug()) 
                        p.printDebugInfo(System.out);

                }

                if (work >= totalWork)
                    break;
                
                algorithm.nextSample();
            }

            algorithm.reset();
        }

        // print the collected statistics
        SimulationObservation[] obvList
            = statsMap.values().toArray(new SimulationObservation[0]);
        Arrays.sort(obvList,
                    new Comparator<SimulationObservation>() {
                        public int compare(SimulationObservation o1,
                                           SimulationObservation o2) {
                            return o1.work - o2.work;
                        }
                    });

        // generate some regressions
        SimpleRegression regMSETrue = new SimpleRegression();
        SimpleRegression regBias2True = new SimpleRegression();
        SimpleRegression regVarianceTrue = new SimpleRegression();
        SimpleRegression regN = new SimpleRegression();
        SimpleRegression regMAvg = new SimpleRegression();

        // output statistics
        for (int i = 0; i < obvList.length; i++) {
            SimulationObservation o = obvList[i];
            if (o.statsError.getCount() != pathCount) 
                throw new RuntimeException("incorrect number of observations");

            System.out.printf("work: %d", o.work);

            double logWork = Math.log10(o.work);
            double mseTrue = o.statsErrorSquared.getMean();
            double biasTrue = o.statsError.getMean();
            double biasTrueSE = o.statsError.getStandardError();
            double bias2True = biasTrue*biasTrue;
            double stdevTrue = o.statsError.getStandardDeviation();
            double varianceTrue = stdevTrue*stdevTrue;
            
            // update regressions
            regMSETrue.addData(logWork, Math.log10(mseTrue));
            regBias2True.addData(logWork, Math.log10(bias2True));
            regVarianceTrue.addData(logWork, Math.log10(varianceTrue));
            regN.addData(logWork, Math.log10(o.statsStage1Count.getMean()));
            regMAvg.addData(logWork, 
                            Math.log10(o.statsAvgStage2Count.getMean()));

            System.out.printf(" MSE: %g se %g",
                              mseTrue,
                              o.statsErrorSquared.getStandardError());


            System.out.printf(" n: %.0f +- %.0f", 
                              o.statsStage1Count.getMean(),
                              o.statsStage1Count.getStandardDeviation());
            System.out.printf(" m_avg: %.0f +- %.0f", 
                              o.statsAvgStage2Count.getMean(),
                              o.statsAvgStage2Count.getStandardDeviation());

            System.out.printf(" m_05_95: %.0f %.0f", 
                              o.statsMinStage2Count.getMean(),
                              o.statsMaxStage2Count.getMean());
            
            System.out.printf(" bias2: %g", bias2True);
            System.out.printf(" bias2_e: %g +- %g", 
                              o.statsBias2Estimate.getMean(),
                              o.statsBias2Estimate.getStandardDeviation());

            System.out.printf(" var: %g", varianceTrue);
            System.out.printf(" var_e: %g +- %g", 
                              o.statsVarianceEstimate.getMean(),
                              o.statsVarianceEstimate.getStandardDeviation());

            System.out.println();
        }

        // output regression info
        System.out.printf("MSE_true_loglog_slope: %f se %f R2: %f\n",
                          regMSETrue.getSlope(),
                          regMSETrue.getSlopeStdErr(),
                          regMSETrue.getRSquare());
        System.out.printf("bias2_true_loglog_slope: %f se %f R2: %f\n",
                          regBias2True.getSlope(),
                          regBias2True.getSlopeStdErr(),
                          regBias2True.getRSquare());
        System.out.printf("var_true_loglog_slope: %f se %f R2: %f\n",
                          regVarianceTrue.getSlope(),
                          regVarianceTrue.getSlopeStdErr(),
                          regVarianceTrue.getRSquare());
        System.out.printf("n_loglog_slope: %f se %f R2: %f\n",
                          regN.getSlope(),
                          regN.getSlopeStdErr(),
                          regN.getRSquare());
        System.out.printf("m_avg_loglog_slope: %f se %f R2: %f\n",
                          regMAvg.getSlope(),
                          regMAvg.getSlopeStdErr(),
                          regMAvg.getRSquare());

    }

    public static void main(String[] argv) throws Exception {
        (new ProbLossMain()).run(argv);
    }

}