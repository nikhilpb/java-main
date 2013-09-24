package com.moallemi.contresalloc;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.*;
import com.moallemi.math.stats.*;
import com.moallemi.util.*;
import com.moallemi.minsum.*;
import com.moallemi.resalloc.*;

public class CRCMain extends CommandLineMain {
    private ContRateControlProblem[] problemList;
    private boolean printOpt = false;
    private boolean noTimes = false;

    private DecimalFormat df = new DecimalFormat("0.00");
    private DecimalFormat df2 = new DecimalFormat("0.000000");
    private DecimalFormat df3 = new DecimalFormat("0.####E0");
    private CommandLineIterator cmd;
    private int problemCount;
    
    protected boolean processCommand(CommandLineIterator c) 
        throws Exception
    {
	cmd = c;
        String base = cmd.next();
	
        if (base.equals("printopt"))
            printOpt = true;
	
        else if (base.equals("notimes"))
            noTimes = true;
	
        else if (base.equals("problem")) {
	    doParseProblem();
        }
	
	else if (base.equals("solve")) {
	    doSolve();
	}

        else {
            return false;
	}
	
        return true;
    }
    
    
    private void doRegularGraphType(Random baseRandom1)
    {
	int userCount = cmd.nextInt();
	double alpha = cmd.nextDouble(); //ratio between link/user
	int lDegree = cmd.nextInt();
	int linkCount = 
	    (int) Math.round(((double)userCount * alpha));
	
	problemList = new ContRateControlProblem [problemCount];
	for (int i = 0; i < problemCount; i++) {
	    Random r = getChildRandom(baseRandom1);
	    problemList[i] = 
		new ContRateControlProblem(userCount, linkCount );
	    FactorGraphFactory
		.buildRandomRegularGraphByFactor(r,
						 problemList[i],
						 lDegree);
	}
    }



    private void doSolve(){
        IterativeLogUtilSolver solver;
        int maxIterCount = cmd.nextInt();     
        int printInterval = cmd.nextInt();
        String solverType = cmd.next();

        if (solverType.equals("minsum")) {
            double messageDamp = cmd.nextDouble();
            double initialOptPtAlpha = cmd.nextDouble();
            double gradNormTolerance = cmd.nextDouble();
            double decreasingStep_a = cmd.nextDouble();
            double decreasingStep_b = cmd.nextDouble();

            solver = new CRCMinSumSolver(messageDamp,
                                         initialOptPtAlpha,
                                         gradNormTolerance,
                                         decreasingStep_a,
                                         decreasingStep_b);
        }
        else if (solverType.equals("gradient") || solverType.equals("jacobi")) {
            double initialOptPtAlpha = cmd.nextDouble();
            double gradNormTolerance = cmd.nextDouble();
            double decreasingStep_a = cmd.nextDouble();
            double decreasingStep_b = cmd.nextDouble();

            solver = solverType.equals("gradient")
                ? new GradientDescentSolver(initialOptPtAlpha,
                                            gradNormTolerance,
                                            decreasingStep_a,
                                            decreasingStep_b)
                : new JacobiSolver(initialOptPtAlpha,
                                   gradNormTolerance,
                                   decreasingStep_a,
                                   decreasingStep_b);
        }
        else 
            throw new IllegalArgumentException("unknown solver type: " 
                                               + solverType);

	TicTocTimer t = new TicTocTimer();
	for (int i = 0; i < problemList.length; i++) {
            solver.setProblem(problemList[i]);

	    int iter;
	    double max = Double.NEGATIVE_INFINITY;
	    double lastTime = 0.0;
            double maxTime = 0.0;
            int maxIter = -1;
	    for (iter = 0; iter < maxIterCount; iter++) {
		t.tic();
		solver.iterate();            
		lastTime += t.toc();

		ContRateControlSolution solution 
                    = solver.getSolution();
		double obj = solution.getObjectiveValue();
		
		if (obj > max) {
                    max = obj;
                    maxTime = lastTime;
                    maxIter = iter;
		}

		if (printInterval > 0 && iter % printInterval == 0) {
		    System.out
                        .print((i+1) + "-" + (iter+1) + ": "
                               + df2.format(obj)
                               + " "
                               + df2.format(solver
                                           .getGradientNormAtOperatingPoint())
                               + " "
                               + df3.format(solver
                                            .getOperatingPointChangeNorm()));
                    if (solver instanceof CRCMinSumSolver)
                        System.out
                            .print(" "
                                   + df2.format(((CRCMinSumSolver) solver)
                                                .getBellmanError()));
		    System.out.println();
		    
		    if(printOpt)
			System.out.println(">>>> " + 
					   solution.toString());
		}
		
		if (solver.hasConverged())
		    break;
	    }
	    
	    System.out.print(solverType + " problem: " + (i+1));
	    System.out.print(" obj: " + df2.format(max));
	    if (!noTimes)
		System.out.print(" t2: " + df.format(lastTime) 
				 + " (s)");
	    System.out.print(" i2: " + (iter+1));
	    if (!noTimes)
		System.out.print(" t1: " + df.format(maxTime) 
				 + " (s)");
	    System.out.print(" i1: " + (maxIter+1));
	    System.out.print(" err: " 
			     + df2.format(solver
                                          .getGradientNormAtOperatingPoint()));
            if (solver instanceof CRCMinSumSolver)
                System.out.print(" "
                                 + df2.format(((CRCMinSumSolver) solver)
                                              .getBellmanError()));

            System.out.print(solver.hasConverged() ? " *" : " X");
                             
	    System.out.println();                
	}
    }
    
    private void doParseProblem()
	throws Exception
    {
	problemCount = cmd.nextInt();
	Random baseRandom0 = getRandom();

	doParseGraphType(baseRandom0);
	
	doParseUtilityType(baseRandom0);
	
	doParseCapacityType();
	
	
	//set Beta's for the problems   
	double beta = cmd.nextDouble();
	for (int i = 0; i < problemCount; i++){
	    problemList[i].setBarrierCoefficient(beta);
	}
    }

    private void doParseGraphType(Random baseRandom0)
	throws Exception
    {
	String graphType = cmd.next();
	Random baseRandom1 = getChildRandom(baseRandom0);
	if (graphType.equals("regular")) {
	    doRegularGraphType(baseRandom1);
	}
	else if (graphType.equals("single")) {
	    int userCount = cmd.nextInt();
	    problemList = new ContRateControlProblem [problemCount];
	    for (int i = 0; i < problemCount; i++) {
		problemList[i] = 
		    new ContRateControlProblem(userCount,
					       1);
		FactorGraphFactory
		    .buildCompleteGraph(problemList[i]);
	    }
	}
	else
	    throw new Exception("unknown graph type: " 
				+ graphType);
    }

    private void doParseUtilityType(Random baseRandom0)
	throws Exception
    {
	String utilType = cmd.next();
	Random baseRandom2 = getChildRandom(baseRandom0);
	if (utilType.equals("exp")) {
	    double utilMean = cmd.nextDouble();
	    for (int i = 0; i < problemCount; i++) {
		ContRateControlProblem p = problemList[i];
		int userCount = p.getVariableCount();
		Random r = getChildRandom(baseRandom2);
		for (int u = 0; u < userCount; u++)
		    p.setUserUtility(u, 
				     Distributions
				     .nextExponential(r,
						      1.0/utilMean));
	    }
	}
	else 
	    throw new Exception("unknown utility type: " 
				+ utilType);
    }
    
    private void doParseCapacityType()
	throws Exception
    {
	String capacityType = cmd.next();
	if (capacityType.equals("fixed")) {
	    double capacity = cmd.nextDouble();
	    for (int i = 0; i < problemCount; i++) {
		ContRateControlProblem p = problemList[i];
		int linkCount = p.getFactorCount();
		for (int l = 0; l < linkCount; l++)
		    p.setLinkCapacity(l, capacity);
	    }
	}
	else
	    throw new Exception("unknown capacity type: " 
				+ capacityType);
    }

   
    public static void main(String[] argv) throws Exception {
        (new CRCMain()).run(argv);
    }
    
}
