package com.nikhilpb.stopping;

import com.nikhilpb.adp.*;
import com.nikhilpb.util.math.PSDMatrix;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/11/13
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class KernelSolverCplex implements Solver {
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
  private IloNumVar lambda0C, lambda0S;
  private IloNumVar[][] lambdaC, lambdaS;
  private IloNumVar[] lambdaSLast;
  private IloCplex cplex;
  private IloRange[] bConsts;
  private static final double kTol = 1E-4;
  private double[] b;
  ArrayList<StateFunction> contValues, valueFuns;

  public KernelSolverCplex(StoppingModel model,
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
    startInd = new int[timePeriods];
    for (int t = 1; t < timePeriods; ++ t) {
      startInd[t] = 2 * sampleCount * (t - 1) + 1;
    }
    qSize = (2 * timePeriods - 3) * sampleCount + 1;
    System.out.println("size of the Q matrix: " + qSize);
    qMat = new double[qSize][qSize];
    qMat[0][0] = kernel.value(model.getBaseState(), model.getBaseState());
    for (int t = 1; t < timePeriods - 1; ++ t) {
      ArrayList<StoppingState> curStates = sampler.getStates(t);
      for (int i = startInd[t]; i < startInd[t + 1]; ++ i) {
        for (int j = startInd[t]; j < startInd[t + 1]; ++ j) {
          int stateInd1 = stateIndForInd(i), stateInd2 = stateIndForInd(j);
          qMat[i][j] = kernel.value(curStates.get(stateInd1),
                                           curStates.get(stateInd2));
        }
      }
    }
    ArrayList<StoppingState> lastStates = sampler.getStates(timePeriods - 1);
    for (int i = startInd[timePeriods - 1]; i < startInd[timePeriods - 1] + sampleCount; ++ i) {
      for (int j = startInd[timePeriods - 1]; j < startInd[timePeriods - 1] + sampleCount; ++ j) {
        int stateInd1 = stateIndForInd(i), stateInd2 = stateIndForInd(j);
        qMat[i][j] = kernel.value(lastStates.get(stateInd1),
                                         lastStates.get(stateInd2));
      }
    }
    for (int t = 0; t < timePeriods - 1; ++ t) {
      ArrayList<StoppingState> curStates = sampler.getStates(t), nextStates = sampler.getStates(t + 1);
      for (int i = 0; i < curStates.size(); ++ i) {
        for (int j = 0; j < curStates.size(); j++) {
          int indi = indOfVar(t, i, StoppingAction.CONTINUE);
          int indj = indOfVar(t, j, StoppingAction.CONTINUE);
          qMat[indi][indj] += gaussianKernelDoubleE.eval(curStates.get(i), curStates.get(j));
        }
        for (int j = 0; j < nextStates.size(); ++ j) {
          int indi = indOfVar(t, i, StoppingAction.CONTINUE);
          int indjs = indOfVar(t + 1, j, StoppingAction.STOP);
          qMat[indi][indjs] = qMat[indjs][indi] = - gaussianKernelE.eval(curStates.get(i),
                                                                                nextStates.get(j));
          if (t < timePeriods - 2) {
            int indjc = indOfVar(t + 1, j, StoppingAction.CONTINUE);
            qMat[indi][indjc] = qMat[indjc][indi] = qMat[indi][indjs];
          }
        }
      }
    }

    cplex = new IloCplex();
    lambda0C = cplex.numVar(0., 1.);
    lambda0S = cplex.numVar(0., 1.);
    double[] ub = new double[sampleCount],
            lb = new double[sampleCount],
            ones = new double[sampleCount],
            negOnes = new double[sampleCount];
    Arrays.fill(ub, 1.);
    Arrays.fill(lb, 0.);
    Arrays.fill(ones, 1.);
    Arrays.fill(negOnes, - 1.);
    lambdaSLast = cplex.numVarArray(sampleCount, lb, ub);
    lambdaS = new IloNumVar[timePeriods - 2][];
    lambdaC = new IloNumVar[timePeriods - 2][];
    for (int t = 1; t < timePeriods - 1; ++ t) {
      lambdaS[t - 1] = cplex.numVarArray(sampleCount, lb, ub);
      lambdaC[t - 1] = cplex.numVarArray(sampleCount, lb, ub);
    }
    IloLinearNumExpr zeroMass = cplex.linearNumExpr();
    zeroMass.addTerm(1.0, lambda0C);
    zeroMass.addTerm(1.0, lambda0S);
    bConsts = new IloRange[timePeriods];
    bConsts[0] = cplex.addEq(1.0, zeroMass);
    for (int t = 1; t < timePeriods; ++ t) {
      IloLinearNumExpr massBalance = cplex.linearNumExpr();
      if (t == 1) {
        massBalance.addTerm(lambda0C, - 1.0);
      } else {
        massBalance.addTerms(negOnes, lambdaC[t - 2]);
      }
      if (t == timePeriods - 1) {
        massBalance.addTerms(ones, lambdaSLast);
      } else {
        massBalance.addTerms(ones, lambdaC[t - 1]);
        massBalance.addTerms(ones, lambdaS[t - 1]);
      }
      bConsts[t] = cplex.addEq(0., massBalance);
      for (int i = 0; i < sampleCount; ++ i) {
        IloLinearNumExpr stateMass = cplex.linearNumExpr();
        if (t < timePeriods - 1) {
          stateMass.addTerm(1.0, lambdaS[t - 1][i]);
          stateMass.addTerm(1.0, lambdaC[t - 1][i]);
        } else {
          stateMass.addTerm(1.0, lambdaSLast[i]);
        }
        cplex.addLe(stateMass, kappa / sampleCount);
      }
    }
    List<IloNumExpr> objTerms = new ArrayList<IloNumExpr>();
    for (int i = 0; i < qSize; ++ i) {
      for (int j = 0; j < qSize; ++ j) {
        if (qMat[i][j] > kTol) {
          objTerms.add(cplex.prod(getVarFromInd(i), getVarFromInd(j), qMat[i][j]));
        }
      }
      if (actionForInd(i) == StoppingAction.STOP) {
        int t = timePeriodForInd(i);
        int ind = stateIndForInd(i);
        IloNumExpr o = cplex.prod(model.getRewardFunction().value(sampler.getStates(t).get(ind),
                                                                         StoppingAction.STOP) * - 2.0 * gamma,
                                         getVarFromInd(i));
        objTerms.add(o);
      }
    }
    IloNumExpr obj = cplex.sum(objTerms.toArray(new IloNumExpr[objTerms.size()]));
    cplex.addMinimize(obj);
    b = new double[timePeriods];
  }

  @Override
  public boolean solve() throws Exception {
    boolean solved = cplex.solve();
    if (! solved) {
      return solved;
    }
    double l0C = cplex.getValue(lambda0C);
    double[][] lS = new double[timePeriods - 2][], lC = new double[timePeriods - 2][];
    for (int t = 1; t < timePeriods - 1; ++ t) {
      lS[t - 1] = cplex.getValues(lambdaS[t - 1]);
      lC[t - 1] = cplex.getValues(lambdaC[t - 1]);
    }
    double[] lSLast = cplex.getValues(lambdaSLast);

    contValues = new ArrayList<StateFunction>();
    for (int t = 0; t < timePeriods; ++ t) {
      if (t == timePeriods - 1) {
        contValues.add(new ConstantStateFunction(- Double.MAX_VALUE));
        continue;
      }
      double[] lmdCur;
      if (t == 0) {
        lmdCur = new double[1];
        lmdCur[0] = l0C;
      } else {
        lmdCur = lC[t - 1];
      }
      double[] lmdNext = new double[sampleCount];
      if (t < timePeriods - 2) {
        for (int i = 0; i < sampleCount; ++ i) {
          lmdNext[i] = lC[t][i] + lS[t][i];
        }
      } else {
        for (int i = 0; i < sampleCount; ++ i) {
          lmdNext[i] = lSLast[i];
        }
      }
      contValues.add(new KernelContFunction(
                                                   sampler.getStates(t),
                                                   sampler.getStates(t + 1),
                                                   lmdCur,
                                                   lmdNext,
                                                   gaussianKernelE,
                                                   gaussianKernelDoubleE,
                                                   model,
                                                   gamma,
                                                   0.));
      for (int i = 0; i < 20; ++ i) {
        double price = 90.0 + i;
        double[] pVec = {Math.log(price)};
        StoppingState s = new StoppingState(pVec, t);
        System.out.println("t = " + t + ", p = " + price + ", v = " + contValues.get(t).value(s));
      }
    }

    valueFuns = new ArrayList<StateFunction>();
    for (int t = 0; t < timePeriods; ++ t) {
      if (t == 0) {
        valueFuns.add(new ConstantStateFunction(0.));
        continue;
      }
      double[] lmdPrev, lmdCur;
      if (t == 1) {
        lmdPrev = new double[1];
        lmdPrev[0] = l0C;
      } else {
        lmdPrev = new double[sampleCount];
        for (int i = 0; i < sampleCount; ++ i) {
          lmdPrev[i] = lC[t - 2][i];
        }
      }
      lmdCur = new double[sampleCount];
      if (t == timePeriods - 1) {
        for (int i = 0; i < sampleCount; ++ i) {
          lmdCur[i] = lSLast[i];
        }
      } else {
        for (int i = 0; i < sampleCount; ++ i) {
          lmdCur[i] = lC[t - 1][i] + lS[t - 1][i];
        }
      }
      valueFuns.add(new KernelStateFunction(
                                                   sampler.getStates(t - 1),
                                                   sampler.getStates(t),
                                                   lmdPrev,
                                                   lmdCur,
                                                   kernel,
                                                   gaussianKernelE,
                                                   model,
                                                   gamma));
    }

    b = findOffsets();
    for (int t = 0; t < timePeriods - 1; ++ t) {
      if (t == 0) {
        contValues.set(0, new ConstantStateFunction(Double.MAX_VALUE));
      } else {
        ((KernelContFunction) contValues.get(t)).setB(b[t + 1]);
      }
    }
    return solved;
  }

  @Override
  public Policy getPolicy() {
    QFunction qFunction = new TimeDepQFunction(contValues);
    return new QFunctionPolicy(model, qFunction, model.getRewardFunction(), 1.);
  }

  private int timePeriodForInd(int i) {
    if (i == 0)
      return 0;
    return (i - 1) / (2 * sampleCount) + 1;
  }

  private StoppingAction actionForInd(int i) {
    if (i == 0)
      return StoppingAction.CONTINUE;
    return (((i - 1) / sampleCount) % 2 == 0) ?
                   StoppingAction.STOP : StoppingAction.CONTINUE;
  }

  private int stateIndForInd(int i) {
    if (timePeriodForInd(i) == 0)
      return 0;
    if (actionForInd(i) == StoppingAction.STOP) {
      return i - startInd[timePeriodForInd(i)];
    } else {
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
    for (int i = 0; i < qSize; ++ i) {
      for (int j = 0; j < qSize; ++ j) {
        System.out.printf("%.3f ", qMat[i][j]);
      }
      System.out.println();
    }
  }

  private IloNumVar getVarFromInd(int i) {
    int t = timePeriodForInd(i);
    int ind = stateIndForInd(i);
    StoppingAction action = actionForInd(i);
    if (t == 0) {
      if (action == StoppingAction.STOP) {
        return lambda0S;
      } else {
        return lambda0C;
      }
    } else if (t == timePeriods - 1) {
      return lambdaSLast[ind];
    } else {
      if (action == StoppingAction.STOP) {
        return lambdaS[t - 1][ind];
      } else {
        return lambdaC[t - 1][ind];
      }
    }
  }

  private double arraySum(double[] arr) {
    double val = 0.;
    for (int i = 0; i < arr.length; ++ i) {
      val += arr[i];
    }
    return val;
  }

  private double[] findOffsets() throws IloException {
    IloCplex cplexLP = new IloCplex();
    IloNumVar[] bVar;
    IloNumVar[][] sVar;
    IloNumVar s0Var;
    double[] lb = new double[timePeriods], ub = new double[timePeriods], zeros = new double[timePeriods];
    Arrays.fill(lb, - Double.MAX_VALUE);
    Arrays.fill(ub, Double.MAX_VALUE);
    Arrays.fill(zeros, 0.);
    bVar = cplexLP.numVarArray(timePeriods, lb, ub);
    ub = new double[timePeriods - 1];
    lb = new double[timePeriods - 1];
    Arrays.fill(ub, Double.MAX_VALUE);
    Arrays.fill(lb, 0.);
    sVar = new IloNumVar[sampleCount][];
    for (int i = 0; i < sampleCount; ++ i) {
      sVar[i] = cplexLP.numVarArray(timePeriods - 1, lb, ub);
    }
    s0Var = cplexLP.numVar(0., Double.MAX_VALUE);
    IloLinearNumExpr obj = cplexLP.linearNumExpr();
    obj.addTerm(1.0, bVar[0]);
    obj.addTerm(kappa, s0Var);
    for (int i = 0; i < sampleCount; ++ i) {
      for (int t = 1; t < timePeriods; ++ t) {
        obj.addTerm(kappa / sampleCount, sVar[i][t - 1]);
      }
    }
    cplexLP.addMinimize(obj);
    IloLinearNumExpr constr = cplexLP.linearNumExpr();
    constr.addTerm(1.0, s0Var);
    constr.addTerm(1.0, bVar[0]);
    double rhs = model.getRewardFunction().value(model.getBaseState(), StoppingAction.STOP);
    cplexLP.addGe(constr, rhs);
    constr = cplexLP.linearNumExpr();
    constr.addTerm(1.0, s0Var);
    constr.addTerm(1.0, bVar[0]);
    constr.addTerm(- 1., bVar[1]);
    rhs = contValues.get(0).value(model.getBaseState());
    cplexLP.addGe(constr, rhs);
    for (int i = 0; i < sampleCount; ++ i) {
      for (int t = 1; t < timePeriods; ++ t) {
        StoppingState state = sampler.getStates(t).get(i);
        rhs = model.getRewardFunction().value(state, StoppingAction.STOP);
        rhs -= valueFuns.get(t).value(state);
        constr = cplexLP.linearNumExpr();
        constr.addTerm(1., sVar[i][t - 1]);
        constr.addTerm(1., bVar[t]);
        cplexLP.addGe(constr, rhs);
        if (t < timePeriods - 1) {
          rhs = contValues.get(t).value(state);
          rhs -= valueFuns.get(t).value(state);
          constr = cplexLP.linearNumExpr();
          constr.addTerm(1., sVar[i][t - 1]);
          constr.addTerm(1., bVar[t]);
          constr.addTerm(- 1., bVar[t + 1]);
          cplexLP.addGe(constr, rhs);
        }
      }
    }
    cplexLP.solve();
    return cplexLP.getValues(bVar);
  }
}
