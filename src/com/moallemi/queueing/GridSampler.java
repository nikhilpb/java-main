package com.moallemi.queueing;

import java.util.*;

import com.moallemi.adp.*;
import com.moallemi.math.Distributions;
import com.moallemi.math.stats.ObjectHistogram;
import com.moallemi.util.*;
import com.moallemi.util.data.*;

public class GridSampler implements StateSampler {
    private OpenQueueingNetworkModel model;
    private double gamma;
    private int cutoff;
    private QueueStateSymmetry symmetry;
    private StateList states;
    private double[] weights;

    public GridSampler(OpenQueueingNetworkModel model,
                       double gamma,
                       QueueStateSymmetry symmetry,
                       int cutoff) 
    {
        this.model = model;
        this.gamma = gamma;
        this.symmetry = symmetry;
        this.cutoff = cutoff;
    }

    public void sample(Random random, int sampleCount) {
        int queueCount = model.getQueueCount();
        int maxQueueLength = model.getMaxQueueLength();


        TupleIterator i = new TupleIterator(queueCount, cutoff + 1);
        int theoreticalGridSize = i.getSize();
        ObjectHistogram<QueueState> gridHistogram 
            = new ObjectHistogram<QueueState> (2*theoreticalGridSize + 1);
        while (i.hasNext()) {
            int[] tuple = i.next();
            int[] copy = new int [queueCount];
            System.arraycopy(tuple, 0, copy, 0, queueCount);

            if (symmetry != null)
                symmetry.canonicalForm(copy);
                 
            // add it to the map
            QueueState state = new QueueState(copy);
            gridHistogram.add(state);
        }
        int gridSize = gridHistogram.getTotalCount();
        if (gridSize != i.getSize())
            throw new IllegalStateException("grid size inconsistent");

        if (sampleCount > 0 && gridSize > sampleCount) 
            throw new IllegalArgumentException("sample count "
                                               + sampleCount
                                               + " too small, grid size is "
                                               + gridSize);

        // sample for the rest
        int remainingSamples = 0;
        if (sampleCount > 0)
            remainingSamples = sampleCount - gridSize;
        ObjectHistogram<QueueState> sampleHistogram = 
            new ObjectHistogram<QueueState> (2*remainingSamples + 1);
        for (int c = 0; c < remainingSamples; c++) {
            int[] queueLengths = new int [queueCount];

            // sample a state outside the grid
            boolean reject = true;
            do {
                
                for (int j = 0; j < queueCount; j++)  {
                    int x = Distributions.nextGeometric(random, 
                                                        gamma,
                                                        maxQueueLength + 1);
                    if (x > cutoff)
                        reject = false;
                    queueLengths[j] = x;
                }
            } while (reject);

            if (symmetry != null)
                symmetry.canonicalForm(queueLengths);

            // add it to the histogram
            QueueState state = new QueueState(queueLengths);
            sampleHistogram.add(state);
        }


        int totalPoints = gridHistogram.getNumBins() 
            + sampleHistogram.getNumBins();
        State[] stateArray = new State [totalPoints];
        weights = new double [totalPoints];
        int cnt = 0;

        // add points on the grid
        double totalGridMass = 0.0;
        double theoreticalGridMass = 
            Math.pow(1.0 - Math.pow(gamma, cutoff + 1),
                     queueCount);
        double factor = Math.pow(1.0 - gamma, queueCount);
        for (Iterator<QueueState> j = gridHistogram.binIterator(); 
             j.hasNext(); ) {
            QueueState state = j.next();
            double w =
                Math.pow(gamma, state.getTotalQueueLength())
                * ((double) gridHistogram.getBinCount(state))
                * factor;
            totalGridMass += w;
            if (remainingSamples == 0)
                w /= theoreticalGridMass;

            stateArray[cnt] = state;
            weights[cnt] = w;
            cnt++;
        }
        if (Math.abs(theoreticalGridMass - totalGridMass) > 1e-6)
            throw new IllegalStateException("bad grid mass");

        // add points outside the grid
        if (remainingSamples > 0) {
            double f = (1.0 - totalGridMass) / ((double) remainingSamples);
            for (Iterator<QueueState> j = sampleHistogram.binIterator(); 
                 j.hasNext(); ) {
                QueueState state = j.next();
                double w = ((double) sampleHistogram.getBinCount(state)) * f;

                stateArray[cnt] = state;
                weights[cnt] = w;
                cnt++;
            }
        }

        if (cnt != stateArray.length)
            throw new IllegalStateException("badly allocated array");

        states = new StateList(model, stateArray);
    }

    public StateList getStateList() { return states; }
    public double[] getWeights() { return weights; }
}
            

            
