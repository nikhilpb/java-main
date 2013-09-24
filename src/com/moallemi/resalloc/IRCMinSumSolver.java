package com.moallemi.resalloc;

import com.moallemi.minsum.*;

public class IRCMinSumSolver {
    protected InelasticRateControlProblem problem;
    protected double damp;

    protected int userCount;
    protected int linkCount;
    
    protected boolean[] infeasibleUser;
    protected double[] bias;
    protected InelasticRateControlSolution solution;

    // indexed by [user][link_adj_index]
    protected double[][] userIncomingMsg;
    protected double[] sumUserIncomingMsg;
    // indexed by [link][user_adj_index]
    protected double[][] linkIncomingMsg;
    protected PiecewiseConstantFunction[][] sumLinkIncomingMsgUp;
    protected PiecewiseConstantFunction[][] sumLinkIncomingMsgDown;
    
    private double bellmanError;

    public double getBellmanError() { return bellmanError; }
    
    
    public IRCMinSumSolver(InelasticRateControlProblem problem, double damp) {
        this.problem = problem;
        this.damp = damp;

        userCount = problem.getVariableCount();
        linkCount = problem.getFactorCount();
        solution = new InelasticRateControlSolution(problem);

        // compute a list of users which can never be feasible
        infeasibleUser = new boolean [userCount];
        for (int u = 0; u < userCount; u++) {
            infeasibleUser[u] = false;
            double b_u = problem.getUserMinBandwidth(u);
            int degree = problem.getVariableDegree(u);
            for (int lIndex = 0; lIndex < degree; lIndex++) {
                int l = problem.getVariableNeighbor(u, lIndex);
                if (b_u > problem.getLinkCapacity(l)) {
                    infeasibleUser[u] = true;
                    break;
                }
            }
        }
    
        userIncomingMsg = new double [userCount][];
        sumUserIncomingMsg = new double [userCount];
        for (int u = 0; u < userCount; u++) {
            if (!infeasibleUser[u])
                userIncomingMsg[u] = new double [problem.getVariableDegree(u)];
        }

        linkIncomingMsg = new double [linkCount][];
        sumLinkIncomingMsgUp = new PiecewiseConstantFunction [linkCount][];
        sumLinkIncomingMsgDown = new PiecewiseConstantFunction [linkCount][];
        for (int l = 0; l < linkCount; l++) {
            int degree = problem.getFactorDegree(l);
            linkIncomingMsg[l] = new double [degree];
            sumLinkIncomingMsgUp[l] = new PiecewiseConstantFunction [degree];
            sumLinkIncomingMsgDown[l] = new PiecewiseConstantFunction [degree];
            double c_l = problem.getLinkCapacity(l);

            int lastU = -1;
            for (int uIndex = 0; uIndex < degree; uIndex++) {
                int u = problem.getFactorNeighbor(l, uIndex);
                if (!infeasibleUser[u]) {
                    if (lastU >= 0)
                        sumLinkIncomingMsgDown[l][uIndex]
                            = new PiecewiseConstantFunction(c_l);
                    lastU = u;
                }
            }

            lastU = -1;
            for (int uIndex = degree - 1; uIndex >= 0; uIndex--) {
                int u = problem.getFactorNeighbor(l, uIndex);
                if (!infeasibleUser[u]) {
                    if (lastU >= 0) 
                        sumLinkIncomingMsgUp[l][uIndex]
                            = new PiecewiseConstantFunction(c_l);
                    lastU = u;
                }
            }
        }

        bias = new double [userCount];
    }

    public void iterate() {
        bellmanError = 0.0;

        // compute new messages to links
        // u(x_u) + \sum_{m \neq l} V_{m\ra u}(x_u)
        for (int l = 0; l < linkCount; l++) {
            int degree = linkIncomingMsg[l].length;
            for (int uIndex = 0; uIndex < degree; uIndex++) {
                int u = problem.getFactorNeighbor(l, uIndex);
                if (infeasibleUser[u])
                    continue;

                int lIndex = problem.getFactorNeighborOffset(l, uIndex);
                double newValue = problem.getUserUtility(u)
                    + sumUserIncomingMsg[u]
                    - userIncomingMsg[u][lIndex];

                bellmanError += Math.abs(newValue - linkIncomingMsg[l][uIndex]);
                linkIncomingMsg[l][uIndex] = damp * newValue
                    + (1.0 - damp) * linkIncomingMsg[l][uIndex];
            }
        }

        // solve dynamic programming subproblems at each factor node
        // up and down
        for (int l = 0; l  < linkCount; l++) {
            int degree = linkIncomingMsg[l].length;
            PiecewiseConstantFunction lastF = null;
            int lastU = -1;
            int lastUIndex = -1;
            for (int uIndex = 0; uIndex < degree; uIndex++) {
                int u = problem.getFactorNeighbor(l, uIndex);
                if (!infeasibleUser[u]) {
                    if (lastU >= 0) {
                        sumLinkIncomingMsgDown[l][uIndex]
                            .setSingleOpt(linkIncomingMsg[l][lastUIndex],
                                          problem.getUserMinBandwidth(lastU),
                                          lastF);
                        lastF = sumLinkIncomingMsgDown[l][uIndex];
                    }
                    lastU = u;
                    lastUIndex = uIndex;
                }
            }
            lastF = null;
            lastU = -1;
            lastUIndex = -1;
            for (int uIndex = degree - 1; uIndex >= 0; uIndex--) {
                int u = problem.getFactorNeighbor(l, uIndex);
                if (!infeasibleUser[u]) {
                    if (lastU >= 0) {
                        sumLinkIncomingMsgUp[l][uIndex]
                            .setSingleOpt(linkIncomingMsg[l][lastUIndex],
                                          problem.getUserMinBandwidth(lastU),
                                          lastF);
                        lastF = sumLinkIncomingMsgUp[l][uIndex];
                    }
                    lastU = u;
                    lastUIndex = uIndex;
                }
            }
        }
            

        // compute new messages to users
        // max_{x_v} \sum_{v\neq u} V_{r\ra l}(x_v}
        // s.t. \sum_{v\neq u} b_v x_v <= c_l - b_u x_u 
        for (int u = 0; u < userCount; u++) {
            if (infeasibleUser[u])
                continue;
            sumUserIncomingMsg[u] = 0.0;

            int degree = userIncomingMsg[u].length;
            double b_u = problem.getUserMinBandwidth(u);
            for (int lIndex = 0; lIndex < degree; lIndex++) {
                int l = problem.getVariableNeighbor(u, lIndex);
                int uIndex = problem.getVariableNeighborOffset(u, lIndex);

                PiecewiseConstantFunction f1, f2;
                if (sumLinkIncomingMsgUp[l][uIndex] != null) {
                    f1 = sumLinkIncomingMsgUp[l][uIndex];
                    f2 = sumLinkIncomingMsgDown[l][uIndex];
                }
                else {
                    f1 = sumLinkIncomingMsgDown[l][uIndex];
                    f2 = sumLinkIncomingMsgUp[l][uIndex];
                }

                double newValue = f1 == null
                    ? 0.0
                    : f1.computePairOpt(problem.getLinkCapacity(l), b_u, f2);

                if (newValue > 0.0)
                    throw new IllegalStateException("positive message to user");

                bellmanError += Math.abs(newValue 
                                         - userIncomingMsg[u][lIndex]);
                userIncomingMsg[u][lIndex] = damp * newValue
                    + (1.0 - damp) * userIncomingMsg[u][lIndex];
                sumUserIncomingMsg[u] += userIncomingMsg[u][lIndex];
            }
        }
    }


    public void computeSolution() {
        for (int u = 0; u < userCount; u++) {
            // two possible rounding rules here, seem roughly equivalent

//             if (infeasibleUser[u]) 
//                 bias[u] = Double.NEGATIVE_INFINITY;
//             else if (sumUserIncomingMsg[u] >= 0.0) 
//                 bias[u] = Double.POSITIVE_INFINITY;
//             else
//                 bias[u] = - problem.getUserUtility(u) / sumUserIncomingMsg[u];

            bias[u] = infeasibleUser[u]
                ? Double.NEGATIVE_INFINITY
                : problem.getUserUtility(u) + sumUserIncomingMsg[u];
        }
        solution.setGreedy(bias);
    }

    public InelasticRateControlSolution getSolution() { return solution; }
}
 