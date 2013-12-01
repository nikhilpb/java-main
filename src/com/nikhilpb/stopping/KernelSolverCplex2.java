package com.nikhilpb.stopping;

import com.nikhilpb.adp.Policy;
import com.nikhilpb.adp.Solver;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/29/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class KernelSolverCplex2 implements Solver {
    protected StoppingModel model;
    protected double gamma, kappa;
    protected GaussianStateKernel kernel;
    protected int timePeriods;
    protected GaussianKernelE gaussianKernelE;
    protected GaussianKernelDoubleE gaussianKernelDoubleE;
    protected double[][] qMat;
    protected int[] startInd;
    private int qSize, sampleCount;
    protected StoppingStateSampler sampler;
    private IloNumVar lambda0C, lambda0;
    private IloNumVar[][] lambdaC, lambda;
    private IloNumVar[] lambdaLast;
    private IloCplex cplex;

    public KernelSolverCplex2(StoppingModel model,
                             double kappa,
                             double gamma,
                             double bandWidth,
                             double peak,
                             int sampleCount,
                             long sampleSeed) {
        this.model = model;
        timePeriods = model.getTimePeriods();
        this.gamma = gamma;
        this.kappa = kappa;
        this.sampleCount = sampleCount;
        kernel = new GaussianStateKernel(bandWidth, peak);
        gaussianKernelE = new GaussianKernelE(model.getMeanArray(), model.getCovarMatrix(), bandWidth, peak);
        gaussianKernelDoubleE = new GaussianKernelDoubleE(model.getCovarMatrix(), bandWidth, peak);
        sampler = new StoppingStateSampler(model);
        sampler.sample(sampleCount, sampleSeed);
        startInd = new int[timePeriods];
        for (int t = 1; t < timePeriods; ++t) {
            startInd[t] = 2 * sampleCount * (t - 1) + 1;
        }
        qSize = (2 * timePeriods - 3) * sampleCount + 1;
        qMat = new double[qSize][qSize];
        qMat[0][0] = gaussianKernelDoubleE.eval((StoppingState)model.getBaseState(),
                                                (StoppingState)model.getBaseState());
        for (int t = 1; t < timePeriods; ++t) {
            ArrayList<StoppingState> curStates = sampler.getStates(t);
            int startI = startInd[t];
            for (int i = 0; i < curStates.size(); ++i) {
                for (int j = 0; j < curStates.size(); ++j) {
                    qMat[startI + i][startI + j] = kernel.value(curStates.get(i), curStates.get(j));
                }
            }
        }

        for (int t = 1; t < timePeriods - 1; ++t) {
            ArrayList<StoppingState> curStates = sampler.getStates(t);
            int startI = startInd[t] + sampleCount;
            for (int i = 0; i < curStates.size(); ++i) {
                for (int j = 0; j < curStates.size(); ++j) {
                    qMat[startI + i][startI + j] = gaussianKernelDoubleE.eval(curStates.get(i), curStates.get(j));
                }
            }
        }

        for (int t = 0; t < timePeriods - 1; ++t) {
            if (t == 0) {
                StoppingState curState = (StoppingState)model.getBaseState();
                ArrayList<StoppingState> nextStates = sampler.getStates(1);
                for (int i = 0; i < nextStates.size(); ++i) {
                    StoppingState nextState = nextStates.get(i);
                    qMat[0][i + 1] = qMat[i + 1][0] = gaussianKernelE.eval(curState, nextState);
                }
            } else {
                ArrayList<StoppingState> curStates = sampler.getStates(t);
                ArrayList<StoppingState> nextStates = sampler.getStates(t+1);
                for (int i = 0; i < curStates.size(); ++i) {
                    for (int j = 0; j < nextStates.size(); ++j) {
                        StoppingState curState = curStates.get(i);
                        StoppingState nextState = nextStates.get(j);
                        qMat[startInd[t] + sampleCount + i][startInd[t+1] + j] =
                               qMat[startInd[t+1] + j][startInd[t] + sampleCount + i] =
                                       gaussianKernelE.eval(curState, nextState);
                    }
                }
            }
        }
        printQ();

    }

    private void printQ() {
        for (int i = 0; i < qSize; ++i) {
            for (int j = 0; j < qSize; ++j) {
                System.out.printf("%.3f ", qMat[i][j]);
            }
            System.out.println();
        }
    }

    @Override
    public boolean solve() throws Exception {
        return false;
    }

    @Override
    public Policy getPolicy() {
        return null;
    }
}
