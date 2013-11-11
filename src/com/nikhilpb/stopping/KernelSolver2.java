package com.nikhilpb.stopping;

import com.nikhilpb.adp.Policy;
import com.nikhilpb.adp.Solver;
import com.nikhilpb.util.math.PSDMatrix;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/11/13
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class KernelSolver2 implements Solver {
    protected StoppingModel model;
    protected double gamma, kappa;
    protected GaussianStateKernel kernel;
    protected int timePeriods;
    protected MeanGaussianKernel oneExp, twoExp;
    protected double[][] qMat;
    protected int[] startInd;
    private int qSize, sampleCount;
    protected StoppingStateSampler sampler;

    public KernelSolver2(StoppingModel model,
                         double kappa,
                         double gamma,
                         double bandWidth,
                         int sampleCount,
                         long sampleSeed) {

        this.model = model;
        timePeriods = model.getTimePeriods();
        this.gamma = gamma;
        this.kappa = kappa;
        this.sampleCount = sampleCount;
        kernel = new GaussianStateKernel(bandWidth);
        oneExp = new MeanGaussianKernel(model.getCovarMatrix(), bandWidth);
        twoExp = new MeanGaussianKernel(PSDMatrix.times(model.getCovarMatrix(), 2.), bandWidth);
        sampler = new StoppingStateSampler(model);
        sampler.sample(sampleCount, sampleSeed);
        startInd = new int[timePeriods];
        for (int t = 1; t < timePeriods; ++t) {
            startInd[t] = 2 * sampleCount * (t - 1) + 1;
        }
        qSize = (2 * timePeriods - 3) * sampleCount + 1;
        System.out.println("size of the Q matrix: " + qSize);
        qMat = new double[qSize][qSize];
        qMat[0][0] = kernel.value(model.getBaseState(), model.getBaseState());
        for (int t = 1; t < timePeriods - 1; ++t) {
            ArrayList<StoppingState> curStates = sampler.getStates(t);
            for (int i = startInd[t]; i < startInd[t+1]; ++i) {
                for (int j = startInd[t]; j < startInd[t+1]; ++j) {
                    int stateInd1 = stateIndForInd(i), stateInd2 = stateIndForInd(j);
                    qMat[i][j] = kernel.value(curStates.get(stateInd1),
                                              curStates.get(stateInd2));
                }
            }
        }
        ArrayList<StoppingState> lastStates = sampler.getStates(timePeriods - 1);
        for (int i = startInd[timePeriods-1]; i < startInd[timePeriods-1] + sampleCount; ++i) {
            for (int j = startInd[timePeriods-1]; j < startInd[timePeriods-1] + sampleCount; ++j) {
                int stateInd1 = stateIndForInd(i), stateInd2 = stateIndForInd(j);
                qMat[i][j] = kernel.value(lastStates.get(stateInd1),
                        lastStates.get(stateInd2));
            }
        }
        for (int t = 0; t < timePeriods - 1; ++t) {
            ArrayList<StoppingState> curStates = sampler.getStates(t), nextStates = sampler.getStates(t+1);
            for (int i = 0; i < curStates.size(); ++i) {
                GaussianTransition gti = (GaussianTransition)
                        model.getDistribution(curStates.get(i), StoppingAction.CONTINUE);
                final double[] meani = gti.getMean();
                for (int j = 0; j < curStates.size(); j++) {
                    int indi = indOfVar(t, i, StoppingAction.CONTINUE);
                    int indj = indOfVar(t, j, StoppingAction.CONTINUE);
                    GaussianTransition gtj = (GaussianTransition)
                            model.getDistribution(curStates.get(j), StoppingAction.CONTINUE);
                    double[] meanj = gtj.getMean();
                    double[] diff = new double[meani.length];
                    for (int k = 0; k < meani.length; ++k) {
                        diff[k] = meani[k] - meanj[k];
                    }
                    qMat[indi][indj] += twoExp.eval(diff);
                }
                for (int j = 0; j < nextStates.size(); ++j) {
                    int indi = indOfVar(t, i, StoppingAction.CONTINUE);
                    int indjs = indOfVar(t+1, j, StoppingAction.STOP);
                    double[] diff = new double[meani.length];
                    for (int k = 0; k < meani.length; ++k) {
                        diff[k] = meani[k] - nextStates.get(j).vector[k];
                    }
                    qMat[indi][indjs] = qMat[indjs][indi] = oneExp.eval(diff);
                    if (t < timePeriods - 2) {
                        int indjc = indOfVar(t+1, j, StoppingAction.CONTINUE);
                        qMat[indi][indjc] = qMat[indjc][indi] = qMat[indi][indjs];
                    }
                }
            }

        }
        PSDMatrix qPsd = new PSDMatrix(qMat);
    }

    @Override
    public boolean solve() throws Exception {
        return false;
    }

    @Override
    public Policy getPolicy() {
        return null;
    }

    private int timePeriodForInd(int i) {
        if (i == 0)
            return 0;
        return (i-1) / (2*sampleCount) + 1;
    }

    private StoppingAction actionForInd(int i) {
        if (i == 0)
            return StoppingAction.CONTINUE;
        return (((i-1) / sampleCount) % 2 == 0) ?
                StoppingAction.STOP : StoppingAction.CONTINUE;
    }

    private int stateIndForInd(int i) {
        if (timePeriodForInd(i) == 0)
            return 0;
        if (actionForInd(i) == StoppingAction.STOP) {
            return i - startInd[timePeriodForInd(i)];
        }
        else {
            return i - startInd[timePeriodForInd(i)] - sampleCount;
        }
    }

    private int indOfVar(int tp, int ind, StoppingAction action) {
        if (tp == 0) {
            return 0;
        }
        if (action == StoppingAction.STOP) {
            return startInd[tp] + ind;
        } else {
            return startInd[tp] + sampleCount + ind;
        }
    }

    private void printQ() {
        for (int i = 0; i < qSize; ++i) {
            for (int j = 0; j < qSize; ++j) {
                System.out.printf("%.3f ", qMat[i][j]);
            }
            System.out.println();
        }
    }
}
