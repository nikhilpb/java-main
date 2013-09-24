package com.moallemi.queueing;

import java.util.*;

import com.moallemi.adp.*;
import com.moallemi.math.DiscreteDistribution;

public class ServerAction implements Action {
    private OpenQueueingNetworkModel model;
    private int[] working;
    private DiscreteDistribution distribution;
    private int[][] offsets;

    public ServerAction(OpenQueueingNetworkModel model, 
                        int[] working) 
    {
        this.model = model;
	this.working = working;

        int serverCount = model.getServerCount();
        int queueCount = model.getQueueCount();

        // count the number of possible next states
        int nextStateCount = 0;
        for (int i = 0; i < queueCount; i++) {
            if (model.getArrivalRate(i) > 0.0)
                nextStateCount++;
            for (int j = 0; j < serverCount; j++) {
                if (model.getServiceRate(i,j) > 0.0) {
                    for (int r = 0; r <= queueCount; r++) {
                        if (model.getRouteProbability(i, r) > 0.0) {
                            nextStateCount++;
                        }
                    }
                }
            }
        }
        
        // construct the distribution
        int count = 0;
        double[] probability = new double [nextStateCount];
        offsets = new int [nextStateCount][];
        double normalization = model.getNormalization();

        // arrivals
        for (int i = 0; i < queueCount; i++) {
            double lambda = model.getArrivalRate(i);
            if (lambda > 0.0) {
                offsets[count] = new int [queueCount];
                offsets[count][i] = 1;
                probability[count] = lambda / normalization;
                count++;
            }
        }

        // services
        for (int i = 0; i < queueCount; i++) {
            for (int j = 0; j < serverCount; j++) {
                double mu = model.getServiceRate(i, j);
                if (mu > 0.0) {
                    for (int r = 0; r <= queueCount; r++) {
                        double p = mu * model.getRouteProbability(i, r);
                        if (p > 0.0) {
                            if (working[j] == i) {
                                offsets[count] = new int [queueCount];
                                offsets[count][i] = -1;
                                if (r < queueCount)
                                    offsets[count][r] = 1;
                            }
                            probability[count] = p / normalization;
                            count++;
                        }
                    }
                }
            }
        }

        if (count != nextStateCount)
            throw new IllegalStateException("bad next state count");

        distribution = new DiscreteDistribution(probability);
    }

    protected DiscreteDistribution getDistribution() {
        return distribution;
    }

    protected int[] getOffsets(int position) {
        return offsets[position];
    }

    public int getQueueWorkedOn(int server) {
	return working[server];
    }
    
    public int getServerCount() { return working.length; }

    public boolean isCompatible(State state) {
        QueueState qState = (QueueState) state;
	for (int j = 0; j < working.length; j++) {
            if (working[j] >= 0) {
                if (qState.getQueueLength(working[j]) <= 0)
                    return false;
                double mu = model.getServiceRate(working[j], j);
                if (mu <= 0.0)
                    return false;
            }
	}
	return true;
    }

    public boolean equals(Object other) {
	if (other instanceof ServerAction) {
            ServerAction o = (ServerAction) other;
	    if (this.working.length != o.working.length)
		return false;
	    for (int i = 0; i < this.working.length; i++) {
		if (this.working[i] != o.working[i])
		    return false;
	    }
	    return true;
	}
	return false;
    }
    
    public int hashCode() {
	int h = 0;
	for (int i = 0; i < working.length; i++)
	    h = h*137 + working[i];
	return h;
    }


    public static ServerAction[] enumerate(OpenQueueingNetworkModel model) {
        int serverCount = model.getServerCount();
        int queueCount = model.getQueueCount();
        int[] working = new int [serverCount];
        Arrays.fill(working, -1);
        ArrayList<ServerAction> list = new ArrayList<ServerAction> ();
        boolean found = true;
        while (found) {
            int[] copy = new int [serverCount];
            System.arraycopy(working, 0, copy, 0, serverCount);
            list.add(new ServerAction(model, copy));

            found = false;
            for (int j = 0; j < serverCount && !found; j++) {
                while (++working[j] < queueCount) {
                    if (model.getServiceRate(working[j], j) > 0.0)
                        break;
                }
                if (working[j] >= queueCount) 
                    working[j] = -1;
                else 
                    found = true;
            }
        }

        return list.toArray(new ServerAction [0]);
    }
            
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < working.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(working[i]+1);
        }
        return sb.toString();
    }

}
