package com.moallemi.adp;

import java.util.*;

import com.moallemi.math.*;
import com.moallemi.math.stats.*;

/**
 * Evaluate a function via monte-carlo.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2006-06-15 21:02:08 $
 */
public class MonteCarloEvaluator {
    private SampleStatistics stats;
    private PathSimulator simulator;
    private Random baseRandom;
    private StateFunction function;
    private int timeStepCount;
    private int samplePathCount;
    private long seed = 0L;
    private MonteCarloStateObserver observer;

    /**
     * Constructor.
     *
     * @param simulator the simulation engine
     * @param baseRandom a source of randomness
     * @param function the function
     * @param timeStepCount number of time steps
     * @param samplePathCount number of sample paths
     */
    public MonteCarloEvaluator(PathSimulator simulator,
                               Random baseRandom,
                               StateFunction function,
                               int timeStepCount,
                               int samplePathCount,
                               MonteCarloStateObserver observer) 
    {
        this.simulator = simulator;
        this.baseRandom = baseRandom;
        this.function = function;
        this.timeStepCount = timeStepCount;
        this.samplePathCount = samplePathCount;
        this.observer = observer;
        stats = new SampleStatistics(samplePathCount);
    }

    public boolean hasNext() { return stats.getCount() < samplePathCount; }

    public double next() {
        MVSampleStatistics thisStats = new MVSampleStatistics();
        int path = stats.getCount();
        if (path >= samplePathCount)
            throw new IllegalStateException("no more paths left");

        // use antithetic sampling
        Random random;
        if (path % 2 == 0) {
            seed = baseRandom.nextLong();
            random = new Random(seed);
        }
        else {
            random = new MirroredRandom(new Random(seed));
        }

        simulator.reset(random);
        State lastState = null;
        double lastValue = 0.0;
        for (int time = 0; time < timeStepCount; time++) {
            if (!simulator.hasNext()) 
                throw new IllegalStateException("no next state");
            State nextState = simulator.next();
            if (observer != null)
                observer.observe(path, time + 1, nextState);

            // save a little time on self-transitions
            if (nextState != lastState) {
                lastState = nextState;
                lastValue = function.getValue(lastState);
            }
            thisStats.addSample(lastValue);
        }

        double value = thisStats.getMean();
        stats.addSample(value);

        return value;
    }

    /**
     * Get the sample statistics for this simulation.
     *
     * @return the sample statistics
     */
    public SampleStatistics getSampleStatistics() { return stats; }
}
