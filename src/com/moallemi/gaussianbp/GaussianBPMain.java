package com.moallemi.gaussianbp;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.graph.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;

public class GaussianBPMain {
    private String[] argv;
    private int index;

    private Graph graph;
    private FactoredGaussian dist;
    private GaussianBP model;

    private long seed = -1L;
    private boolean debug = false;

    public GaussianBPMain(String[] argv) { this.argv = argv; index = 0; }

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
        else if (argv[index].equals("model")) {
	    String type = argv[++index];
	    if (type.equals("johnson")) {
                double rho = Double.parseDouble(argv[++index]);

                graph = new Graph();
                dist = new FactoredGaussian(4);
                graph.beginModification();
                graph.addNode(new Node());
                dist.setSingle(0, new Gaussian1D(1.0, 1.0));
                graph.addNode(new Node());
                dist.setSingle(1, new Gaussian1D(1.0, 0.0));
                graph.addNode(new Node());
                dist.setSingle(2, new Gaussian1D(1.0, 0.0));
                graph.addNode(new Node());
                dist.setSingle(3, new Gaussian1D(1.0, 0.0));
                graph.addEdge(new Edge(graph.getNode(0), graph.getNode(1)));
                dist.setPairwise(0, 1, new Gaussian2D(0.0, -rho, 0.0, 
                                                      0.0, 0.0));
                graph.addEdge(new Edge(graph.getNode(1), graph.getNode(2)));
                dist.setPairwise(1, 2, new Gaussian2D(0.0, rho, 0.0, 
                                                      0.0, 0.0));
                graph.addEdge(new Edge(graph.getNode(2), graph.getNode(3)));
                dist.setPairwise(2, 3, new Gaussian2D(0.0, rho, 0.0, 
                                                      0.0, 0.0));
                graph.addEdge(new Edge(graph.getNode(3), graph.getNode(0)));
                dist.setPairwise(3, 0, new Gaussian2D(0.0, rho, 0.0,
                                                      0.0, 0.0));
                graph.addEdge(new Edge(graph.getNode(0), graph.getNode(2)));
                dist.setPairwise(0, 2, new Gaussian2D(0.0, rho, 0.0,
                                                      0.0, 0.0));
                graph.endModification();
                
                model = new GaussianBP(graph, dist);
	    }
	    else
		throw new IllegalArgumentException("unknown model type: "
						   + type);
            System.out.println("nodes: " + graph.getNodeCount());
            System.out.println("edges: " + graph.getEdgeCount());
        }
        else if (argv[index].equals("dumpgraph")) {
            graph.dumpInfo(System.out);
        }
        else if (argv[index].equals("seed")) {
	    seed = Long.parseLong(argv[++index]);
        }
        else if (argv[index].equals("isvalid")) {
            if (dist.isValid(graph)) 
                System.out.println("model is valid");
            else
                System.out.println("model is INVALID");
        }
        else if (argv[index].equals("run")) {
            int nodeCount = graph.getNodeCount();
            int totalIter = Integer.parseInt(argv[++index]);
            int printIter = Integer.parseInt(argv[++index]);

            model.reset();
            int s = 0;
            int t = 0;
            
            for (t = 0; t < totalIter; t++) {
                if (t % printIter == 0) {
                    System.out
                        .print("t: " + t);
                    
                    model.dumpInfo(System.out);
                }

                model.iterate();
            }
        }
        else 
            throw new Exception("unknown command: " + argv[index]);
        
        index++;
    }


    public static void main(String[] argv) throws Exception {
        GaussianBPMain m = new GaussianBPMain(argv);
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
