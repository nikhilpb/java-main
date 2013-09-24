package com.moallemi.iqswitch;

import java.io.*;
import java.util.*;

import com.moallemi.math.*;
import com.moallemi.math.stats.MVSampleStatistics;

/**
 * Learn by the TD(\lambda) Kalman Filter algorithm.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2006-10-29 17:39:48 $
 */
public class KalmanFilterSolver implements PolicySolver {
    private Random random;
    private SwitchModel model;
    private BasisSet basis;
    private double[] r;
    private double alpha, gammaA, gammaB;
    private int timeStepCount, timeReportCount;
    private BipartiteMatcherFactory factory;
    // debugging output
    private PrintStream debugOut;
    private int debugIter;

    public KalmanFilterSolver(SwitchModel model,
                              BasisSet basis,
                              Random random,
                              double gammaA,
                              double gammaB,
                              int timeStepCount,
                              int timeReportCount,
                              double alpha,
                              BipartiteMatcherFactory factory)
    {
        this.model = model;
        this.basis = basis;
        this.random = random;
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

            // update Kalman filter weights
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
            for (int i = 0; i < basisSize; i++) 
                for (int j = 0; j < basisSize; j++) 
                    H[i][j] = x1 * H[i][j] - x2 * Hpsi[i] * Hpsi[j];
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
            for (int i = 0; i < basisSize; i++) 
                r[i] += x3 * Hpsi2[i];

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
