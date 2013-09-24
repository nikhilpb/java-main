package com.moallemi.iqswitch;

import java.io.*;
import java.util.*;

import com.moallemi.math.BipartiteMatcherFactory;
import com.moallemi.math.stats.MVSampleStatistics;

public class TDSolver implements PolicySolver {
    private Random random;
    private SwitchModel model;
    private BasisSet basis;
    private double[] r;
    private double alpha, lambda, gammaA, gammaB;
    private int timeStepCount, timeReportCount;
    private BipartiteMatcherFactory factory;
    // debugging output
    private PrintStream debugOut;
    private int debugIter;

    public TDSolver(SwitchModel model,
                    BasisSet basis,
                    Random random,
                    double gammaA,
                    double gammaB,
                    double lambda,
                    int timeStepCount,
                    int timeReportCount,
                    double alpha,
                    BipartiteMatcherFactory factory)
    {
        this.model = model;
        this.basis = basis;
        this.random = random;
        this.lambda = lambda;
        this.alpha = alpha;
        this.gammaA = gammaA;
        this.gammaB = gammaB;
        this.timeStepCount = timeStepCount;
        this.timeReportCount = timeReportCount;
        this.factory = factory;
        r = new double [basis.size()];
    }

    public boolean solve() {
        // better initial guess?
        Arrays.fill(r, 0.0);

        int basisSize = basis.size();
        double[] z = new double [basisSize];

        SwitchState currentState = model.getBaseState();
        Function costFunction = model.getCostFunction();

        SeparableFunction vFunction = 
            new LinearCombinationFunction(r,
                                          basis);
        MatchingPolicy policy = new MatchingPolicy(model,
                                                   vFunction,
                                                   factory);

        MVSampleStatistics stats = new MVSampleStatistics();
        MVSampleStatistics allStats = new MVSampleStatistics();

        double[] psi = new double [basisSize];
        for (int time = 0; time < timeStepCount; time++) {
            // update statistics
            double currentCost = costFunction.getValue(currentState);
            stats.addSample(currentCost);
            allStats.addSample(currentCost);

            // compute the next action and expected value
            policy.setState(currentState);
            SwitchAction action = policy.getAction();
            double expectedValue = policy.getValue();

            // compute the current value
            basis.evaluate(currentState, psi);
            double currentValue = 0.0;
            for (int i = 0; i < basisSize; i++)
                currentValue += r[i] * psi[i];

            // compute temporal difference
            double d = currentCost 
                + alpha * expectedValue
                - currentValue;

            // update eligibility vector and weights
            double scale = gammaA * d;
            if (gammaB > 0.0)
                scale *= gammaB / (gammaB + ((double) time));
            for (int i = 0; i < basisSize; i++) {
                z[i] *= alpha * lambda;
                z[i] += psi[i];
                r[i] += scale * z[i];
            }
            
            // sample the next state
            currentState = model.sampleNextState(currentState,
                                                 action,
                                                 random);

            if ((time + 1) % timeReportCount == 0) {
                System.out.println("TIME: "
                                   + (time+1)
                                   + " AVG COST: "
                                   + stats.getMean()
                                   + " "
                                   + allStats.getMean());
                stats.clear();
            }
            
            if (debugOut != null && (time + 1) % debugIter == 0) {
                debugOut.print((time+1));
                for (int i = 0; i < basisSize; i++)
                    debugOut.print("," + r[i]);
                debugOut.println();
            }

        }

        return true;
    }


    public SeparableFunction getPolicyFunction() {
        return new LinearCombinationFunction(r, basis);
    }
       
    public void dumpInfo(PrintStream out) {
        int basisSize = basis.size();
        for (int i = 0; i < basisSize; i++)
            out.println("coeff[" + (i+1) + "] "
                        + basis.getFunctionName(i)
                        + " = " 
                        + r[i]);

    }   

    public void setDebug(PrintStream debugOut, int debugIter) {
        this.debugIter = debugIter;
        this.debugOut = debugIter > 0 ? debugOut : null;
    }
}