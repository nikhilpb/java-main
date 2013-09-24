package com.moallemi.iqswitch;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.moallemi.math.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;
import com.moallemi.util.data.Pair;


public class SwitchMain extends CommandLineMain {
    private SwitchModel model;
    private PolicySolver solver;
    private MatchingPolicy policy;
    private MatchingPolicy refPolicy;
    private BasisSet basis;
    private SwitchState[] stateList;
    private double[] stateWeights;

    private CplexFactory cplexFactory = new CplexFactory();
    private BipartiteMatcherFactory mFactory = new BipartiteMatcherFactory();
    private ObjectHistogram<SwitchState> stateHistogram 
        = new ObjectHistogram<SwitchState> ();
    private String debugIterFile = null;
    private int debugIter = 0;
    
    protected boolean processCommand(CommandLineIterator cmd) 
        throws Exception
    {
        
        String base = cmd.next();
        if (base.equals("loadmodel")) {
            String fname = cmd.next();
            System.out.println("loading model: " + fname);
            PropertySet props = new PropertySet(new File(fname));
            model = new SwitchModel(props);
        }

        else if (base.equals("model")) {
            String switchSize = cmd.next();
            String type = cmd.next();
            if (type.equals("uniform")) {
                String rho = cmd.next();
                PropertySet props = new PropertySet();
                props.setProperty("switch_size", switchSize);
                props.setProperty("arrival_type", "uniform");
                props.setProperty("rho", rho);
                model = new SwitchModel(props);
            }
            else if (type.equals("diagonal")) {
                String k = cmd.next();
                String rho = cmd.next();
		String falloff = cmd.next();
                PropertySet props = new PropertySet();
                props.setProperty("switch_size", switchSize);
                props.setProperty("arrival_type", "diagonal");
                props.setProperty("rho", rho);
                props.setProperty("falloff", falloff);
                props.setProperty("k", k);
                model = new SwitchModel(props);
            }
            else
                throw new CommandLineException("unknown switch model: " 
                                               + type);
        }

        else if (base.equals("setdebugiter")) {
            debugIterFile = cmd.next();
            debugIter = cmd.nextInt();
        }

        else if (base.equals("setrefpolicy")) {
            refPolicy = policy;
        }

        else if (base.equals("cplex")) {
            String fname = cmd.next();
            PropertySet props;
            if (fname.equals("null"))
                props = null;
            else
                props = new PropertySet(new File(fname));
            cplexFactory = new CplexFactory(props);
        }

        else if (base.equals("matcher")) {
            String type = cmd.next();
            mFactory.setType(type);
        }

        else if (base.equals("loadfactor")) {
            System.out.println("computing load factor");
            double load = model.getLoadFactor();
            System.out.println("LOAD FACTOR: " + load);
        }

        else if (base.equals("policy")) {
            String type = cmd.next();
            System.out.println("loading " + type + " policy");
            if (type.equals("mw")) {
                double alpha = cmd.nextDouble();
                ScalarFunction f = new PolyScalarFunction(alpha);
                SeparableFunction valueFunction =
                    new SymmetricQueueLengthScalarFunction(model, f);
                policy = new MatchingPolicy(model, valueFunction, mFactory);
            }
            else if (type.equals("mwh")) {
                double alpha = cmd.nextDouble();
                SeparableFunction valueFunction =
                    new MaxWeightHeuristicPolicyFunction(model, alpha);
                policy = new MatchingPolicy(model, valueFunction, mFactory);
            }
            else if (type.equals("rowcol")) {
                double alpha = cmd.nextDouble();
                ScalarFunction[] f = new ScalarFunction [1];
                f[0] = new PolyScalarFunction(alpha);
                BasisSet b = 
                    new SymmetricRowColSumBasisSet(model,
                                                   -1,
                                                   -1,
                                                   true,
                                                   new ScalarFunction [0],
                                                   f);
                double[] w = new double [2];
                Arrays.fill(w, 1.0);
                SeparableFunction valueFunction =
                    new LinearCombinationFunction(w, b);
                policy = new MatchingPolicy(model, valueFunction, mFactory);
            }
            else if (type.equals("rowcolh")) {
                double alpha = cmd.nextDouble();
                SeparableFunction valueFunction =
                    new RowColSumHeuristicPolicyFunction(model, alpha);
                policy = new MatchingPolicy(model, valueFunction, mFactory);
            }
            else if (type.equals("lph")) {
                SeparableFunction valueFunction =
                    new LongestPortHeuristicPolicyFunction(model);
                policy = new MatchingPolicy(model, valueFunction, mFactory);
            }
            else
                throw new CommandLineException("unknown policy type: " + type);
        }

        else if (base.equals("basis")) {
            String type = cmd.next();
            System.out.println("loading " + type + " basis set");
            if (type.equals("symrowcolsum")) {
                int cutoff = cmd.nextInt();
                int singleCutoff = cmd.nextInt();
                boolean symRowCol = cmd.nextBoolean();
                ScalarFunction[] rowColFunctions = 
                    parseScalarFunctions(cmd.next());
                ScalarFunction[] queueFunctions = 
                    parseScalarFunctions(cmd.next());
                basis = 
                    new SymmetricRowColSumBasisSet(model,
                                                   cutoff,
                                                   singleCutoff,
                                                   symRowCol,
                                                   queueFunctions,
                                                   rowColFunctions);
            }
            else if (type.equals("rowcolsum")) {
                int cutoff = cmd.nextInt();
                int singleCutoff = cmd.nextInt();
                ScalarFunction[] queueFunctions = 
                    parseScalarFunctions(cmd.next());
                ScalarFunction[] rowColFunctions = 
                    parseScalarFunctions(cmd.next());
                basis = 
                    new RowColSumBasisSet(model,
                                          cutoff,
                                          singleCutoff,
                                          queueFunctions,
                                          rowColFunctions);
            }
            else 
                throw new CommandLineException("unknown basis " + type);
            System.out.println(basis.size() + " functions");
        }

        else if (base.equals("solver")) {
            String type = cmd.next();
            if (type.equals("td")) {
                System.out.println("solving using TD solver");
                double alpha = cmd.nextDouble();
                double gammaA = cmd.nextDouble();
                double gammaB = cmd.nextDouble();
                double lambda = cmd.nextDouble();
                int timeStepCount = cmd.nextInt();
                int timeReportCount = cmd.nextInt();
                solver = 
                    new TDSolver(model,
                                 basis,
                                 getRandom(),
                                 gammaA,
                                 gammaB,
                                 lambda,
                                 timeStepCount,
                                 timeReportCount,
                                 alpha,
                                 mFactory);
            }                
            else if (type.equals("kalman")) {
                System.out.println("solving using Kalman Filter solver");
                double alpha = cmd.nextDouble();
                double gammaA = cmd.nextDouble();
                double gammaB = cmd.nextDouble();
                int timeStepCount = cmd.nextInt();
                int timeReportCount = cmd.nextInt();
                solver = 
                    new KalmanFilterSolver(model,
                                           basis,
                                           getRandom(),
                                           gammaA,
                                           gammaB,
                                           timeStepCount,
                                           timeReportCount,
                                           alpha,
                                           mFactory);
            }                
            else if (type.equals("alp")) {
                System.out.println("solving using ALP solver");
                double alpha = cmd.nextDouble();
                solver = new ALPSolver(model,
                                       basis,
                                       stateList,
                                       stateWeights,
                                       alpha,
                                       cplexFactory);
            }
            else
                throw new CommandLineException("unknown solver: " 
                                               + type);

            System.out.println("starting solution");

            PrintStream debugOut = null;
            if (debugIter > 0) {
                if (solver instanceof TDSolver) {
                    debugOut = openOutput(debugIterFile);
                    ((TDSolver) solver).setDebug(debugOut, debugIter);
                }
                else if (solver instanceof KalmanFilterSolver) {
                    debugOut = openOutput(debugIterFile);
                    ((KalmanFilterSolver) solver).setDebug(debugOut, 
                                                           debugIter);
                }
            }
            else {
                if (solver instanceof TDSolver) 
                    ((TDSolver) solver).setDebug(null, 0);
                else if (solver instanceof KalmanFilterSolver)
                    ((KalmanFilterSolver) solver).setDebug(null, 0);
            }

            if (solver.solve()) 
                System.out.println("solution successful");
            else {
                //System.out.println("solution unsuccessful");
                throw new IllegalStateException("solution unsuccessful");
            }

            if (debugOut != null)
                closeOutput(debugOut);
            
            SeparableFunction valueFunction = solver.getPolicyFunction();
            policy = new MatchingPolicy(model, valueFunction, mFactory);
        }

        else if (base.equals("simvalue")) {
            System.out.println("simulating average cost");
            int time = cmd.nextInt();
            int printFreq = cmd.nextInt();
            Function costFunction = model.getCostFunction();
            MVSampleStatistics stats = new MVSampleStatistics();
            PathSimulator sim =  new PathSimulator(model,
                                                   policy,
                                                   getRandom());
            for (int t = 1; t <= time; t++) {
                SwitchState state = sim.next();
                double cost = costFunction.getValue(state);
                stats.addSample(cost);
                if (t % printFreq == 0)
                    System.out.println("TIME: "
                                       + t
                                       + " COST: "
                                       + cost
                                       + " AVG COST: "
                                       + stats.getMean());
            }
            System.out.println("TIME: "
                               + time
                               + " AVG COST: "
                               + stats.getMean());
        }

        else if (base.equals("mcvalue")) {
            System.out.println("evaluating average cost by MC");
            int time = cmd.nextInt();
            int paths = cmd.nextInt();
            boolean computeStateHist = cmd.nextBoolean();
            PathSimulator sim = new PathSimulator(model,
                                                  policy,
                                                  null);
            MonteCarloStateObserver observer = null;
            if (computeStateHist) {
                stateHistogram.clearAll();
                observer = new MonteCarloStateObserver() {
                        public void observe(int path, 
                                            int time, 
                                            SwitchState state) 
                        {
                            stateHistogram.add(state);
                        }
                    };
            }
            MonteCarloEvaluator mce = 
                new MonteCarloEvaluator(model,
                                        sim, 
                                        getRandom(),
                                        model.getCostFunction(),
                                        time,
                                        paths,
                                        observer);
            SampleStatistics stats = mce.getSampleStatistics();
            while (mce.hasNext()) {
                double value = mce.next();
                System.out.print("PATH: " + mce.getPath()
                                 + " VALUE: " + value);
                if (mce.getPath() % 2 == 0)
                    System.out.print(" RUNNING AVG: " + stats.getMean());
                System.out.println();
            }
            double stddev = stats.getCount() > 1 
                ? stats.getStandardDeviation()
                : 0.0;
            System.out.println("MEAN: " + stats.getMean()
                               + " STDDEV: " + stddev
                               + " ERROR: " 
                               + stddev/Math.sqrt(stats.getCount()));

        }

        else if (base.equals("dumpinfo")) {
            PrintStream out = openOutput(cmd.next());
            System.out.println("dumping solver info");
            solver.dumpInfo(out);
            closeOutput(out);
        }

        else if (base.equals("dumpmodel")) {
            PrintStream out = openOutput(cmd.next());
            System.out.println("dumping model info");
            model.dumpInfo(out);
            closeOutput(out);
        }

        else if (base.equals("dumpbasis")) {
            PrintStream out = openOutput(cmd.next());
            System.out.println("dumping basis info");
            int basisSize = basis.size();
            for (int i = 0; i < basisSize; i++) 
                out.println("basis[" + (i+1) + "] = " + basis.getFunctionName(i));
            closeOutput(out);
        }

        else if (base.equals("originvalue")) {
            System.out.println("dumping origin value function behavior");
            PrintStream out = openOutput(cmd.next());
            int count = cmd.nextInt();
            double[] value = new double [count+1];
            Function valueFunction = solver.getPolicyFunction();
            int switchSize = model.getSwitchSize();
            int[][] q = new int [switchSize][switchSize];
            SwitchState state = new SwitchState(q);
            for (int i = 0; i <= count; i++) {
                q[0][0] = i;
                value[i] = valueFunction.getValue(state);
            }
            double d0 = value[1] - value[0];
            for (int i = 0; i < count; i++) {
                q[0][0] = i;
                out.println("STATE: "
                            + state.toString() 
                            + " VALUE DERIV: " 
                            + (value[i+1] - value[i])/d0);
            }
            closeOutput(out);
        }

        else if (base.equals("comparepolicy")) {        
            PrintStream out = openOutput(cmd.next());
	    double tolerance = cmd.nextDouble();
            boolean diffOnly = cmd.nextBoolean();
            System.out.println("comparing computed policies");
            // sort the states
            SwitchState[] sorted = new SwitchState [stateList.length];
            System.arraycopy(stateList, 0, sorted, 0, stateList.length);
            Arrays.sort(sorted, new SwitchStateComparator(model));
	    for (int i = 0; i < sorted.length; i++) {
		SwitchState state = sorted[i];
                refPolicy.setState(state);
                SwitchAction refAction = refPolicy.getAction();
                policy.setState(state);
                SwitchAction action = policy.getAction();
                if (!refAction.equals(action)) {
                    boolean refOptForPolicy = 
                        policy.isActionOptimal(refAction, tolerance);
                    boolean policyOptForRef = 
                        refPolicy.isActionOptimal(action, tolerance);
                    if (diffOnly && refOptForPolicy && policyOptForRef)
                        continue;
                    out.print("STATE: " + state.toString() + " ACTION: ");
                    out.print(refAction.toString());
                    if (refOptForPolicy)
                        out.print(" (ab) ");
                    else
                        out.print(" (a) ");
                    out.print(action.toString());
                    if (policyOptForRef)
                        out.print(" (ab)");
                    else
                        out.print(" (b)");
                    out.println();
                }
            }
            closeOutput(out);
        }

        else if (base.equals("statelistfromhist")) {
            int size = stateHistogram.getNumBins();
            stateList = new SwitchState [size];
            stateWeights = new double [size];
            int c = 0;
            for (Iterator<SwitchState> i = stateHistogram.binIterator();
                 i.hasNext(); ) {
                SwitchState q = i.next();                    
                stateList[c] = q;
                stateWeights[c] = stateHistogram.getBinFrequency(q);
                c++;
            }
            verifyWeights();
            System.out.println("UNIQUE STATES: " + stateList.length);
        }

        // pass to subclass
        else
            return false;

        return true;
    }

    public void verifyWeights() {
        int size = stateWeights.length;
        double total = 0.0;
        for (int i = 0; i < size; i++) {
            double w = stateWeights[i];
            if (w < 0.0 || w > 1.0)
                throw new IllegalArgumentException("weights out of range");
            total += w;
        }
        if (Math.abs(total - 1.0) > 1e-10)
            throw new IllegalArgumentException("weights not normalized");
    }

    public ScalarFunction[] parseScalarFunctions(String s) {
        if (s.equals(","))
            return new ScalarFunction [0];
        String[] split = s.split(",");
        ScalarFunction[] f = new ScalarFunction [split.length];
        for (int i = 0; i < f.length; i++) {
            if (split[i].equals("log")) {
                f[i] = new LogScalarFunction();
            }
            else if (split[i].equals("xlogx")) {
                f[i] = new XLogXScalarFunction();
            }
            else {
                double power = Double.parseDouble(split[i]);
                f[i] = new PolyScalarFunction(power);
            }
        }
        return f;
    }

    public static void main(String[] argv) throws Exception {
        (new SwitchMain()).run(argv);
    }
}
