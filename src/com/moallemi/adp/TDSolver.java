package com.moallemi.adp;

import java.io.*;
import java.util.*;

import com.moallemi.math.stats.MVSampleStatistics;

/**
 * Learn by the TD(\lambda) algorithm.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.23 $, $Date: 2006-10-23 07:15:00 $
 */
public class TDSolver implements Solver {
    private Random random;
    private Model model;
    private StateFunction costFunction;
    private BasisSet basis;
    private double[] weights;
    private double alpha, lambda, gammaA, gammaB;
    private int timeStepCount, timeReportCount;
    // debugging output
    private PrintStream debugOut;
    private int debugIter;
    private ValueFunctionPolicyFactory policyFactory;

    public TDSolver(Model model,
                    StateFunction costFunction,
                    ValueFunctionPolicyFactory policyFactory,
                    BasisSet basis,
                    Random random,
                    double gammaA,
                    double gammaB,
                    double lambda,
                    int timeStepCount,
                    int timeReportCount,
                    double alpha)
    {
        this.model = model;
        this.costFunction = costFunction;
        this.basis = basis;
        this.random = random;
        this.lambda = lambda;
        this.alpha = alpha;
        this.gammaA = gammaA;
        this.gammaB = gammaB;
        this.timeStepCount = timeStepCount;
        this.timeReportCount = timeReportCount;
        this.policyFactory = policyFactory;

        weights = new double [basis.size()];
    }

    private static final int CACHE_SIZE = 100000;

    public boolean solve() {
        Arrays.fill(weights, 0.0);

        StateCache stateCache = new StateCache(model, CACHE_SIZE);

        int basisSize = basis.size();
        double[] z = new double [basisSize];
        LinearCombinationFunction vFunction = 
            new LinearCombinationFunction(weights,
                                          basis);

        State currentState = model.getBaseState();
        State nextState = null;

        Policy policy = policyFactory.getPolicy(vFunction);
        MVSampleStatistics stats = new MVSampleStatistics();
        MVSampleStatistics allStats = new MVSampleStatistics();

        int selfCount = 0;
        int lastAction = 0;
        double lastCost = 0.0;
        double[] psi = new double [basisSize];

        for (int time = 0; time < timeStepCount; time++) {
            // update statistics
            double currentCost = selfCount > 0
                ? lastCost
                : costFunction.getValue(currentState);
            stats.addSample(currentCost);
            allStats.addSample(currentCost);

            // compute the next action
            StateInfo info = stateCache.getStateInfo(currentState);
            int a = selfCount > 0
                ? lastAction
                : policy.getAction(currentState, info);

            // sample the next state
            StateDistribution dist = info.getDistribution(a);
            nextState = dist.nextSample(random);


            // keep track of self-transitions, and don't bother updating
            // weights until transition to a new state
            if (nextState == currentState) {
                selfCount++;

            }
            else {
                // compute temporal difference
                double d = currentCost 
                    + alpha * dist.expectedValue(vFunction, currentState)
                    - vFunction.getValue(currentState);
                basis.evaluate(currentState, psi);

                // update eligibility vector and weights
                double x = gammaA * d;
                if (gammaB > 0.0)
                    x *= gammaB / (gammaB + ((double) time));

                for (int i = 0; i < basisSize; i++) {
                    for (int s = 0; s <= selfCount; s++) {
                        z[i] *= alpha * lambda;
                        z[i] += psi[i];
                        weights[i] += x * z[i];
                    }
                    
                    // don't adjust the weights outside the prescribed range
                    if (weights[i] < basis.getMinValue(i)) 
                        weights[i] = basis.getMinValue(i);
                    else if (weights[i] > basis.getMaxValue(i)) 
                        weights[i] = basis.getMaxValue(i);
                }

                currentState = nextState;
                selfCount = 0;
            }

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
                    debugOut.print("," + weights[i]);
                debugOut.println();
            }

            lastAction = a;
            lastCost = currentCost;
        }

        return true;
    }

    public void setDebug(PrintStream debugOut, int debugIter) {
        this.debugIter = debugIter;
        this.debugOut = debugIter > 0 ? debugOut : null;
    }
            
    public StateFunction getValueEstimate() {
        return new LinearCombinationFunction(weights, basis);
    }

    public void dumpInfo(PrintStream out) {
        int basisSize = basis.size();
        for (int i = 0; i < basisSize; i++)
            out.println("coeff " 
                        + basis.getFunctionName(i)
                        + " = " 
                        + weights[i]);

    }   

    public void dumpStateInfo(PrintStream out) {}

    public void exportModel(String fileName) {}

}
