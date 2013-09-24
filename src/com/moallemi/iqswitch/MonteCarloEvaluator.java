package com.moallemi.iqswitch;

import java.util.*;

import com.moallemi.math.*;
import com.moallemi.math.stats.*;

public class MonteCarloEvaluator {
    private SwitchModel model;
    private SampleStatistics stats;
    private PathSimulator simulator;
    private Function function;
    private Random baseRandom;
    private int timeStepCount;
    private int samplePathCount;
    private long seed = 0L;
    private MonteCarloStateObserver observer;
    private double lastValue = 0.0;
    private int path;

    public MonteCarloEvaluator(SwitchModel model,
                               PathSimulator simulator,
                               Random baseRandom,
                               Function function,
                               int timeStepCount,
                               int samplePathCount,
                               MonteCarloStateObserver observer) 
    {
        this.model = model;
        this.simulator = simulator;
        this.baseRandom = baseRandom;
        this.function = function;
        this.timeStepCount = timeStepCount;
        this.samplePathCount = samplePathCount;
        this.observer = observer;
        stats = new SampleStatistics(samplePathCount);

        if (samplePathCount % 2 != 0)
            throw new IllegalArgumentException("number of paths must be even");
        path = 0;
    }

    public boolean hasNext() { return path < samplePathCount; }

    public int getPath() { return path; }

    public double next() {
        MVSampleStatistics thisStats = new MVSampleStatistics();
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
        for (int time = 0; time < timeStepCount; time++) {
            SwitchState nextState = simulator.next();
            if (observer != null)
                observer.observe(path, time + 1, nextState);
            double x = function.getValue(nextState);
            thisStats.addSample(x);
        }

        double value = thisStats.getMean();
        if (path % 2 == 1) 
            stats.addSample(0.5 * (value + lastValue));
        else
            lastValue = value;

        path++;
        return value;
    }

    /**
     * Get the sample statistics for this simulation.
     *
     * @return the sample statistics
     */
    public SampleStatistics getSampleStatistics() { return stats; }
}
