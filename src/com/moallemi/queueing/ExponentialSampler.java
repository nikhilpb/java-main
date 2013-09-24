package com.moallemi.queueing;

import java.util.*;

import com.moallemi.adp.*;
import com.moallemi.math.Distributions;
import com.moallemi.math.stats.ObjectHistogram;
import com.moallemi.util.data.MutableInt;

public class ExponentialSampler implements StateSampler {
    private OpenQueueingNetworkModel model;
    private double gamma;
    private QueueStateSymmetry symmetry;
    private StateList states;
    private double[] weights;

    public ExponentialSampler(OpenQueueingNetworkModel model,
                              double gamma,
                              QueueStateSymmetry symmetry) {
        this.model = model;
        this.gamma = gamma;
        this.symmetry = symmetry;
    }

    public void sample(Random random, int sampleCount) {
        int queueCount = model.getQueueCount();
        int maxQueueLength = model.getMaxQueueLength();
        
        ObjectHistogram<QueueState> sampleHistogram = 
            new ObjectHistogram<QueueState> (2*sampleCount + 1);
        for (int c = 0; c < sampleCount; c++) {
            int[] queueLengths = new int [queueCount];
            // sample a state
            for (int i = 0; i < queueCount; i++) 
                queueLengths[i] = 
                    Distributions.nextGeometric(random, 
                                                gamma,
                                                maxQueueLength + 1);

            if (symmetry != null)
                symmetry.canonicalForm(queueLengths);
            
            QueueState state = new QueueState(queueLengths);
            sampleHistogram.add(state);
        }

        int stateCount = sampleHistogram.getNumBins();
        State[] stateArray = new State [stateCount];
        weights = new double [stateCount];
        int cnt = 0;
        for (Iterator<QueueState> j = sampleHistogram.binIterator(); 
             j.hasNext(); ) {
            QueueState state = j.next();
            stateArray[cnt] = state;
            weights[cnt] = sampleHistogram.getBinFrequency(state);
            cnt++;
        }

        states = new StateList(model, stateArray);
    }

    public StateList getStateList() { return states; }
    public double[] getWeights() { return weights; }
}
            

            
