package com.nikhilpb.stopping;

import com.nikhilpb.adp.Policy;
import com.nikhilpb.adp.Solver;
import com.nikhilpb.util.math.PSDMatrix;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private IloNumVar[][] lambdaCVar, lambdaVar;
    private IloCplex cplex;

    public KernelSolverCplex2(StoppingModel model,
                             double kappa,
                             double gamma,
                             double bandWidth,
                             double peak,
                             int sampleCount,
                             long sampleSeed) throws IloException {
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

        cplex = new IloCplex();

        // set-up of variables
        lambdaVar = new IloNumVar[timePeriods][];
        lambdaCVar = new IloNumVar[timePeriods - 1][];
        double[] lb = new double[sampleCount];
        double[] ub = new double[sampleCount];
        Arrays.fill(lb, 0.);
        Arrays.fill(ub, 1.);
        for (int t = 0; t < timePeriods; ++t) {
            if (t == 0) {
                lambdaVar[0] = new IloNumVar[1];
                lambdaCVar[0] = new IloNumVar[1];
                lambdaVar[0][0] = cplex.numVar(0., 1.);
                lambdaCVar[0][0] = cplex.numVar(0., 1.);
                continue;
            }
            lambdaVar[t] = cplex.numVarArray(sampleCount, lb, ub);
            if (t < timePeriods - 1) {
                lambdaCVar[t] = cplex.numVarArray(sampleCount, lb, ub);
            }
        }

        // adding constraints
        cplex.addEq(1.0, lambdaVar[0][0]);
        cplex.addLe(lambdaCVar[0][0], lambdaVar[0][0]);
        for (int t = 0; t < timePeriods - 1; ++t) {
            IloLinearNumExpr inMass = cplex.linearNumExpr(), outMass = cplex.linearNumExpr();
            for (int i = 0; i < lambdaCVar[t].length; ++i) {
                inMass.addTerm(1.0, lambdaCVar[t][i]);
            }
            for (int i = 0; i < lambdaVar[t+1].length; ++i) {
                outMass.addTerm(1.0, lambdaVar[t+1][i]);
            }
            cplex.addEq(inMass, outMass);
        }
        for (int t = 1; t < timePeriods - 1; ++t) {
            for (int i = 0; i < lambdaVar[t].length; ++i) {
                cplex.addLe(lambdaVar[t][i], kappa / sampleCount);
                cplex.addLe(lambdaCVar[t][i], lambdaVar[t][i]);
            }
        }

        // add objective term
        List<IloNumExpr> objTerms = new ArrayList<IloNumExpr>();
        for (int t = 0; t < timePeriods; ++t) {
            if (t == 0) {
                double reward = model.getRewardFunction().value(model.getBaseState(), StoppingAction.STOP);
                IloNumExpr o = cplex.prod(- reward * 2. * gamma, lambdaVar[0][0]);
                objTerms.add(o);
                o = cplex.prod(reward * 2. * gamma, lambdaCVar[0][0]);
                objTerms.add(o);
                continue;
            }
            for (int i = 0; i < sampler.getStates(t).size(); ++i) {
                StoppingState curState = sampler.getStates(t).get(i);
                double reward = model.getRewardFunction().value(curState, StoppingAction.STOP);
                IloNumExpr o = cplex.prod(- reward * 2. * gamma, lambdaVar[t][i]);
                objTerms.add(o);
                if (t < timePeriods - 1) {
                    o = cplex.prod(reward * 2. * gamma, lambdaCVar[t][i]);
                    objTerms.add(o);
                }
            }
        }

        startInd = new int[timePeriods];
        for (int t = 1; t < timePeriods; ++t) {
            startInd[t] = 2 * sampleCount * (t - 1) + 1;
        }
        qSize = (2 * timePeriods - 3) * sampleCount + 1;
        qMat = new double[qSize][qSize];
        qMat[0][0] = gaussianKernelDoubleE.eval((StoppingState)model.getBaseState(),
                                                (StoppingState)model.getBaseState());
        objTerms.add(cplex.prod(lambdaCVar[0][0], lambdaCVar[0][0], qMat[0][0]));
        for (int t = 1; t < timePeriods; ++t) {
            ArrayList<StoppingState> curStates = sampler.getStates(t);
            int startI = startInd[t];
            for (int i = 0; i < curStates.size(); ++i) {
                for (int j = 0; j < curStates.size(); ++j) {
                    qMat[startI + i][startI + j] = kernel.value(curStates.get(i), curStates.get(j));
                    objTerms.add(cplex.prod(lambdaVar[t][i], lambdaVar[t][j], qMat[startI + i][startI + j]));
                }
            }
        }

        for (int t = 1; t < timePeriods - 1; ++t) {
            ArrayList<StoppingState> curStates = sampler.getStates(t);
            int startI = startInd[t] + sampleCount;
            for (int i = 0; i < curStates.size(); ++i) {
                for (int j = 0; j < curStates.size(); ++j) {
                    qMat[startI + i][startI + j] = gaussianKernelDoubleE.eval(curStates.get(i), curStates.get(j));
                    objTerms.add(cplex.prod(lambdaCVar[t][i], lambdaCVar[t][j], qMat[startI + i][startI + j]));
                }
            }
        }

        for (int t = 0; t < timePeriods - 1; ++t) {
            if (t == 0) {
                StoppingState curState = (StoppingState)model.getBaseState();
                ArrayList<StoppingState> nextStates = sampler.getStates(1);
                for (int i = 0; i < nextStates.size(); ++i) {
                    StoppingState nextState = nextStates.get(i);
                    qMat[0][i + 1] = qMat[i + 1][0] = - gaussianKernelE.eval(curState, nextState);
                    objTerms.add(cplex.prod(lambdaCVar[0][0], lambdaVar[1][i], 2 * qMat[0][i + 1] ));
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
                                       - gaussianKernelE.eval(curState, nextState);
                        objTerms.add(cplex.prod(lambdaCVar[t][i], lambdaVar[t+1][j],
                                - 2. * gaussianKernelE.eval(curState, nextState)));
                    }
                }
            }
        }
        IloNumExpr obj = cplex.sum(objTerms.toArray(new IloNumExpr[objTerms.size()]));
        try {
            PSDMatrix psdMatrix = new PSDMatrix(qMat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cplex.addMinimize(obj);
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
        boolean solved = cplex.solve();
        if (!solved) {
            return solved;
        }
        return true;
    }

    @Override
    public Policy getPolicy() {
        return null;
    }
}
