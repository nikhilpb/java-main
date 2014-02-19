package com.nikhilpb.stopping;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import com.nikhilpb.adp.*;
import com.nikhilpb.util.math.PSDMatrix;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 12/3/13
 * Time: 6:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class KernelSolverCplex3 implements Solver {
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
    private IloNumVar[] bVar;
    private IloNumVar[][] sVar;
    private IloCplex cplex;
    ArrayList<StateFunction> contValues;

    public KernelSolverCplex3(StoppingModel model,
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
        sVar = new IloNumVar[timePeriods][];
        double[] lb = new double[sampleCount];
        double[] lbs = new double[sampleCount];
        double[] ub = new double[sampleCount];
        Arrays.fill(lb, -Double.MAX_VALUE);
        Arrays.fill(lbs, 0.);
        Arrays.fill(ub, Double.MAX_VALUE);
        for (int t = 0; t < timePeriods; ++t) {
            if (t == 0) {
                lambdaVar[0] = new IloNumVar[1];
                lambdaCVar[0] = new IloNumVar[1];
                sVar[0] = new IloNumVar[1];
                lambdaVar[0][0] = cplex.numVar(-Double.MAX_VALUE, Double.MAX_VALUE);
                lambdaCVar[0][0] = cplex.numVar(-Double.MAX_VALUE, Double.MAX_VALUE);
                sVar[0][0] = cplex.numVar(0., Double.MAX_VALUE);
                continue;
            }
            lambdaVar[t] = cplex.numVarArray(sampleCount, lb, ub);
            sVar[t] = cplex.numVarArray(sampleCount, lbs, ub);
            if (t < timePeriods - 1) {
                lambdaCVar[t] = cplex.numVarArray(sampleCount, lb, ub);
            }
        }

        lb = new double[timePeriods];
        ub = new double[timePeriods];
        Arrays.fill(lb, -Double.MAX_VALUE);
        Arrays.fill(ub, Double.MAX_VALUE);
        bVar = cplex.numVarArray(timePeriods, lb, ub);

        // add objective term
        List<IloNumExpr> objTerms = new ArrayList<IloNumExpr>();
        startInd = new int[timePeriods];
        for (int t = 1; t < timePeriods; ++t) {
            startInd[t] = 2 * sampleCount * (t - 1) + 1;
        }
        qSize = (2 * timePeriods - 3) * sampleCount + 1;
        qMat = new double[qSize][qSize];
        qMat[0][0] = gaussianKernelDoubleE.eval((StoppingState)model.getBaseState(),
                (StoppingState)model.getBaseState());

        // quadratic terms involving terms in the same time-period
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

        // quadratic terms involving variables in different time periods
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
//        Matrix qMatrix = new Matrix(qMat);
//        EigenvalueDecomposition eig = qMatrix.eig();
//        Matrix dMat = eig.getD();
//        for (int i = 0; i < qSize; ++i) {
//            System.out.println(dMat.get(i, i));
//        }

        double[][] psdQMat = PSDMatrix.makePSD(qMat);
        objTerms.add(cplex.prod(lambdaCVar[0][0], lambdaCVar[0][0], this.gamma * psdQMat[0][0]));

        for (int t = 1; t < timePeriods; ++t) {
            int startI = startInd[t];
            for (int i = 0; i < sampleCount; ++i) {
                for (int j = 0; j < sampleCount; ++j) {
                        objTerms.add(cplex.prod(lambdaVar[t][i], lambdaVar[t][j],
                                this.gamma * psdQMat[startI + i][startI + j]));
                }
            }
        }

        for (int t = 1; t < timePeriods - 1; ++t) {
            int startI = startInd[t] + sampleCount;
            for (int i = 0; i < sampleCount; ++i) {
                for (int j = 0; j < sampleCount; ++j) {
                    objTerms.add(cplex.prod(lambdaCVar[t][i], lambdaCVar[t][j],
                                this.gamma * psdQMat[startI + i][startI + j]));
                }
            }
        }

        for (int t = 0; t < timePeriods - 1; ++t) {
            if (t == 0) {
                for (int i = 0; i < sampleCount; ++i) {
                        objTerms.add(cplex.prod(lambdaCVar[0][0], lambdaVar[1][i],
                                2. * this.gamma * psdQMat[0][i + 1] ));
                }
            } else {
                for (int i = 0; i < sampleCount; ++i) {
                    for (int j = 0; j < sampleCount; ++j) {
                        objTerms.add(cplex.prod(lambdaCVar[t][i], lambdaVar[t+1][j],
                                2. * this.gamma * psdQMat[startInd[t] + sampleCount + i][startInd[t+1] + j]));
                    }
                }
            }
        }


        // adding the b objective term
        objTerms.add(cplex.prod(bVar[0], 1.));

        // adding the s objective terms
        objTerms.add(cplex.prod(sVar[0][0], kappa));
        for (int t = 1; t < timePeriods; ++t) {
            for (int s = 0; s < sampleCount; ++s) {
                objTerms.add(cplex.prod(sVar[t][s], kappa / sampleCount));
            }
        }
        IloNumExpr obj = cplex.sum(objTerms.toArray(new IloNumExpr[objTerms.size()]));
        cplex.addMinimize(obj);

        // adding the constraints
        IloLinearNumExpr lhs = cplex.linearNumExpr();
        lhs.addTerm(1., sVar[0][0]);
        lhs.addTerm(1., bVar[0]);
        StoppingState baseState = (StoppingState)model.getBaseState();
        RewardFunction rf = model.getRewardFunction();
        cplex.addGe(lhs, rf.value(baseState, StoppingAction.STOP));
        IloLinearNumExpr rhs = cplex.linearNumExpr();
        rhs.addTerm(1., bVar[1]);
        rhs.addTerm(lambdaCVar[0][0], gaussianKernelDoubleE.eval(baseState, baseState));
        for (int s = 0; s < sampleCount; ++s) {
            StoppingState nextState = sampler.getStates(1).get(s);
            rhs.addTerm(lambdaVar[1][s], gaussianKernelE.eval(baseState, nextState));
        }
        cplex.addGe(lhs, rhs);

        for (int t = 1; t < timePeriods; ++t) {
            for (int s = 0; s < sampleCount; ++s) {
                lhs = cplex.linearNumExpr();
                lhs.addTerm(1., sVar[t][s]);
                lhs.addTerm(1., bVar[t]);
                StoppingState curState = sampler.getStates(t).get(s);
                for (int ss = 0; ss < sampleCount; ++ss) {
                    StoppingState otherState = sampler.getStates(t).get(ss);
                    lhs.addTerm(lambdaVar[t][ss], kernel.value(curState, otherState));
                }
                for (int ss = 0; ss < sampler.getStates(t-1).size(); ++ss) {
                    StoppingState prevState = sampler.getStates(t-1).get(ss);
                    lhs.addTerm(lambdaCVar[t-1][ss], gaussianKernelE.eval(prevState, curState));
                }
                cplex.addGe(lhs, rf.value(curState, StoppingAction.STOP));
                if (t < timePeriods - 1) {
                    rhs = cplex.linearNumExpr();
                    rhs.addTerm(bVar[t+1], 1.);
                    for (int ss = 0; ss < sampler.getStates(t).size(); ++ss) {
                        StoppingState otherState = sampler.getStates(t).get(ss);
                        rhs.addTerm(lambdaCVar[t][ss], gaussianKernelDoubleE.eval(curState, otherState));
                    }
                    for (int ss = 0; ss < sampler.getStates(t+1).size(); ++ss) {
                        StoppingState nextState = sampler.getStates(t+1).get(ss);
                        rhs.addTerm(lambdaVar[t+1][ss], gaussianKernelE.eval(curState, nextState));
                    }
                    cplex.addGe(lhs, rhs);
                }
            }
        }


    }

    @Override
    public boolean solve() throws Exception {
        boolean solved = cplex.solve();
        if (!solved) {
            return solved;
        }
        double[][] alpha = new double[timePeriods][];
        double[][] alphaC = new double[timePeriods-1][];
        for (int t = 0; t < timePeriods; ++t) {
            if (t != 0) {
                alpha[t] = cplex.getValues(lambdaVar[t]);
            }
            if (t < timePeriods - 1) {
                alphaC[t] = cplex.getValues(lambdaCVar[t]);
            }
        }
        double[] b = cplex.getValues(bVar);
        contValues = new ArrayList<StateFunction>();
        for (int t = 0; t < timePeriods; ++t) {
            if (t == timePeriods-1) {
                contValues.add(new ConstantStateFunction(-Double.MAX_VALUE));
                continue;
            }
            contValues.add(new KernelContFunction2(
                    sampler.getStates(t),
                    sampler.getStates(t+1),
                    alphaC[t],
                    alpha[t+1],
                    gaussianKernelE,
                    gaussianKernelDoubleE,
                    model,
                    b[t]));
        }
        return solved;
    }

    @Override
    public Policy getPolicy() {
        QFunction qFunction = new TimeDepQFunction(contValues);
        return new QFunctionPolicy(model, qFunction, model.getRewardFunction(), 1.);
    }
}
