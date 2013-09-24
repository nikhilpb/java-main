package com.moallemi.avg;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;

public class AveragingMain {
    private String[] argv;
    private int index;
    private Graph graph;
    private Averager averager;
    private double[] yValues;
    private long seed = -1L;
    private boolean debug = false;

    public AveragingMain(String[] argv) { this.argv = argv; index = 0; }

    public boolean hasNext() {
        return index < argv.length;
    }

    public Random getRandom() {
	return new Random(seed >= 0L ? seed : System.currentTimeMillis());
    }

    public void processNext() throws Exception {
        int lastIndex = index;

	if (argv[index].equals("debug"))
	    debug = true;
        else if (argv[index].equals("graph")) {
	    String type = argv[++index];
	    if (type.equals("lattice")) {
		int nodesPerAxis = Integer.parseInt(argv[++index]);
		int dimension = Integer.parseInt(argv[++index]);
		int connectivity = Integer.parseInt(argv[++index]);
		boolean isTorus = 
		    Boolean.valueOf(argv[++index]).booleanValue();
		boolean isL1Norm = 
		    Boolean.valueOf(argv[++index]).booleanValue();
		graph = GraphFactory.buildLattice(nodesPerAxis, 
						  dimension,
						  connectivity,
						  isTorus,
						  isL1Norm);
	    }
	    else if (type.equals("lollipop")) {
		int nodeCount = Integer.parseInt(argv[++index]);
		graph = GraphFactory.buildLollipop(nodeCount);
	    }
	    else if (type.equals("squarediagonal")) {
		graph = GraphFactory.buildSquareWithDiagonal();
	    }
	    else if (type.equals("complete")) {
                int nodes = Integer.parseInt(argv[++index]);
		graph = GraphFactory.buildComplete(nodes);
	    }
	    else
		throw new IllegalArgumentException("unknown graph type: "
						   + type);
            System.out.println("nodes: " + graph.getNodeCount());
            System.out.println("edges: " + graph.getEdgeCount());
//             DFS dfs = (new DFS(graph, graph.getNode(0))).compute();
//             System.out.println("connected components: " 
//                                + dfs.getConnectedComponentCount());


        }
        else if (argv[index].equals("dumpgraph")) {
            graph.dumpInfo(System.out);
        }
        else if (argv[index].equals("seed")) {
	    seed = Long.parseLong(argv[++index]);
        }
        else if (argv[index].equals("sety")) {
	    boolean isZeroMean = 
		Boolean.valueOf(argv[++index]).booleanValue();
            int nodeCount = graph.getNodeCount();

            yValues = new double [nodeCount];
            String type = argv[++index];

            if (type.equals("normal")) {
                Random random = getRandom();
                for (int i = 0; i < nodeCount; i++)
                    yValues[i] = random.nextGaussian();
            }
            else if (type.equals("unit")) 
                yValues[0] = Math.sqrt(nodeCount);
            else
                throw new IllegalArgumentException("unknown y init: " 
                                                   + type);
            MVSampleStatistics stats = new MVSampleStatistics();

	    if (isZeroMean) {
		for (int i = 0; i < nodeCount; i++)
		    stats.addSample(yValues[i]);
		double mean = stats.getMean();
		for (int i = 0; i < nodeCount; i++)
		    yValues[i] -= mean;
		stats.clear();
	    }
		
            for (int i = 0; i < nodeCount; i++)
                stats.addSample(yValues[i]);
            System.out.println("true mean: " + stats.getMean());
        }
        else if (argv[index].equals("algorithm")) {
            String type = argv[++index];
            if (type.equals("pairwise"))
                averager = new PairwiseAverager(graph, yValues);
            else if (type.equals("cp")) {
		String betaStr = argv[++index];
		double beta = betaStr.equals("inf")
		    ? Double.MAX_VALUE
		    : Double.parseDouble(betaStr);
                averager = new ConsensusPropagationAverager(graph,
                                                            yValues,
                                                            beta);
            }
            else if (type.equals("cpm")) {
                double beta = Double.parseDouble(argv[++index]);
                averager = new ConsensusPropagationMeanAverager(graph,
								yValues,
								beta);
            }
            else if (type.equals("attcp")) {
		String betaStr = argv[++index];
		double beta = betaStr.equals("inf")
		    ? Double.MAX_VALUE
		    : Double.parseDouble(betaStr);
                averager = new AttenuatedCPAverager(graph,
                                                    yValues,
                                                    beta);
            }
            else 
                throw new IllegalArgumentException("unknown algorithm: "
                                                   + type);
        }
        else if (argv[index].equals("run")) {
            int nodeCount = graph.getNodeCount();
            int totalIter = Integer.parseInt(argv[++index]);
            int printIter = Integer.parseInt(argv[++index]);

            averager.reset();
            int s = 0;
            int t = 0;
            
            DecimalFormat df = new DecimalFormat("0.0000");
            for (t = 0; t < totalIter; t++) {
                if (t % printIter == 0) {
                    if (debug) {
                        if (averager 
                            instanceof ConsensusPropagationAverager) {
                            ConsensusPropagationAverager cpAvg =
                                (ConsensusPropagationAverager) averager;
                            cpAvg.dumpInfo(System.out);
                        }
                        else if (averager 
                            instanceof AttenuatedCPAverager) {
                            AttenuatedCPAverager cpAvg =
                                (AttenuatedCPAverager) averager;
                            cpAvg.dumpInfo(System.out);
                        }
                    }


                    System.out
                        .print("t: " + t
                               + " " + df.format(averager.getConvergenceError())
                               + " " + df.format(averager.getTrueError())
                               );
                    if (averager 
                        instanceof ConsensusPropagationAverager) {
                        ConsensusPropagationAverager cpAvg =
                            (ConsensusPropagationAverager) averager;
                        System.out
                            .print(" " + df.format(cpAvg.getKMean())
                                   + " " + df.format(cpAvg.getKStdDev())
                                   + " " + df.format(cpAvg.getMuMean())
                                   + " " + df.format(cpAvg.getMuStdDev())
				   );
                    }
                    System.out.println();
                }

                averager.iterate();
            }

            System.out
                .print("t: " + t
                       + " " + df.format(averager.getConvergenceError())
                       + " " + df.format(averager.getTrueError())
                       );
            if (averager 
                instanceof ConsensusPropagationAverager) {
                ConsensusPropagationAverager cpAvg =
                    (ConsensusPropagationAverager) averager;
                System.out
                    .print(" " + df.format(cpAvg.getKMean())
                           + " " + df.format(cpAvg.getKStdDev())
                           + " " + df.format(cpAvg.getMuMean())
                           + " " + df.format(cpAvg.getMuStdDev())
                           );
            }
            System.out.println();
        }
        else if (argv[index].equals("relaxtime")) {
            int nodeCount = graph.getNodeCount();
            int maxIter = Integer.parseInt(argv[++index]);
            int tolCount = Integer.parseInt(argv[++index]);
	    double[] tolerance = new double [tolCount];
	    for (int i = 0; i < tolCount; i++)
		tolerance[i] = Double.parseDouble(argv[++index]);
	    double[] tolStats = new double [tolCount];
            Arrays.fill(tolStats, -1.0);
            
            averager.reset();
            int t;
            for (t = 0; t < maxIter; t++) {
                double error = averager.getTrueError();
                
                boolean moreLeft = false;
                for (int i = 0; i < tolCount; i++) {
                    if (tolStats[i] >= 0.0)
                        continue;
                    if (error < tolerance[i]) 
                        tolStats[i] = t;
                    else
                        moreLeft = true;
                }
            
                if (!moreLeft)
                    break;
            
                averager.iterate();
		    
                t++;
            }

            DecimalFormat df = new DecimalFormat("0.0000");
            DecimalFormat df2 = new DecimalFormat("0.0");
            for (int i = 0; i < tolCount; i++) {
                System.out.print("tolerance: " + df.format(tolerance[i]));
                System.out.print(" mean relaxation time: ");
                if (tolStats[i] < 0.0)
                    System.out.print(">" + df2.format(maxIter));
                else 
                    System.out.print(df2.format(tolStats[i]));
                System.out.println();
            }
            
        }
        else
            throw new IllegalArgumentException("unknown command: " 
                                               + argv[index]);

        index++;

        System.out.print(">>");
        for (int i = lastIndex; i < index; i++)
            System.out.print(" " + argv[i]);
        System.out.println();
    }


    public static void main(String[] argv) throws Exception {
        AveragingMain m = new AveragingMain(argv);
        System.out.print(">>>");
        for (int i = 0; i < argv.length; i++)
            System.out.print(" " + argv[i]);
        System.out.println();

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
