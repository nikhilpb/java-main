package com.moallemi.adp;

import java.io.*;
import java.util.*;

import com.moallemi.math.stats.MVSampleStatistics;

/**
 * Learn by the TD(\lambda) Kalman Filter algorithm.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.11 $, $Date: 2006-10-23 07:15:00 $
 */
public class KalmanFilterSolver implements Solver {
    private Random random;
    private Model model;
    private StateFunction costFunction;
    private BasisSet basis;
    private double[] weights;
    private double alpha, gammaA, gammaB;
    private int timeStepCount, timeReportCount;
    // debugging output
    private PrintStream debugOut;
    private int debugIter;
    private ValueFunctionPolicyFactory policyFactory;

    public KalmanFilterSolver(Model model,
                              StateFunction costFunction,
                              ValueFunctionPolicyFactory policyFactory,
                              BasisSet basis,
                              Random random,
                              double gammaA,
                              double gammaB,
                              int timeStepCount,
                              int timeReportCount,
                              double alpha)
    {
        this.model = model;
        this.costFunction = costFunction;
        this.basis = basis;
        this.random = random;
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
        LinearCombinationFunction vFunction = 
            new LinearCombinationFunction(weights,
                                          basis);

        State currentState = model.getBaseState();
        State nextState = null;

        Policy policy = policyFactory.getPolicy(vFunction);
        MVSampleStatistics stats = new MVSampleStatistics();
        MVSampleStatistics allStats = new MVSampleStatistics();

        int lastAction = 0;
        int selfCount = 0;

        double[] psi = new double [basisSize];
        double[][] H = new double [basisSize][basisSize];
        for (int i = 0; i < basisSize; i++)
            H[i][i] = 1.0;
        double[] Hpsi = new double [basisSize];
        double[] Hpsi2 = new double [basisSize];

        for (int time = 0; time < timeStepCount; time++) {
            // update statistics
            double currentCost = costFunction.getValue(currentState);
            stats.addSample(currentCost);
            allStats.addSample(currentCost);

            // compute the next action
            StateInfo info = stateCache.getStateInfo(currentState);
            // optimization: do not change action on self-transitions
            int a = selfCount > 0
                ? lastAction
                : policy.getAction(currentState, info);

            // sample the next state
            StateDistribution dist = info.getDistribution(a);
            nextState = dist.nextSample(random);

            // keep track of self-transitions
            if (nextState == currentState)
                selfCount++;
            else
                selfCount = 0;

            // compute temporal difference
            double d = currentCost 
                + alpha * dist.expectedValue(vFunction, currentState)
                - vFunction.getValue(currentState);
            
            // update Kalman filter weights
            // compute psi_t
            basis.evaluate(currentState, psi);
            // compute H_{t-1} * psi_t
            for (int i = 0; i < basisSize; i++) {
                Hpsi[i] = 0.0;
                for (int j = 0; j < basisSize; j++) 
                    Hpsi[i] += H[i][j] * psi[j];
            }
            // compute psi_t'*H_{t-1}*psi_t
            double psiHpsi = 0.0;
            for (int i = 0; i < basisSize; i++)
                psiHpsi += psi[i] * Hpsi[i];
            // update H_t
            double t = time + 2;
            double x1 = t / (t - 1.0);
            double x2 = x1 / (t - 1.0 + psiHpsi);
            for (int i = 0; i < basisSize; i++) {
                for (int j = 0; j < basisSize; j++) {
                    H[i][j] = x1 * H[i][j] - x2 * Hpsi[i] * Hpsi[j];
                }
            }
            // compute H_t*psi_t
            for (int i = 0; i < basisSize; i++) {
                Hpsi2[i] = 0.0;
                for (int j = 0; j < basisSize; j++) 
                    Hpsi2[i] += H[i][j] * psi[j];
            }


            // update weights
            double x3 = d *  gammaA;
            if (gammaB > 0.0)
                x3 *= gammaB / (t + gammaB) ;
            for (int i = 0; i < basisSize; i++) {
                weights[i] += x3 * Hpsi2[i];
            
                // don't adjust the weights outside the prescribed range
                if (weights[i] < basis.getMinValue(i)) 
                    weights[i] = basis.getMinValue(i);
                else if (weights[i] > basis.getMaxValue(i)) 
                    weights[i] = basis.getMaxValue(i);
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
            currentState = nextState;
        }

        return true;
    }

    public StateFunction getValueEstimate() {
        return new LinearCombinationFunction(weights, basis);
    }

    public void setDebug(PrintStream debugOut, int debugIter) {
        this.debugIter = debugIter;
        this.debugOut = debugIter > 0 ? debugOut : null;
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
