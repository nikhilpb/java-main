package com.moallemi.iqswitch;

import java.util.*;
import java.io.*;

import com.moallemi.adp.*;
import com.moallemi.util.*;
import com.moallemi.math.Distributions;

public class SwitchModel {
    private int switchSize;
    private double[][] lambda;
    private double[][] cost;

    public SwitchModel(PropertySet props) {
        switchSize = props.getInt("switch_size");
        String arrivalType = props.getString("arrival_type");

        lambda = new double [switchSize][switchSize];
        if (arrivalType.equals("uniform")) {
            double rho = props.getDouble("rho");
            for (int src = 0; src < switchSize; src++)
                Arrays.fill(lambda[src], rho / ((double) switchSize));
        }
        else if (arrivalType.equals("diagonal")) {
            double rho = props.getDouble("rho");
            int k = props.getInt("k");
            double falloff = props.getDouble("falloff");
            if (k > switchSize)
                throw new IllegalArgumentException("k too large");
            for (int src = 0; src < switchSize; src++) {
		double l = 1.0;
                for (int i = 0; i < k; i++) {
                    int dest = (src + i) % switchSize;
                    lambda[src][dest] = l;
		    l *= falloff;
                }
            }
	    normalize(rho);
        }
        else if (arrivalType.equals("random")) {
            double rho = props.getDouble("rho");
            long seed = props.getLongDefault("random_seed", -1L);
            String type = props.getString("dist_type");
            Random random = 
                new Random(seed >= 0L ? seed : System.currentTimeMillis());

            // generate random arrival rates
            for (int src = 0; src < switchSize; src++) {
                for (int dest = 0; dest < switchSize; dest++) {
                    if (type.equals("exponential")) 
                        lambda[src][dest]
                            = Distributions.nextExponential(random, 1.0);
                    else if (type.equals("uniform"))
                        lambda[src][dest] = random.nextDouble();
                    else
                        throw new IllegalArgumentException("unknown "
                                                           + "distribution: "
                                                           + type);
                }
            }

	    normalize(rho);
        }
        else if (arrivalType.equals("specified")) {
            for (int src = 0; src < switchSize; src++) {
                for (int dest = 0; dest < switchSize; dest++) {
                    double v = props.getDoubleDefault("lambda[" + (src+1)
                                                      + "][" + (dest+1) + "]",
                                                      0.0);
                    lambda[src][dest] = v;
                }
            }

        }
        else
            throw new IllegalArgumentException("unknown arrival distribution "
                                               + arrivalType);

        cost = new double [switchSize][switchSize];
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                double v = props.getDoubleDefault("cost[" + (src+1)
                                                  + "][" + (dest+1) + "]",
                                                  1.0);
                cost[src][dest] = v;
            }
        }

        initArrivalProbabilities();
    }            

    // normalize to load rho
    private void normalize(double rho) {
	// normalize to achieve load rho
	double max = 0.0;
	for (int src = 0; src < switchSize; src++) {
	    double sum = 0.0;
	    for (int dest = 0; dest < switchSize; dest++)
		sum += lambda[src][dest];
	    if (sum > max)
		max = sum;
	}
	for (int dest = 0; dest < switchSize; dest++) {
	    double sum = 0.0;
	    for (int src = 0; src < switchSize; src++) 
		sum += lambda[src][dest];
	    if (sum > max)
		max = sum;
	}
	double scale = rho / max;
	for (int src = 0; src < switchSize; src++) 
	    for (int dest = 0; dest < switchSize; dest++) 
		lambda[src][dest] *= scale;
    }

    public Function getCostFunction() {
        return new Function() {
            public double getValue(SwitchState state) {
                double sum = 0.0;
                for (int src = 0; src < switchSize; src++) 
                    for (int dest = 0; dest < switchSize; dest++) 
                        sum += cost[src][dest] * 
                            state.getQueueLength(src, dest);
                return sum;
            }
        };
    }

    public int getSwitchSize() { return switchSize; }

    public double getArrivalProbability(int src, int dest) {
        return lambda[src][dest];
    }

    public SwitchState getBaseState() {
        int[][] q = new int [switchSize][switchSize];
        return new SwitchState(q);
    }

    public double getLoadFactor() {
        double load = 0.0;
        for (int src = 0; src < switchSize; src++) {
            double sum = 0.0;
            for (int dest = 0; dest < switchSize; dest++) 
                sum += lambda[src][dest];
            if (sum > load)
                load = sum;
        }
        for (int dest = 0; dest < switchSize; dest++) {
            double sum = 0.0;
            for (int src = 0; src < switchSize; src++) 
                sum += lambda[src][dest];
            if (sum > load)
                load = sum;
        }
        return load;
    }


    // probabilities for arrival in a sort or destination port
    // indexed by [src][arrivalCount]
    private double[][] sourceArrivalProbability;
    // indexed by [dest][arrivalCount]
    private double[][] destArrivalProbability;

    private void initArrivalProbabilities() {
        double[] cur = new double [switchSize+1];
        double[] tmp = new double [switchSize+1];

        sourceArrivalProbability = new double [switchSize][switchSize+1];
        for (int src = 0; src < switchSize; src++) {
            double[] p = sourceArrivalProbability[src];
            // compute cumulative probabilities by convolution
            p[0] = 1.0;
            for (int dest = 0; dest < switchSize; dest++) {
                double arrivalP = lambda[src][dest];
                double noArrivalP = 1.0 - lambda[src][dest];
                for (int i = dest + 1; i >= 1; i--)
                    p[i] = arrivalP * p[i-1] + noArrivalP * p[i];
                p[0] *= noArrivalP;
            }
            // sanity check
            double sum = 0.0;
            for (int total = 0; total <= switchSize; total++) 
                sum += sourceArrivalProbability[src][total];
            if (Math.abs(sum - 1.0) > 1e-10)
                throw new IllegalStateException("probability does not "
                                                + "sum to 1.0");
        }

        destArrivalProbability = new double [switchSize][switchSize+1];
        for (int dest = 0; dest < switchSize; dest++) {
            double[] p = destArrivalProbability[dest];
            // compute cumulative probabilities by convolution
            p[0] = 1.0;
            for (int src = 0; src < switchSize; src++) {
                double arrivalP = lambda[src][dest];
                double noArrivalP = 1.0 - lambda[src][dest];
                for (int i = src + 1; i >= 1; i--)
                    p[i] = arrivalP * p[i-1] + noArrivalP * p[i];
                p[0] *= noArrivalP;
            }
            // sanity check
            double sum = 0.0;
            for (int total = 0; total <= switchSize; total++) 
                sum += destArrivalProbability[dest][total];
            if (Math.abs(sum - 1.0) > 1e-10)
                throw new IllegalStateException("probability does not "
                                                + "sum to 1.0");
        }
    }
            

    public double getSourceArrivalProbability(int src, int arrivalCount) {
        return arrivalCount < 0 || arrivalCount > switchSize
            ? 0.0
            : sourceArrivalProbability[src][arrivalCount];
    }

    public double getDestArrivalProbability(int dest, int arrivalCount) {
        return arrivalCount < 0 || arrivalCount > switchSize
            ? 0.0
            : destArrivalProbability[dest][arrivalCount];
    }

    public SwitchState sampleNextState(SwitchState state,
                                       SwitchAction action,
                                       Random r) {
        int[][] out = new int [switchSize][switchSize];
        state.copyQueueLengths(out);
        
        for (int dest = 0; dest < switchSize; dest++) {
            // services
            int wSrc = action.getSourceWorkedOn(dest);
            if (wSrc >= 0) {
                if (out[wSrc][dest] <= 0)
                    throw new IllegalStateException("working on empty queue");
                out[wSrc][dest]--;
            }

            // arrivals
            for (int src = 0; src < switchSize; src++) {
                if (r.nextDouble() <= lambda[src][dest])
                    out[src][dest]++;
            }
        }

        return new SwitchState(out);
    }

    public void dumpInfo(PrintStream out) throws IOException {
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                if (lambda[src][dest] != 0.0) {
                    out.println("lambda[" + (src+1) 
                                + "][" + (dest+1) + "] = "
                                + lambda[src][dest]);
                }
            }
        }
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                if (cost[src][dest] != 1.0) {
                    out.println("cost[" + (src+1) 
                                + "][" + (dest+1) + "] = "
                                + cost[src][dest]);
                }
            }
        }
    }


}
