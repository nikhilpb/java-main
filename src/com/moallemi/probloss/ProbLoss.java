package com.moallemi.probloss;

import java.io.*;
import java.util.*;

import com.moallemi.math.NormalDistribution;
import com.moallemi.math.stats.*;

/**
 * Class for 2 stage Monte Carlo loss probability estimation.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2009-02-16 21:57:38 $
 */
public class ProbLoss {
    // source of randomness for stage 2 samples
    private Random baseRandom;
    // source of randomness for stage 1 samples
    private Random zRandom;

    // parameters of the distribution we are estimating
    private ProbLossModel model;

    // stage 1 sample
    public class Stage1Sample {
        // the index in the list of this sample
        private int index;
        // the true value at this sample
        private double Z;
        // underlying stage 2 samples
        private MVSampleStatistics samples
            = new MVSampleStatistics();
        // random number generator for child samples
        private Random random;
        // localized normal value estimate, bias estimate
        private boolean hasStats = false;
        private double normalValue = Double.NaN;
        private double localBias = Double.NaN;
        private double localBiasImprovement = Double.NaN;

        public Stage1Sample(final int index,
                            final double Z, 
                            final Random random) 
        {
            this.index = index;
            this.Z = Z;
            this.random = random;
        }

        public int getIndex() { return index; }
        public double getZ() { return Z; }
        public int getCount() { return samples.getCount(); }
        public double getEstimatedZ() { return samples.getMean(); }

        private void updateStats() {
            double sigma2 = model.getSigma2();
            double threshold = model.getThreshold();
            double m = samples.getCount();
            double v = (Math.sqrt(m) / sigma2) * (Z - threshold);
            double vPlus1 = (Math.sqrt(m + 1.0) / sigma2)
                * (Z - threshold);
            normalValue = NormalDistribution.gsl_cdf_ugaussian_P(v);
            if (v < 0.0) {
                // localBias = normalValue;
                localBias = -normalValue;
                localBiasImprovement = 
                    -NormalDistribution.gsl_cdf_ugaussian_P(vPlus1) 
                    - localBias;
            }
            else {
                // localBias = -NormalDistribution.gsl_cdf_ugaussian_Q(v);
                localBias = NormalDistribution.gsl_cdf_ugaussian_P(-v);
                localBiasImprovement = 
                    NormalDistribution.gsl_cdf_ugaussian_P(-vPlus1) 
                    - localBias;
            }
            hasStats = true;
            // debugging
            if (Double.isInfinite(normalValue)) {
                System.out.printf("m = %d Z = %f v = %f", 
                                  samples.getCount(),
                                  Z,
                                  v);
                throw new RuntimeException("normalValue nan");
            }
        }
            
        public double getNormalValue() {
            if (!hasStats) updateStats();
            return normalValue; 
        }
        public double getLocalBias() {
            if (!hasStats) updateStats();
            return localBias; 
        }
        public double getLocalBiasImprovement() {
            if (!hasStats) updateStats();
            return localBiasImprovement; 
        }

        protected void addSample() {
            double x = model.nextStage2Sample(random, Z);
            samples.addSample(x);
            hasStats = false;
        }
    }

    // list of stage 1 samples
    ArrayList<Stage1Sample> stage1List = new ArrayList<Stage1Sample>();
    // count of total stage 2 samples
    int stage2Count = 0;

    // constructor
    public ProbLoss(final ProbLossModel model,
                    final Random random) {
        this.model = model;
        this.baseRandom = new Random(random.nextLong());
        this.zRandom = random;
    }

    // constructor
    public ProbLoss(final ProbLossModel model,
                    final Random random,
                    final int initialStage1Count,
                    final int initialStage2Count) 
    {
        this(model, random);
        if (initialStage1Count < 1 || initialStage2Count < 1)
            throw new RuntimeException("initial stage 1 count and "
                                       + "initial stage 2 count must "
                                       + "be at least 1");
        for (int i = 0; i < initialStage1Count; i++) {
            addStage1Sample();
            // we get 1 stage sample automatically
            for (int j = 1; j < initialStage2Count; j++) 
                addStage2Sample(i);
        }
    }

    // accessors

    public int getStage1Count() { return stage1List.size(); }
    public int getStage2Count(final int i) { 
        return stage1List.get(i).getCount(); 
    }
    public int getTotalStage2Count() { return stage2Count; }
    public double getAverageStage2Count() { 
        return ((double) stage2Count) / ((double) stage1List.size()); 
    }
    public Stage1Sample getStage1Sample(int i) { return stage1List.get(i); }
    public ProbLossModel getModel() { return model; }
    
    public double getProbLossEstimate() {
        int n = stage1List.size();
        int lossCount = 0;
        double threshold = model.getThreshold();
        for (int i = 0; i < n; i++) {
            if (stage1List.get(i).getEstimatedZ() <= threshold) 
                lossCount++;
        }
        return ((double) lossCount) / ((double) n);
    }

    public double getBiasEstimate() {
        int n = stage1List.size();
        double estimate = 0.0;
        double threshold = model.getThreshold();
        for (int i = 0; i < n; i++) {
            Stage1Sample stage1 = stage1List.get(i);
            estimate += stage1.getLocalBias();
        }
        return estimate / ((double) n);
    }

    public double getVarianceEstimate() {
        int n = stage1List.size();
        double mu = 0.0;
        for (int i = 0; i < n; i++) 
            mu += stage1List.get(i).getNormalValue();
        mu /= (double) n;
        return mu * (1.0 - mu) / ((double) n);
    }

    // modifiers

    public void addStage2Sample(int i) {
        Stage1Sample stage1 = stage1List.get(i);
        stage1.addSample();
        stage2Count++;
    }

    public Stage1Sample addStage1Sample() {
        double Z = model.nextStage1Sample(zRandom);
        Random childRandom = new Random(baseRandom.nextLong());
        int n = stage1List.size();
        Stage1Sample stage1 = new Stage1Sample(n, Z, childRandom);
        stage1List.add(stage1);
        addStage2Sample(n);
        return stage1;
    }

    public void printDebugInfo(PrintStream out) {
        Stage1Sample[] s = stage1List.toArray(new Stage1Sample [0]);
        Arrays.sort(s, comparatorZValue());


        System.out.printf("DEBUG stage 2 samples, work: %d n: %d m_bar: %f\n",
                          getTotalStage2Count(),
                          getStage1Count(),
                          getAverageStage2Count());
        System.out.printf("bias_real: %g bias_estimate: %g " 
                          + "variance_estimate: %g\n",
                          getProbLossEstimate() - model.getProbLossTrue(),
                          getBiasEstimate(),
                          getVarianceEstimate());
                          
        int n = s.length;
        for (int i = 0; i < n; i++) 
            out.printf("Z: %f m: %d local_bias: %g\n",
                       s[i].getZ(),
                       s[i].getCount(),
                       s[i].getLocalBias());


        System.out.println();
    }


    // comparators for Stage1Sample

    // sort by number of stage 2 samples, ascending
    public static Comparator<Stage1Sample> comparatorStage2Count() {
        return new Comparator<ProbLoss.Stage1Sample> () {
            public int compare(ProbLoss.Stage1Sample o1,
                               ProbLoss.Stage1Sample o2) 
            {
                return o1.getCount() - o2.getCount();
            }
        };
    }

    // sort by abs(local bias improvement), descending
    public static Comparator<Stage1Sample> comparatorLocalBiasImprovement() {
        return new Comparator<Stage1Sample> () {
            public int compare(Stage1Sample o1,
                               Stage1Sample o2) 
            {
                double b1, b2;
                b1 = Math.abs(o1.getLocalBiasImprovement());
                b2 = Math.abs(o2.getLocalBiasImprovement());
                if (b1 < b2)
                    return 1;
                if (b1 > b2)
                    return -1;
                return 0;
            }
        };
    }

    // sort by Z, ascending
    public static Comparator<Stage1Sample> comparatorZValue() {
        return new Comparator<Stage1Sample> () {
            public int compare(Stage1Sample o1,
                               Stage1Sample o2) 
            {
                double z1, z2;
                z1 = o1.getZ();
                z2 = o2.getZ();
                if (z1 < z2)
                    return -1;
                if (z1 > z2)
                    return 1;
                return 0;
            }
        };
    }

}
    