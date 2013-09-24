package com.moallemi.facloc;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.*;
import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;

public class FacilityLocationMain {
    private String[] argv;
    private int index;
    private FacilityLocationProblem[] problemList;
    private long seed = -1L;
    private boolean debug = false;
    private boolean forceConnected = false;
    private boolean testOpt = false;
    private boolean printOpt = false;

    public FacilityLocationMain(String[] argv) { this.argv = argv; index = 0; }

    public boolean hasNext() {
        return index < argv.length;
    }

    public Random getRandom() {
	return new Random(seed >= 0L ? seed : System.currentTimeMillis());
    }

    private DecimalFormat df = new DecimalFormat("0.00");
    private DecimalFormat df2 = new DecimalFormat("0.000000");
    public String format(double x) {
        return  x >= Double.MAX_VALUE ? "+inf" : df2.format(x);
    }

    public void processNext() throws Exception {
        int lastIndex = index;

	if (argv[index].equals("debug"))
	    debug = true;
	else if (argv[index].equals("testopt"))
	    testOpt = true;
	else if (argv[index].equals("printopt"))
	    printOpt = true;
	else if (argv[index].equals("forceconnected"))
            forceConnected = true;
        else if (argv[index].equals("seed")) 
	    seed = Long.parseLong(argv[++index]);
        else if (argv[index].equals("problem")) {
            String type = argv[++index];

            if (type.equals("discrete")) {
                String graphType = argv[++index];
                double radius = Double.parseDouble(argv[++index]);
                int problemCount = Integer.parseInt(argv[++index]);
                int nodeCount = Integer.parseInt(argv[++index]);
                double costMean = Double.parseDouble(argv[++index]);
            
                problemList = new FacilityLocationProblem [problemCount];
                Random baseRandom = getRandom();
                for (int i = 0; i < problemCount; i++) {
                    Random r = new Random(baseRandom.nextLong());
                    Graph graph;
                    if (graphType.equals("geo"))
                        graph 
                            = GraphFactory.buildGeometric2D(nodeCount,
                                                            radius,
                                                            r,
                                                            false);
                    else if (graphType.equals("geoavg"))
                        graph 
                            = GraphFactory.buildGeometric2DAvgDegree(nodeCount,
                                                                     radius,
                                                                     r);
                    else
                        throw new Exception("unknown graph type: " + graphType);
                    
                    if (forceConnected) {
                        DFS dfs = (new DFS(graph)).compute();
                        if (dfs.getConnectedComponentCount() > 1) {
                            Graph[] comp = dfs.buildConnectedComponents();
                            graph.beginModification();
                            for (int j = 1; j < comp.length; j++) {
                                Node n1 = 
                                    comp[0].getNode(r.nextInt(comp[0]
                                                              .getNodeCount()));
                                Node n2 = 
                                    comp[j].getNode(r.nextInt(comp[j]
                                                              .getNodeCount()));
                                graph.addEdge(new Edge(n1, n2));
                            }
                            graph.endModification();
                        }
                    }
                    Random r2 = new Random(baseRandom.nextLong());
                    double[] cost = new double [nodeCount];
                    for (int j = 0; j < nodeCount; j++) 
                        cost[j] = 
                            Distributions.nextExponential(r2, 1.0/costMean);
                    problemList[i] = new DiscreteFacilityLocationProblem(graph,
                                                                         cost);
                }
            }
            else if (type.equals("general")) {
                int problemCount = Integer.parseInt(argv[++index]);
                int nodeCount = Integer.parseInt(argv[++index]);
                double costMean = Double.parseDouble(argv[++index]);
                int closest = Integer.parseInt(argv[++index]);

                problemList = new FacilityLocationProblem [problemCount];
                Random baseRandom = getRandom();
                for (int p = 0; p < problemCount; p++) {
                    double[] x = new double [nodeCount];
                    double[] y = new double [nodeCount];
                    for (int i = 0; i < nodeCount; i++) {
                        do {
                            x[i] = 2.0*baseRandom.nextDouble() - 1.0;
                            y[i] = 2.0*baseRandom.nextDouble() - 1.0;
                        } while (x[i]*x[i] + y[i]*y[i] >= 1.0);
                    }
                    double[][] d = new double [nodeCount][nodeCount];
                    for (int i = 0; i < nodeCount; i++) {
                        d[i][i] = 0.0;
                        for (int j = 0; j < i; j++) {
                            double dx = x[i] - x[j];
                            double dy = y[i] - y[j];
                            d[i][j] = d[j][i] = Math.sqrt(dx*dx + dy*dy);
                        }
                    }
                    double[] cost = new double [nodeCount];
                    for (int j = 0; j < nodeCount; j++) 
                        cost[j] = 
                            Distributions.nextExponential(baseRandom, 
                                                          1.0/costMean);
                    problemList[p] 
                        = new GeneralFacilityLocationProblem(nodeCount,
                                                             nodeCount,
                                                             closest,
                                                             d, 
                                                             cost);
                }
            }
            else {
                throw new Exception("unknown type: " + type);
            }
        }
//         else if (argv[index].equals("modelstats")) {
//             for (int i = 0; i < problemList.length; i++) {
//                 double degree = problemList[i].getGraph().getAverageDegree();
//                 boolean isConnected 
//                     = problemList[i].getAllPairsShortestPaths().isConnected();
//                 int maxDist 
//                     = problemList[i].getAllPairsShortestPaths()
//                     .getMaximumPathLength();
//                 System.out.println("problem: " + (i+1)
//                                    + " deg: " + format(degree)
//                                    + " conn: " + isConnected
//                                    + " diam: " + maxDist);
//                 if (debug)
//                     problemList[i].getGraph().dumpInfo(System.out);
//             }
//         }
        else if (argv[index].equals("loadmodels")) {
            int cnt = Integer.parseInt(argv[++index]);
            problemList = new FacilityLocationProblem [cnt];
            for (int i = 0; i < cnt; i++) {
                String fname = argv[++index];
                BufferedReader in = new BufferedReader(new FileReader(fname));
                problemList[i] = FacilityLocationProblemIO.readModel(in);
                in.close();
            }
        }
        else if (argv[index].equals("dumpmodel")) {
            String prefix = argv[++index];
            for (int i = 0; i < problemList.length; i++) {
                PrintWriter out;
                if (prefix.equals("-")) 
                    out = new PrintWriter(System.out, true);
                else 
                    out = 
                        new PrintWriter(new 
                                        BufferedWriter(new 
                                                       FileWriter(prefix + "-" + (i+1) + ".txt")));
                FacilityLocationProblemIO.writeModel(problemList[i], out);
                if (prefix.equals("-"))
                    out.flush();
                else
                    out.close();

            }
        }
        else if (argv[index].equals("cplexsolve")) {
            CplexFactory factory = new CplexFactory();
            TicTocTimer t = new TicTocTimer();
            for (int i = 0; i < problemList.length; i++) {
                CplexSolver solver = new CplexSolver(problemList[i],
                                                     factory);
                t.tic();
                boolean status = solver.solve();
                double time = t.toc();

                System.out.print("CPLEX problem: " + (i+1));
                System.out.print(" obj: " + format(solver.getObjectiveValue()));
                System.out.print((solver.isGlobalOptimum() ? " *" : " x"));
                System.out.print(" t2: " + format(time) + " (s)");


                if (testOpt) {
                    LocalOptimumTester optTester 
                        = new LocalOptimumTester(problemList[i]);
                    optTester
                        .setOptimalFacilities(solver
                                              .getOptimalFacilities());
                    
                    if (optTester.isLocalOptimumAddFacility())
                        System.out.print(" A");
                    if (optTester.isLocalOptimumDropFacility())
                        System.out.print(" D");
                    if (optTester.isLocalOptimumSwapFacility())
                        System.out.print(" S");
                    if (optTester.isLocalOptimumSwap2Facility())
                        System.out.print(" S2");
                }

                System.out.println();
                
                if (printOpt)
                    System.out.println(">>>> " + 
                                       solver.getOptimalFacilitiesString());
                
            }
        }
        else if (argv[index].equals("solve")) {
            String type = argv[++index];
            int maxDist = 0;
            if (type.equals("discrete"))
                maxDist = Integer.parseInt(argv[++index]);
            double damp = 0.0;
            if (type.equals("bipartite") 
                || type.equals("att") 
                || type.equals("datt") 
                || type.equals("trw") 
                || type.equals("strw") 
                || type.equals("satt") 
                || type.equals("sbipartite")
                || type.equals("fsbipartite"))
                damp = Double.parseDouble(argv[++index]);
            int minTreeCount = 0;
            if (type.equals("strw"))
                minTreeCount = Integer.parseInt(argv[++index]);
            int maxIterCount = Integer.parseInt(argv[++index]);
            double tolerance = Double.parseDouble(argv[++index]);

            double[] obj = new double [maxIterCount];
            double[] cumTime = new double [maxIterCount];
            boolean[] globalOpt = new boolean [maxIterCount];
            TicTocTimer t = new TicTocTimer();
            Random baseRandom = getRandom();
            for (int i = 0; i < problemList.length; i++) {
                MinSumSolver solver;
                String name;
                if (type.equals("discrete")) {
                    solver = new FLMinSumSolver(problemList[i],
                                                maxDist);
                    name = "MINSUM";
                }
                else if (type.equals("bipartite")) {
                    solver = new BipartiteFLMinSumSolver(problemList[i],
                                                         damp);
                    name = "MINSUM";
                }
                else if (type.equals("sbipartite")) {
                    solver = new SparseBipartiteFLMinSumSolver(problemList[i],
                                                               damp);
                    name = "MINSUM";
                }
                else if (type.equals("fsbipartite")) {
                    solver = 
                        new FastSparseBipartiteFLMinSumSolver(problemList[i],
                                                              damp);
                    name = "MINSUM";
                }
                else if (type.equals("trw")) {
                    solver = new BipartiteFLTRWMinSumSolver(problemList[i],
                                                            damp);
                    name = "TRW_MINSUM";
                }
                else if (type.equals("strw")) {
                    Random r = new Random(baseRandom.nextLong());
                    solver = 
                        new SparseBipartiteFLTRWMinSumSolver(problemList[i],
                                                             damp,
                                                             minTreeCount,
                                                             r);
                    name = "TRW_MINSUM";
                }
                else if (type.equals("att")) {
                    solver = new BipartiteFLAttMinSumSolver(problemList[i],
                                                            damp);
                    name = "AMINSUM";
                }
                else if (type.equals("datt")) {
                    solver = new BipartiteFLDecAttMinSumSolver(problemList[i],
                                                               damp);
                    name = "ADMINSUM";
                }
                else if (type.equals("satt")) {
                    solver = 
                        new SparseBipartiteFLAttMinSumSolver(problemList[i],
                                                             damp);
                    name = "AMINSUM";
                }
                else
                    throw new Exception("unknown type: " + type);
                
//                 if (debug 
//                     && solver instanceof SparseBipartiteFLTRWMinSumSolver) {
//                     ((SparseBipartiteFLTRWMinSumSolver) solver)
//                         .dumpRho(System.out);
//                 }

                LocalOptimumTester optTester = null;
                if (testOpt) 
                    optTester = new LocalOptimumTester(problemList[i]);

                int iter;
                double min = Double.MAX_VALUE;
                boolean foundGlobal = false;
                double lastTime = 0.0;
                for (iter = 0; iter < maxIterCount; iter++) {
                    t.tic();
                    solver.iterate();                    
                    lastTime += t.toc();
                    cumTime[iter] = lastTime;
                    obj[iter] = solver.getObjectiveValue();
                    globalOpt[iter] = solver.isGlobalOptimum();
                    if (globalOpt[iter]) 
                        foundGlobal = true;
                    double error = solver.getBellmanError();

                    if (obj[iter] < min)
                        min = obj[iter];
                    
                    if (debug) {
                        System.out.print((i+1) + "-" + (iter+1) + ": "
                                         + format(obj[iter])
                                         + " "
                                         + (globalOpt[iter] ? "*" : "x")
                                         + " " 
                                         + df2.format(error));
                        if (testOpt) {
                            optTester
                                .setOptimalFacilities(solver
                                                      .getOptimalFacilities());

                            if (optTester.isLocalOptimumAddFacility())
                                System.out.print(" A");
                            if (optTester.isLocalOptimumDropFacility())
                                System.out.print(" D");
                            if (optTester.isLocalOptimumSwapFacility())
                                System.out.print(" S");
                            if (optTester.isLocalOptimumSwap2Facility())
                                System.out.print(" S2");
                        }

                        System.out.println();
                        
                        if (printOpt)
                            System.out.println(">>>> " + 
                                               solver.getOptimalFacilitiesString());
                    }

                    if (error < tolerance) {
                        if (solver instanceof BipartiteFLDecAttMinSumSolver) {
                            BipartiteFLDecAttMinSumSolver s = 
                                (BipartiteFLDecAttMinSumSolver) solver;
                            if (!s.isFullyDecimated())
                                s.decimate();
                            else
                                break;
                        }
                        else
                            break;
                    }
                }
                int max = iter >= maxIterCount ? iter - 1 : iter;
                int last;
                for (last = 0; last <= max && obj[last] > min; last++)
                    ;

                System.out.print(name + " problem: " + (i+1));
                System.out.print(" obj: " + format(min));
                System.out.print((foundGlobal ? " *" : " x"));
                System.out.print(" t2: " + format(cumTime[max]) + " (s)");
                System.out.print(" i2: " + (max+1));
                System.out.print(" t1: " + format(cumTime[last]) + " (s)");
                System.out.print(" i1: " + (last+1));
                System.out.print(" err: " 
                                 + df2.format(solver.getBellmanError()));

                if (testOpt) {
                    optTester
                        .setOptimalFacilities(solver
                                              .getOptimalFacilities());
                    
                    if (optTester.isLocalOptimumAddFacility())
                        System.out.print(" A");
                    if (optTester.isLocalOptimumDropFacility())
                        System.out.print(" D");
                    if (optTester.isLocalOptimumSwapFacility())
                        System.out.print(" S");
                    if (optTester.isLocalOptimumSwap2Facility())
                        System.out.print(" S2");
                }
                    
                if (solver instanceof BipartiteFLAttMinSumSolver)
                    System.out.print(" sm: " 
                                     + format(solver.getSumNorm()
                                              /((double) problemList[i]
                                                .getCityCount())));
                System.out.println();                
            }
        }
        else 
            throw new Exception("unknown command: " + argv[index]);
       

        ++index;
        System.out.print(">>>");
        for (int i = lastIndex; i < index; i++)
            System.out.print(" " + argv[i]);
        System.out.println();
    }

    public static void main(String[] argv) throws Exception {
        FacilityLocationMain m = new FacilityLocationMain(argv);

        TicTocTimer t = new TicTocTimer();
        while (m.hasNext()) {
            t.tic();
            m.processNext();
            t.toc();
            t.tocPrint();
        }
        
        System.exit(0);
    }

}