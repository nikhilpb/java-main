package com.nikhilpb.stopping;

import com.nikhilpb.adp.Policy;
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
 * Date: 11/4/13
 * Time: 6:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class KernelSolverCplex extends KernelSolver {
    private IloNumVar[][] lambdaC, lambdaS;
    private IloCplex cplex;

    public KernelSolverCplex(StoppingModel model,
                             double kappa,
                             double gamma,
                             double bandWidth,
                             int sampleCount,
                             long sampleSeed) throws IloException {
        init(model, kappa, gamma, bandWidth, sampleCount, sampleSeed);
        cplex = new IloCplex();
        lambdaS = new IloNumVar[timePeriods][];
        lambdaC = new IloNumVar[timePeriods][];

        for (int t = 0; t < timePeriods; ++t) {
            int stateCount = sampleStates.get(t).size();
            double[] lb = new double[stateCount], ub = new double[stateCount], ones = new double[stateCount];
            Arrays.fill(lb, 0.);
            Arrays.fill(ub, 1.);
            Arrays.fill(ones, 1.);
            lambdaS[t] = cplex.numVarArray(stateCount, lb, ub);
            lambdaC[t] = cplex.numVarArray(stateCount, lb, ub);
            IloLinearNumExpr probMass = cplex.linearNumExpr();
            probMass.addTerms(lambdaS[t], ones);
            probMass.addTerms(lambdaC[t], ones);
            cplex.addEq(1., probMass);
            for (int i = 0; i < stateCount; ++i) {
                IloLinearNumExpr stateMass = cplex.linearNumExpr();
                stateMass.addTerm(1., lambdaS[t][i]);
                stateMass.addTerm(1., lambdaC[t][i]);
                cplex.addLe(this.kappa / ((double) stateCount), stateMass);
            }
        }

        List<IloNumExpr> objTerms = new ArrayList<IloNumExpr>();
        for (int t = 0; t < timePeriods; ++t) {
            ArrayList<StoppingState> states = sampleStates.get(t);
            if (t == 0) {
                for (int i = 0; i < states.size(); ++i) {
                    objTerms.add(cplex.prod(kernel.value(states.get(i), model.getBaseState()), lambdaC[0][i]));
                    objTerms.add(cplex.prod(kernel.value(states.get(i), model.getBaseState()), lambdaS[0][i]));
                }
            }

            for (int i = 0; i < states.size(); ++i) {
                objTerms.add(cplex.prod(model.getRewardFunction().value(states.get(i),
                                                                        StoppingAction.STOP)
                                                                        * 2. * this.gamma,
                                        lambdaS[t][i]));
                QPColumn stopColumn = columnStore.getColumn(t, i, StoppingAction.STOP);
                System.out.println("time: " + t + "\nstop column: "  + stopColumn.toString());
                QPColumn contColumn = columnStore.getColumn(t, i, StoppingAction.CONTINUE);
                System.out.println("time: " + t + "\ncont column: "  + contColumn.toString());
                for (int j = 0; j < stopColumn.curS.length; ++j) {
                    objTerms.add(cplex.prod(lambdaS[t][i], lambdaS[t][j], stopColumn.curS[j]));
                }
                for (int j = 0; j < stopColumn.curC.length; ++j) {
                    objTerms.add(cplex.prod(lambdaS[t][i], lambdaC[t][j], stopColumn.curC[j]));
                }
                if (stopColumn.prevC != null) {
                    for (int j = 0; j < stopColumn.prevC.length; ++j) {
                        objTerms.add(cplex.prod(lambdaS[t][i], lambdaC[t-1][j], stopColumn.prevC[j]));
                    }
                }
                if (stopColumn.nextC != null) {
                    for (int j = 0; j < stopColumn.nextC.length; ++j) {
                        objTerms.add(cplex.prod(lambdaS[t][i], lambdaC[t+1][j], stopColumn.nextC[j]));
                    }
                }
                if (stopColumn.nextS != null) {
                    for (int j = 0; j < stopColumn.nextS.length; ++j) {
                        objTerms.add(cplex.prod(lambdaS[t][i], lambdaS[t+1][j], stopColumn.nextS[j]));
                    }
                }

                for (int j = 0; j < contColumn.curS.length; ++j) {
                    objTerms.add(cplex.prod(lambdaC[t][i], lambdaS[t][j], contColumn.curS[j]));
                }
                for (int j = 0; j < contColumn.curC.length; ++j) {
                    objTerms.add(cplex.prod(lambdaC[t][i], lambdaC[t][j], contColumn.curC[j]));
                }
                if (contColumn.prevC != null) {
                    for (int j = 0; j < contColumn.prevC.length; ++j) {
                        objTerms.add(cplex.prod(lambdaC[t][i], lambdaC[t-1][j], contColumn.prevC[j]));
                    }
                }
                if (contColumn.nextC != null) {
                    for (int j = 0; j < contColumn.nextC.length; ++j) {
                        objTerms.add(cplex.prod(lambdaC[t][i], lambdaC[t+1][j], contColumn.nextC[j]));
                    }
                }
                if (contColumn.nextS != null) {
                    for (int j = 0; j < contColumn.nextS.length; ++j) {
                        objTerms.add(cplex.prod(lambdaC[t][i], lambdaS[t+1][j], contColumn.nextS[j]));
                    }
                }
            }
        }
        IloNumExpr obj = cplex.sum(objTerms.toArray(new IloNumExpr[objTerms.size()]));
        cplex.addMinimize(obj);
    }

    @Override
    public boolean solve() throws IloException {
        return cplex.solve();
    }

    @Override
    public Policy getPolicy() {
        return null;
    }
}
