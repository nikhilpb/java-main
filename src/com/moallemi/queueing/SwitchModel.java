package com.moallemi.queueing;

import java.util.*;
import java.io.*;

import com.moallemi.adp.*;
import com.moallemi.util.*;
import com.moallemi.math.Distributions;

public class SwitchModel extends OpenQueueingNetworkModel {
    private int switchSize;
    // indexed by [sourcePort][destinationPort]
    private int[][] inputQueueMap;
    private int[] sourcePort;
    private int[] destPort;

    public SwitchModel(PropertySet props) {
        super(props);
    }

    protected void init(PropertySet props) {
        // some initialization
        switchSize = props.getInt("switch_size");
        String arrivalType = props.getString("arrival_type");

        int serverCount = switchSize;
        int queueCount = switchSize*switchSize;
        inputQueueMap = new int [switchSize][switchSize];
        sourcePort = new int [queueCount];
        destPort = new int [queueCount];
        int cnt = 0;
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                inputQueueMap[src][dest] = cnt;
                sourcePort[cnt] = src;
                destPort[cnt] = dest;
                cnt++;
            }
        }

        double[] lambda = new double [queueCount];
        if (arrivalType.equals("uniform")) {
            double rho = props.getDouble("rho");
            Arrays.fill(lambda, rho / ((double) switchSize));
        }
        else if (arrivalType.equals("random")) {
            double rho = props.getDouble("rho");
            long seed = props.getLongDefault("random_seed", 0L);
            String type = props.getString("dist_type");
            Random random = 
                new Random(seed >= 0L ? seed : System.currentTimeMillis());

            // generate random arrival rates
            for (int i = 0; i < queueCount; i++) {
                if (type.equals("exponential")) 
                    lambda[i] = Distributions.nextExponential(random, 1.0);
                else if (type.equals("uniform"))
                    lambda[i] = random.nextDouble();
                else
                    throw new IllegalArgumentException("unknown distribution: "
                                                       + type);
            }

            // normalize to achieve load rho
            double max = 0.0;
            for (int src = 0; src < switchSize; src++) {
                double sum = 0.0;
                for (int dest = 0; dest < switchSize; dest++)
                    sum += lambda[inputQueueMap[src][dest]];
                if (sum > max)
                    max = sum;
            }
            for (int dest = 0; dest < switchSize; dest++) {
                double sum = 0.0;
                for (int src = 0; src < switchSize; src++) 
                    sum += lambda[inputQueueMap[src][dest]];
                if (sum > max)
                    max = sum;
            }
            double scale = rho / max;
            for (int i = 0; i < queueCount; i++)
                    lambda[i] *= scale;

        }
        else if (arrivalType.equals("specified")) {
            for (int src = 0; src < switchSize; src++) {
                for (int dest = 0; dest < switchSize; dest++) {
                    double v = props.getDoubleDefault("lambda[" + (src+1)
                                                      + "][" + (dest+1) + "]",
                                                      0.0);
                    lambda[inputQueueMap[src][dest]] = v;
                }
            }

        }
        else
            throw new IllegalArgumentException("unknown arrival distribution "
                                               + arrivalType);

        double[][] mu = new double [queueCount][serverCount];
        for (int i = 0; i < queueCount; i++) {
            int dest = destPort[i];
            for (int j = 0; j < serverCount; j++) 
                mu[i][j] = dest == j ? 1.0 : 0.0;
        }

        double[][] routing = new double [queueCount][queueCount+1];
        for (int i = 0; i < queueCount; i++) 
            routing[i][queueCount] = 1.0;

        double[] cost = new double [queueCount];
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                double v = props.getDoubleDefault("cost[" + (src+1)
                                                  + "][" + (dest+1) + "]",
                                                  1.0);
                cost[inputQueueMap[src][dest]] = v;
            }
        }
        double[] rejectionCost = new double [queueCount];
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                double v = props.getDoubleDefault("rejection_cost[" + (src+1)
                                                  + "][" + (dest+1) + "]",
                                                  0.0);
                rejectionCost[inputQueueMap[src][dest]] = v;
            }
        }      

        int maxQueueLength = props.getInt("max_queue_length");

        init(lambda, mu, routing, maxQueueLength, cost, rejectionCost);
    }            
    
    protected void initActions() {
        ServerAction[] actions = ServerAction.enumerate(this);
        ArrayList<ServerAction> list = new ArrayList<ServerAction>();

        boolean[] used = new boolean [switchSize];
        outer:
        for (int a = 0; a < actions.length; a++) {
            ServerAction action = actions[a];
            Arrays.fill(used, false);

            // see if this action is a matching
            for (int j = 0; j < switchSize; j++) {
                int q = action.getQueueWorkedOn(j);
                if (q < 0)
                    continue;

                // check to see each server is working on a queue
                // that belongs to it
                if (destPort[q] != j)
                    continue outer;

                // check to see that no two servers are working on the
                // same source
                if (used[sourcePort[q]])
                    continue outer;
                used[sourcePort[q]] = true;
            }
            
            list.add(action);
        }

        super.allActions = list.toArray(new ServerAction [0]);
    }

    protected boolean isWorkConserving(QueueState state, ServerAction action) 
    {
        boolean[] used = new boolean [switchSize];
        Arrays.fill(used, false);
	for (int j = 0; j < switchSize; j++) {
            int q = action.getQueueWorkedOn(j);
            if (q >= 0)
                used[sourcePort[q]] = true;
	}
	for (int j = 0; j < switchSize; j++) {
            int q = action.getQueueWorkedOn(j);
            if (q < 0) {
                for (int sP = 0; sP < switchSize; sP++) {
                    if (!used[sP]) {
                        q = inputQueueMap[sP][j];
                        if (getServiceRate(q, j) > 0.0 
                            && state.getQueueLength(q) > 0)
                            return false;
                    }
                }
            }
        }
	return true;
    }

    public int getSourcePort(int queueIndex) {
        return sourcePort[queueIndex];
    }

    public int getDestPort(int queueIndex) {
        return destPort[queueIndex];
    }

    public int getQueueIndex(int src, int dest) {
        return inputQueueMap[src][dest];
    }

    public int getSwitchSize() {
        return switchSize;
    }

    public void dumpInfo(PrintStream out) throws IOException {
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = getQueueIndex(src, dest);
                if (getArrivalRate(q) > 0.0)
                    out.println("lambda[" + (src+1) + "]["
                                + (dest+1) + "]= " +
                                getArrivalRate(q));
            }
        }
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = getQueueIndex(src, dest);
                out.println("cost[" + (src+1) + "]["
                            + (dest+1) + "]= " 
                            + getCost(q));
            }
        } 
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = getQueueIndex(src, dest);
                if (getRejectionCost(q) > 0.0)
                    out.println("rejection_cost[" + (src+1) + "]["
                                + (dest+1) + "]= " 
                                + getRejectionCost(q));
            }
        } 
    }

    public void writeModel(PrintStream out) throws IOException {
        out.println("switch_size = " + switchSize);
        out.println("arrival_type = specified");
        out.println("max_queue_length = " + getMaxQueueLength());
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = getQueueIndex(src, dest);
                if (getArrivalRate(q) > 0.0)
                    out.println("lambda[" + (src+1) + "]["
                                + (dest+1) + "]= " 
                                + getArrivalRate(q));
            }
        }
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = getQueueIndex(src, dest);
                if (getCost(q) != 1.0)
                    out.println("cost[" + (src+1) + "]["
                                + (dest+1) + "]= " 
                                + getCost(q));
            }
        } 
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = getQueueIndex(src, dest);
                if (getRejectionCost(q) > 0.0)
                    out.println("rejection_cost[" + (src+1) + "]["
                                + (dest+1) + "]= " 
                                + getRejectionCost(q));
            }
        } 
    }
        
}