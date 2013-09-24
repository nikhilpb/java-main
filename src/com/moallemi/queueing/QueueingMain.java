package com.moallemi.queueing;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.moallemi.adp.*;
import com.moallemi.math.CplexFactory;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;
import com.moallemi.util.data.Pair;


public class QueueingMain extends CommandLineMain {
    private OpenQueueingNetworkModel model;
    private StateList states;
    private double[] weights;
    private Solver solver;
    private BasisSet basis;
    private Policy policy;
    private Policy refPolicy;
    private ObjectHistogram<QueueState> stateHistogram 
        = new ObjectHistogram<QueueState> ();
    private Comparator<State> comparator = new QueueStateComparator();
    private long seed = 0L;
    private String debugIterFile = null;
    private int debugIter = 0;
    private ValueFunctionPolicyFactory policyFactory 
        = new ValueFunctionPolicyFactory();

    protected boolean processCommand(final CommandLineIterator cmd) 
        throws Exception 
    {
        String base = cmd.next();

        if (base.equals("model")) {
            String type = cmd.next();
            String fname = cmd.next();
            System.out.println("loading " + type + " model: " + fname);
            PropertySet props = new PropertySet(new File(fname));
            if (type.equals("open"))
                model = new OpenQueueingNetworkModel(props);
            else if (type.equals("switch"))
                model = new SwitchModel(props);
            else
                throw new RuntimeException("unknown model type");
        }
        else if (base.equals("policystyle")) {
            String type = cmd.next();

            if (type.equals("normal"))
                policyFactory = new ValueFunctionPolicyFactory();
            else if (type.equals("switch"))
                policyFactory = 
                    new SwitchValueFunctionPolicyFactory((SwitchModel) model);
            else
                throw new IllegalArgumentException("unknown policy factory: "
                                                   + type);
        }
        else if (base.equals("loadfactor")) {
            System.out.println("computing load factor");
            double load = model.getLoadFactor();
            System.out.println("LOAD FACTOR: " + load);
        }
        else if (base.equals("setrefpolicy")) {
            refPolicy = policy;
        }
        else if (base.equals("setdebugiter")) {
            debugIterFile = cmd.next();
            debugIter = cmd.nextInt();
        }
        else if (base.equals("policy")) {
            String type = cmd.next();
            System.out.println("loading " + type + " policy");
            if (type.equals("maxweight")) {
                double alpha = cmd.nextDouble();
                StateFunction valueFunction =
                    new MaxWeightValueFunction(model, alpha);
                policy = policyFactory.getPolicy(valueFunction);
            }
            else if (type.equals("maxweightheuristic")) {
                double alpha = cmd.nextDouble();
                policy = new MaxWeightHeuristicPolicy(model, alpha);
            }
            else if (type.equals("maxsumrowcolheuristic")) {
                double alpha = cmd.nextDouble();
                StateFunction valueFunction =
                    new SwitchMaxRowColSumFunction((SwitchModel) model,
                                                   alpha);
                policy = policyFactory.getPolicy(valueFunction);
            }
            else
                throw new RuntimeException("unknown policy type");
        }
        else if (base.equals("basis")) {
            String type = cmd.next();
            System.out.println("loading " + type + " basis set");
            if (type.equals("length")) {
                double[] poly = parsePoly(cmd.next());
                basis = new QueueLengthBasisSet(model.getQueueCount(), 
                                                poly);
            }
            else if (type.equals("separable")) {
                int cutoff = cmd.nextInt();
                double[] poly = parsePoly(cmd.next());
                basis = new SeparableBasisSet(model.getQueueCount(), 
                                              cutoff,
                                              poly);
            }
            else if (type.equals("symsep")) {
                int cutoff = cmd.nextInt();
                double[] poly = parsePoly(cmd.next());
                basis = 
                    new SymmetricSeparableBasisSet(model.getQueueCount(), 
                                                   cutoff,
                                                   poly);
            }
            else if (type.equals("qstate")) {
                int cutoff = cmd.nextInt();
                double[] poly = parsePoly(cmd.next());
                basis = 
                    new QueueStateBasisSet(model.getQueueCount(), 
                                           cutoff,
                                           poly);
            }
            else if (type.equals("swsymqstate")) {
                int cutoff = cmd.nextInt();
                double[] poly = parsePoly(cmd.next());
                basis = 
                    new SwitchSymmetricQueueStateBasisSet(model,
                                                          cutoff,
                                                          poly);
            }
            else if (type.equals("swsymrowcolsum")) {
                boolean sym = cmd.nextBoolean();
                int cutoff = cmd.nextInt();
                int singleCutoff = cmd.nextInt();
                double[] poly = parsePoly(cmd.next());
                basis = 
                    new SwitchSymmetricRowColSumBasisSet((SwitchModel) model,
                                                         sym,
                                                         cutoff,
                                                         singleCutoff,
                                                         poly);
            }
            else if (type.equals("swrowcolsum")) {
                int cutoff = cmd.nextInt();
                int singleCutoff = cmd.nextInt();
                double[] poly = parsePoly(cmd.next());
                basis = 
                    new SwitchRowColSumBasisSet((SwitchModel) model,
                                                cutoff,
                                                singleCutoff,
                                                poly);
            }
            else 
                throw new RuntimeException("unknown basis " + type);
            System.out.println(basis.size() + " functions");
        }
        else if (base.equals("enumerate")) {
            System.out.println("enumerating all states");
            states = model.enumerateStates();
            weights = new double [states.getStateCount()];
            Arrays.fill(weights, 1.0/((double) weights.length));
        }
        else if (base.equals("statelistfromhist")) {
            QueueStateSymmetry symmetry = parseSymmetry(cmd.next());
            
            ObjectHistogram<QueueState> histogram;
            if (symmetry == null) 
                histogram = stateHistogram;
            else {
                histogram = new ObjectHistogram<QueueState> ();
                for (Iterator<QueueState> i = stateHistogram.binIterator();
                     i.hasNext(); ) {
                    QueueState q = i.next();
                    int cnt = q.getQueueCount();
                    int[] tmp = new int [cnt];
                    for (int j = 0; j < cnt; j++)
                        tmp[j] = q.getQueueLength(j);
                    symmetry.canonicalForm(tmp);
                    histogram.add(new QueueState(tmp),
                                  stateHistogram.getBinCount(q));
                }
            }

            int size = histogram.getNumBins();
            State[] stateArray = new State [size];
            weights = new double [size];
            int c = 0;
            for (Iterator<QueueState> i = histogram.binIterator();
                 i.hasNext(); ) {
                QueueState q = i.next();                    
                stateArray[c] = q;
                weights[c] = histogram.getBinFrequency(q);
                c++;
            }
            states = new StateList(model, stateArray);
            verifyWeights();
            
            System.out.println("UNIQUE STATES: " + states.getStateCount());
        }
//         else if (base.equals("prenormfromhist")) {
//             Boolean printNorm = cmd.nextBoolean();

//             int basisSize = basis.size();
//             double[] expectedValue = new double [basisSize];
//             for (Iterator<QueueState> i = stateHistogram.binIterator();
//                  i.hasNext(); ) {
//                 QueueState q = i.next();
//                 double w = stateHistogram.getBinFrequency(q);

//                 for (int j = 0; j < basisSize; j++) {
//                     double v = basis.getFunction(j).getValue(q);
//                     expectedValue[j] += w * v * v;
//                 }
//             }
//             prenorm = new double [basisSize];
//             for (int j = 0; j < basisSize; j++) {
//                 if (Math.abs(expectedValue[j]) < 1e-6)
//                     throw new IllegalStateException("unable to normalize "
//                                                     + "basis function "
//                                                     + basis.getFunction(j)
//                                                     .toString());
//                 prenorm[j] = 1.0 / expectedValue[j];
//             }

//             if (printNorm) {
//                 for (int j = 0; j < basisSize; j++)
//                     System.out.println("prenorm " 
//                                        + basis.getFunction(j).toString() 
//                                        + " = " 
//                                        + prenorm[j]);
//             }

//         }
        else if (base.equals("sample")) {
            String type = cmd.next();
            if (type.equals("exp")) {
                double gamma = cmd.nextDouble();
                QueueStateSymmetry symmetry = parseSymmetry(cmd.next());
                int sampleCount = cmd.nextInt();
                System.out.println("sampling exp " + sampleCount + " states");
                ExponentialSampler sampler = 
                    new ExponentialSampler(model, gamma, symmetry);
                sampler.sample(getRandom(), sampleCount);
                states = sampler.getStateList();
                weights = sampler.getWeights();
            }
            else if (type.equals("grid")) {
                double gamma = cmd.nextDouble();
                QueueStateSymmetry symmetry = parseSymmetry(cmd.next());
                int cutoff = cmd.nextInt();
                int sampleCount = cmd.nextInt();
                System.out.println("sampling grid " + sampleCount + " states");
                GridSampler sampler = 
                    new GridSampler(model, gamma, symmetry, cutoff);
                sampler.sample(getRandom(), sampleCount);
                states = sampler.getStateList();
                weights = sampler.getWeights();
            }
            else
                throw new RuntimeException("unknown sampling method " + type);

            verifyWeights();
            System.out.println("UNIQUE STATES: " + states.getStateCount());
        }           
        else if (base.equals("solver") 
                 || base.equals("buildsolver")) {
            boolean solve = base.equals("solver");
            String type = cmd.next();
            if (type.equals("optimal")) {
                System.out.println("solving using optimal solver");
                double alpha = cmd.nextDouble();
                solver = new OptimalPolicySolver(getCplexFactory(),
                                                 states,
                                                 model.getCostFunction(),
                                                 alpha);
            }
            else if (type.equals("approx")) {
                System.out.println("solving using approximate solver");
                double alpha = cmd.nextDouble();
                solver = new ApproximatePolicySolver(getCplexFactory(),
                                                     states,
                                                     model.getCostFunction(),
                                                     weights,
                                                     basis,
                                                     alpha);
            }
            else if (type.equals("approxslack")) {
                System.out.println("solving using approximate solver "
                                   + "with slack");
                double alpha = cmd.nextDouble();
                double eta = cmd.nextDouble();
                StateFunction slackFunction = 
                    new StateFunction() {
                        public double getValue(State state) {
                            QueueState qState = (QueueState) state;
                            int queueCount = qState.getQueueCount();
                            double sum = 1.0;
                            for (int i = 0; i < queueCount; i++) {
                                int q = qState.getQueueLength(i);
                                sum += q*q;
                            }
                            sum /= (double) queueCount;
                            return sum;
                        }
                    };
                solver = 
                    new ApproximateSlackPolicySolver(getCplexFactory(),
                                                     states,
                                                     model.getCostFunction(),
                                                     weights,
                                                     basis,
                                                     alpha,
                                                     eta,
                                                     slackFunction);
            }
            else if (type.equals("approxnc")) {
                System.out.println("solving using non-convex "
                                   + "approximate solver");
                double alpha = cmd.nextDouble();
                solver = 
                    new ApproximateNonConvexPolicySolver(getCplexFactory(),
                                                         states,
                                                         model
                                                         .getCostFunction(),
                                                         weights,
                                                         basis,
                                                         alpha);
            }
            else if (type.equals("td")) {
                System.out.println("solving using TD "
                                   + " solver");
                double alpha = cmd.nextDouble();
                double gammaA = cmd.nextDouble();
                double gammaB = cmd.nextDouble();
                double lambda = cmd.nextDouble();
                int timeStepCount = cmd.nextInt();
                int timeReportCount = cmd.nextInt();
                solver = 
                    new TDSolver(model,
                                 model.getCostFunction(),
                                 policyFactory,
                                 basis,
                                 getRandom(),
                                 gammaA,
                                 gammaB,
                                 lambda,
                                 timeStepCount,
                                 timeReportCount,
                                 alpha);
            }                
            else if (type.equals("kalman")) {
                System.out.println("solving using TD Kalman Filter "
                                   + " solver");
                double alpha = cmd.nextDouble();
                double gammaA = cmd.nextDouble();
                double gammaB = cmd.nextDouble();
                int timeStepCount = cmd.nextInt();
                int timeReportCount = cmd.nextInt();
                solver = 
                    new KalmanFilterSolver(model,
                                           model.getCostFunction(),
                                           policyFactory,
                                           basis,
                                           getRandom(),
                                           gammaA,
                                           gammaB,
                                           timeStepCount,
                                           timeReportCount,
                                           alpha);
            }                
            else 
                throw new Exception("unknown solver: " + type);

            if (solve) {
                System.out.println("starting solution");

                PrintStream debugOut = null;
                if (debugIter > 0) {
                    if (solver instanceof TDSolver) {
                        debugOut = openOutput(debugIterFile);
                        ((TDSolver) solver).setDebug(debugOut, debugIter);
                    }
                    else if (solver instanceof KalmanFilterSolver) {
                        debugOut = openOutput(debugIterFile);
                        ((KalmanFilterSolver) solver)
                            .setDebug(debugOut, debugIter);
                    }
                }
                else {
                    if (solver instanceof TDSolver) 
                        ((TDSolver) solver).setDebug(null, 0);
                    else if (solver instanceof KalmanFilterSolver) 
                        ((KalmanFilterSolver) solver)
                            .setDebug(null, 0);
                }

                if (solver.solve()) 
                    System.out.println("solution successful");
                else {
                    //System.out.println("solution unsuccessful");
                    throw new IllegalStateException("solution unsuccessful");
                }

                if (debugOut != null)
                    closeOutput(debugOut);
                
                policy = policyFactory.getPolicy(solver.getValueEstimate());
            }
        }
        else if (base.equals("value")
                 || base.equals("fullvalue")) {
            boolean fullValue = base.equals("fullvalue");
            System.out.println("computing value function");
            double alpha = cmd.nextDouble();
            PrintStream out = null;
            if (fullValue)
                out = openOutput(cmd.next());

            ValueFunctionCalculator calc = 
                new ValueFunctionCalculator(getCplexFactory(),
                                            states,
                                            model.getCostFunction(),
                                            policy,
                                            alpha);
            calc.solve();
            
            StateFunction value = calc.getValueFunction();

            State state0 = model.getBaseState();
            System.out.println("STATE: "
                               + state0.toString() 
                               + " VALUE: " 
                               + calc.getValue(state0));

            if (fullValue) {
		for (Iterator<State> i = states.getSortedIterator(comparator);
		     i.hasNext(); ) {
		    State state = i.next();
                    out.println("STATE: "
                                + state.toString() 
                                + " VALUE: " 
                                + value.getValue(state));
                }
                closeOutput(out);
            }

        }
        else if (base.equals("value_estimate")) {
            System.out.println("dumping value estimate");

            PrintStream out = openOutput(cmd.next());
            
            StateFunction value = solver.getValueEstimate();

	    for (Iterator<State> i = states.getSortedIterator(comparator);
		 i.hasNext(); ) {
		State state = i.next();
                out.println("STATE: "
                            + state.toString() 
                            + " VALUE ESTIMATE: " 
                            + value.getValue(state));
            }
            closeOutput(out);
        }
        else if (base.equals("simvalue")) {
            System.out.println("simulating average cost");

            int time = cmd.nextInt();
            int printFreq = cmd.nextInt();
            StateFunction costFunction = model.getCostFunction();
            MVSampleStatistics stats = new MVSampleStatistics();
            StateCache cache = new StateCache(model, 256);
            PathSimulator sim =  new PathSimulator(model,
                                                   policy,
                                                   cache,
                                                   getRandom(),
                                                   256);

            for (int t = 1; t <= time; t++) {
                QueueState state = (QueueState) sim.next();
                stats.addSample(costFunction.getValue(state));
                if (t % printFreq == 0)
                    System.out.println("TIME: "
                                       + t
                                       + " COST: "
                                       + costFunction.getValue(state)
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

            StateFunction costFunction = model.getCostFunction();
            StateCache cache = new StateCache(model, 256);
            PathSimulator sim =  new PathSimulator(model,
                                                   policy,
                                                   cache,
                                                   null,
                                                   256);
            MonteCarloStateObserver observer = null;
            if (computeStateHist) {
                stateHistogram.clearAll();
                observer = new MonteCarloStateObserver() {
                        public void observe(int path, int time, State state) {
                            QueueState qs = new QueueState((QueueState) state);
                            stateHistogram.add(qs);
                        }
                    };
            }

            MonteCarloEvaluator mce = 
                new MonteCarloEvaluator(sim, 
                                        getRandom(),
                                        costFunction,
                                        time,
                                        paths,
                                        observer);

            SampleStatistics stats = mce.getSampleStatistics();
            while (mce.hasNext()) {
                double value = mce.next();
                System.out.println("PATH: " + stats.getCount()
                                   + " VALUE: " + value
                                   + " RUNNING AVG: " + stats.getMean());
            }
            double stddev = stats.getCount() > 1 
                ? stats.getStandardDeviation()
                : 0.0;
            System.out.println("MEAN: " + stats.getMean()
                               + " STDDEV: " + stddev
                               + " ERROR: " 
                               + stddev/Math.sqrt(stats.getCount()));

        }
        else if (base.equals("comparepolicy")) {        
            PrintStream out = openOutput(cmd.next());
	    double tolerance = cmd.nextDouble();
            System.out.println("comparing computed policies");
	    for (Iterator<State> i = states.getSortedIterator(comparator);
		 i.hasNext(); ) {
		State state = i.next();
                StateInfo info = states.getStateInfo(state);
		BitSet mask = policy.getOptimalActionMask(state,
                                                          info,
                                                          tolerance);
		BitSet maskRef = refPolicy.getOptimalActionMask(state,
                                                                info,
                                                                tolerance);
                BitSet diff = new BitSet();
                diff.or(mask);
                diff.xor(maskRef);

                if (!diff.isEmpty()) {
                    out.print("STATE: " + state.toString() + " ACTION:");
                    for (int a = diff.nextSetBit(0); 
                         a >= 0; 
                         a = diff.nextSetBit(a+1)) {
                        out.print(mask.get(a) ? " +" : " -");
                        out.print(info.getAction(a).toString());
                    }
                    out.println();
                }
            }
            closeOutput(out);
        }
        else if (base.equals("printpolicy")) {
            PrintStream out = openOutput(cmd.next());
	    double tolerance = cmd.nextDouble();

            System.out.println("dumping computed policy");
	    for (Iterator<State> i = states.getSortedIterator(comparator);
		 i.hasNext(); ) {
		State state = i.next();
                StateInfo info = states.getStateInfo(state);
		Action[] actions = policy.getOptimalActionList(state,
							       info,
							       tolerance);
                out.print("STATE: " + state.toString() + " ACTION:");
		for (int a = 0; a < actions.length; a++)
		    out.print(" " + actions[a].toString());
		out.println();
            }
            closeOutput(out);
        }
        else if (base.equals("dumpinfo")) {
            PrintStream out = openOutput(cmd.next());
            System.out.println("dumping solver info");
            solver.dumpInfo(out);
            closeOutput(out);
        }
	else if (base.equals("dumpsolverinfo")) {
            PrintStream out = openOutput(cmd.next());
            System.out.println("dumping detailed solver info");
            solver.dumpStateInfo(out);
            closeOutput(out);
	}
        else if (base.equals("dumpmodel")) {
            PrintStream out = openOutput(cmd.next());
            System.out.println("dumping model info");
            model.dumpInfo(out);
            closeOutput(out);
        }
        else if (base.equals("dumpstates")) {
            PrintStream out = openOutput(cmd.next());
            System.out.println("dumping states");
	    for (Iterator<State> i = states.getSortedIterator(comparator);
		 i.hasNext(); ) {
		State state = i.next();
		int s = states.getStateIndex(state);
                out.println("STATE: "
                            + state.toString() 
                            + " WEIGHT: " 
                            + Double.toString(weights[s]));
            }
            closeOutput(out);
        }
        else if (base.equals("loadstates")) {
            BufferedReader in = 
                new BufferedReader(new FileReader(cmd.next()));
            ArrayList<Pair<QueueState,Double>> list =
                new ArrayList<Pair<QueueState,Double>> ();
            Pattern pattern = 
                Pattern.compile("^STATE:\\s+(\\S+)\\s+WEIGHT:\\s+(\\S+)$");
            String line;
            while ((line = in.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (!m.matches())
                    throw new IllegalArgumentException("bad state line: " 
                                                       + line);
                String stateString = m.group(1);
                Double weight = new Double(m.group(2));
                list.add(new Pair<QueueState,Double> 
                         (new QueueState(stateString), weight));
            }
            in.close();
            
            int size = list.size();
            State[] stateArray = new State [size];
            weights = new double [size];
            for (int i = 0; i < size; i++) {
                Pair<QueueState,Double> p = list.get(i);
                stateArray[i] = p.getFirst();
                weights[i] = p.getSecond();
            }
            states = new StateList(model, stateArray);
            verifyWeights();

            System.out.println("UNIQUE STATES: " + states.getStateCount());
        }
        else if (base.equals("exportmodel")) {
            System.out.println("exporting solver model");
            solver.exportModel(cmd.next());
        }
        else if (base.equals("writemodel")) {
            String fileName = cmd.next();
            System.out.println("exporting network model to " + fileName);
            PrintStream out = openOutput(fileName);
            model.writeModel(out);
            closeOutput(out);
        }
        else if (base.equals("originvalue")) {
            System.out.println("dumping origin value function behavior");

            PrintStream out = openOutput(cmd.next());

            int count = cmd.nextInt();
            double[] value = new double [count+1];
            StateFunction valueFunction = solver.getValueEstimate();
            int[] q = new int [model.getQueueCount()];
            QueueState state = new QueueState(q);
            for (int i = 0; i <= count; i++) {
                q[0] = i;
                value[i] = valueFunction.getValue(state);
            }
            double d0 = value[1] - value[0];
            for (int i = 0; i < count; i++) {
                q[0] = i;
                out.println("STATE: "
                            + state.toString() 
                            + " VALUE DERIV: " 
                            + (value[i+1] - value[i])/d0);
            }
            closeOutput(out);

        }
        else  
            return false;

        return true;
    }

    protected void verifyWeights() {
        int size = weights.length;
        double total = 0.0;
        for (int i = 0; i < size; i++) {
            double w = weights[i];
            if (w < 0.0 || w > 1.0)
                throw new IllegalArgumentException("weights out of range");
            total += w;
        }
        if (Math.abs(total - 1.0) > 1e-6)
            throw new IllegalArgumentException("weights not normalized");
    }

    protected double[] parsePoly(final String s) {
        String[] split = s.split(",");
        double[] v = new double [split.length];
        for (int i = 0; i < v.length; i++)
            v[i] = Double.parseDouble(split[i]);
        return v;
    }

    protected QueueStateSymmetry parseSymmetry(final String s) {
        if (s.equals("total")) 
            return new TotalQueueStateSymmetry();
        if (s.equals("switch"))
            return new SwitchQueueStateSymmetry(model);
        if (s.equals("none")) 
            return null;
        throw new IllegalArgumentException("unknown symmetry: " + s);
    }



    public static void main(String[] argv) throws Exception {
        (new QueueingMain()).run(argv);
    }
}
