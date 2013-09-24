package com.moallemi.queueing;

import java.util.*;
import java.io.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;
import com.moallemi.math.DiscreteDistribution;
import com.moallemi.util.PropertySet;

public class OpenQueueingNetworkModel implements Model {
    // number of serves
    private int serverCount;
    // number of queues
    private int queueCount;
    // all possible actions
    protected ServerAction[] allActions;
    // arrival rates
    private double [] lambda;
    // service rates
    private double[][] mu;
    // routing probabilities
    private double[][] routing;
    // maximum queue length
    private int maxQueueLength;
    // normalization factor
    private double normalization;
    // cost vector
    private double[] cost;
    private double[] rejectionCost;

    // 
    /**
     * Constructor.
     *
     * @param props set of properties
     */
    public OpenQueueingNetworkModel(PropertySet props) { 
        init(props); 
        initNormalization();
        initActions();
    }

    protected OpenQueueingNetworkModel() {}

    protected void init(PropertySet props) {
        // some initialization
        queueCount = props.getInt("queue_count");
        serverCount = props.getInt("server_count");
        lambda = new double [queueCount];
        for (int i = 0; i < queueCount; i++) {
            lambda[i] = props.getDoubleDefault("lambda[" + (i+1) + "]", 0.0);
            if (lambda[i] < 0.0)
                throw new IllegalArgumentException("bad lambda");
        }
        mu = new double [queueCount][serverCount];
        for (int i = 0; i < queueCount; i++) {
            for (int j = 0; j < serverCount; j++) {
                mu[i][j] = 
                    props.getDoubleDefault("mu[" + (i+1) + "][" + (j+1) + "]", 
                                           0.0);
                if (mu[i][j] < 0.0)
                    throw new IllegalArgumentException("bad mu");
            }
        }
        routing = new double [queueCount][queueCount+1];
        for (int i = 0; i < queueCount; i++) {
            double totalProbability = 0.0;
            for (int r = 0; r <= queueCount; r++) {
                routing[i][r] =
                    props.getDoubleDefault("routing[" + (i+1) + "][" 
                                           + (r+1) + "]", 0.0);
                if (routing[i][r] < 0.0 || routing[i][r] > 1.0) 
                    throw new IllegalArgumentException("bad routing "
                                                       + "probability");
                totalProbability += routing[i][r];
            }
            if (totalProbability > 1.0)
                throw new IllegalArgumentException("bad routing "
                                                   + "probability");
            routing[i][queueCount] += 1.0 - totalProbability;
        }
        maxQueueLength = props.getInt("max_queue_length"); 
        queueCount = lambda.length;
        cost = new double [queueCount];
        for (int i = 0; i < queueCount; i++)
            cost[i] = props.getDouble("cost[" + (i+1) + "]");
        rejectionCost = new double [queueCount];
        for (int i = 0; i < queueCount; i++)
            rejectionCost[i] = props.getDoubleDefault("rejection_cost[" 
                                                      + (i+1) + "]", 0.0);
    }            

    protected void init(double[] lambda,
                        double[][] mu,
                        double[][] routing,
                        int maxQueueLength,
                        double[] cost,
                        double[] rejectionCost) {
        this.lambda = lambda;
        this.mu = mu;
        this.routing = routing;
        queueCount = lambda.length;
        serverCount = mu[0].length;
        this.maxQueueLength = maxQueueLength;
        this.cost = cost;
        this.rejectionCost = rejectionCost;
    }

    protected void initNormalization() {
        normalization = 0.0;
        for (int i = 0; i < queueCount; i++)
            normalization += lambda[i];
        for (int i = 0; i < queueCount; i++) 
            for (int j = 0; j < serverCount; j++)
                normalization += mu[i][j];
    }        

    protected void initActions() {
        allActions = ServerAction.enumerate(this);
    }

    // Model interface

    public State getBaseState() {
        int[] queueLengths = new int [queueCount];
        return new QueueState(queueLengths);
    }

    public StateList enumerateStates() {
        // enumerate states
        int stateCount = 1;
        for (int i = 0; i < queueCount; i++) {
            stateCount *= maxQueueLength + 1;
            if (stateCount <= 0) {
                throw new IllegalStateException("too many states");
            }
        }

	int[] queueLength = new int [queueCount];
        State[] states = new State [stateCount];
        boolean found = true;
        Arrays.fill(queueLength, 0);
        int count = 0;
        while (found) {
            int[] copy = new int [queueCount];
            System.arraycopy(queueLength, 0, copy, 0, queueCount);
	    QueueState state = new QueueState(copy);
            states[count++] = state;

            found = false;
	    for (int i = 0; i < queueLength.length && !found; i++) {
		queueLength[i]++;
		if (queueLength[i] <= maxQueueLength) 
                    found = true;
                else
                    queueLength[i] = 0;
	    }
        }
        if (count != stateCount)
            throw new IllegalStateException();

        return new StateList(this, states);        
    }

    protected boolean isWorkConserving(QueueState state, ServerAction action) 
    {
	for (int j = 0; j < serverCount; j++) {
            if (action.getQueueWorkedOn(j) < 0) {
                for (int i = 0; i < queueCount; i++) {
                    if (mu[i][j] > 0.0 
                        && state.getQueueLength(i) > 0)
                        return false;
                }
            }
	}
	return true;
    }

    // this is inefficient, next states are not cached
    // maybe worth doing that to minimize memory allocation
    public StateInfo getStateInfo(State state, StateSet set) {
        QueueState qState = (QueueState) state;

        // figure out which actions are available
        ArrayList<ServerAction> list = 
            new ArrayList<ServerAction>(allActions.length);
        for (int a = 0; a < allActions.length; a++) {
            if (allActions[a].isCompatible(state)
                && isWorkConserving(qState, allActions[a])) 
                list.add(allActions[a]);
        }
        Action[] actions = list.toArray(new Action [0]);
        if (actions.length <= 0)
            throw new RuntimeException("no available actions");

        StateDistribution[] distributions = 
            new StateDistribution [actions.length];

        // figure out next states under each action
        int[] nextLengths = new int [queueCount];
        for (int a = 0; a < actions.length; a++) {
            ServerAction thisAction = (ServerAction) actions[a];
            DiscreteDistribution distribution = thisAction.getDistribution();
            int distSize = distribution.size();
            State[] nextStates = new State [distSize];
            outer:
            for (int position = 0; position < distSize; position++) {
                int[] offsets = thisAction.getOffsets(position);
                if (offsets == null) {
                    // self-transition
                    nextStates[position] = qState; 
                }
                else {
                    qState.copyQueueLengths(nextLengths);
                    for (int i = 0; i < queueCount; i++) {
                        nextLengths[i] += offsets[i];
                        if (nextLengths[i] > maxQueueLength
                            || nextLengths[i] < 0) {
                            // self-transition
                            nextStates[position] = qState; 
                            continue outer;
                        }
                    }
                    // new state
                    QueueState tmp = new QueueState(nextLengths);
                    QueueState nextState = (QueueState) set.getState(tmp);
                    if (nextState == null) {
                        nextLengths = new int [queueCount];
                        nextState = tmp;
                    }
                    nextStates[position] = nextState;
                }
            }
            distributions[a] = new StateDistribution(distribution, 
                                                     nextStates);
        }                        


        // figure out next state when each queue is serviced
        nextLengths = new int [queueCount];
        QueueState[] nextStateIfServiced = new QueueState [queueCount];
        for (int i = 0; i < queueCount; i++) {
            if (qState.getQueueLength(i) == 0) 
                nextStateIfServiced[i] = qState;
            else {
                qState.copyQueueLengths(nextLengths);
                nextLengths[i]--;
                QueueState tmp = new QueueState(nextLengths);
                QueueState nextState = (QueueState) set.getState(tmp);
                if (nextState == null) {
                    nextLengths = new int [queueCount];
                    nextState = tmp;
                }
                nextStateIfServiced[i] = nextState;
            }
        }

        return new QueueStateInfo(actions, distributions, nextStateIfServiced);
    }

    public StateFunction getCostFunction() {
        return new StateFunction() {
                public double getValue(State state) {
                    QueueState qState = (QueueState) state;
                    double sum = 0.0;
                    for (int i = 0; i < queueCount; i++) {
                        int q = qState.getQueueLength(i);
                        sum += q * cost[i];
                        if (q >= maxQueueLength)
                            sum += rejectionCost[i];
                    }
                    return sum / ((double) queueCount);
                }
            };
    }

    public double getCost(int i) { return cost[i]; }
    public double getRejectionCost(int i) { return rejectionCost[i]; }

    public int getServerCount() { return serverCount; }
    public int getQueueCount() { return queueCount; }
    public int getMaxQueueLength() { return maxQueueLength; }
    public double getArrivalRate(int i) { return lambda[i]; }
    public double getServiceRate(int i, int j) { return mu[i][j]; }
    public double getRouteProbability(int i, int r) { return routing[i][r]; }
    public double getNormalization() { return normalization; }

    public double getLoadFactor() throws IloException {
        IloCplex cplex = new IloCplex();
        
        // variables
        IloNumVar rhoVar = cplex.numVar(0.0, Double.MAX_VALUE);
        IloNumVar[][] xVar = new IloNumVar [queueCount][serverCount];
        for (int i = 0; i < queueCount; i++)
            xVar[i] = cplex.numVarArray(serverCount, 0.0, 1.0);

        // objective
        cplex.addMinimize(rhoVar);

        // rate-matching constraints
        for (int i = 0; i < queueCount; i++) {
            // jobs being worked on
            double[] rates = new double [serverCount];
            for (int j = 0; j < serverCount; j++)
                rates[j] = mu[i][j];
            IloNumExpr expr = cplex.scalProd(rates,
                                             xVar[i]);
            // jobs being routed
            for (int i2 = 0; i2 < queueCount; i2++) {
                double[] routes = new double [serverCount];
                for (int j = 0; j < serverCount; j++)
                    routes[j] = - mu[i2][j] * routing[i2][i];
                expr = cplex.sum(expr,
                                 cplex.scalProd(routes,
                                                xVar[i2]));
            }
            cplex.addEq(expr,
                        lambda[i]);
        }

        // utilization constraints
        for (int j = 0; j < serverCount; j++) {
            double[] mones = new double [queueCount];
            Arrays.fill(mones, -1.0);
            IloNumVar[] qXVar = new IloNumVar [queueCount];
            for (int i = 0; i < queueCount; i++)
                qXVar[i] = xVar[i][j];

            cplex.addGe(cplex.sum(rhoVar,
                                  cplex.scalProd(mones,
                                                 qXVar)),
                        0.0);
        }

        // solve
        if (!cplex.solve())
            throw new IllegalStateException("failed to solve load LP");
        
        return cplex.getValue(rhoVar);
    }
        
    public void dumpInfo(PrintStream out) throws IOException {
        for (int i = 0; i < queueCount; i++) {
            if (lambda[i] > 0.0)
                out.println("lambda[" + (i+1) + "] = " + lambda[i]);
        }
        for (int i = 0; i < queueCount; i++) {
            for (int j = 0; j < serverCount; j++) {
                if (mu[i][j] > 0.0) 
                    out.println("mu[" + (i+1) + "][" + (j+1) + "] = " 
                                + mu[i][j]);
            }
        }
        for (int i = 0; i < queueCount; i++) {
            for (int i2 = 0; i2 < queueCount; i2++) {
                if (routing[i][i2] > 0.0) 
                    out.println("routing[" + (i+1) + "][" + (i2+1) + "] = " 
                                + routing[i][i2]);
            }
        }
        for (int i = 0; i < queueCount; i++) 
            out.println("cost[" + (i+1) + "] = " + cost[i]);
    }

    public void writeModel(PrintStream out) throws IOException {
        out.println("queue_count = " + queueCount);
        out.println("server_count = " + serverCount);
        out.println("max_queue_length = " + maxQueueLength);
        for (int i = 0; i < queueCount; i++) {
            if (lambda[i] > 0.0)
                out.println("lambda[" + (i+1) + "] = " + lambda[i]);
        }
        for (int i = 0; i < queueCount; i++) {
            for (int j = 0; j < serverCount; j++) {
                if (mu[i][j] > 0.0) 
                    out.println("mu[" + (i+1) + "][" + (j+1) + "] = " 
                                + mu[i][j]);
            }
        }
        for (int i = 0; i < queueCount; i++) {
            for (int i2 = 0; i2 < queueCount; i2++) {
                if (routing[i][i2] > 0.0) 
                    out.println("routing[" + (i+1) + "][" + (i2+1) + "] = " 
                                + routing[i][i2]);
            }
        }
        for (int i = 0; i < queueCount; i++) 
            out.println("cost[" + (i+1) + "] = " + cost[i]);
        for (int i = 0; i < queueCount; i++) {
            if (rejectionCost[i] > 0.0)
                out.println("rejection_cost[" + (i+1) + "] = " 
                            + rejectionCost[i]);
        }
    }

}
