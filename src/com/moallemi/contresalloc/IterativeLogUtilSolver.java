package com.moallemi.contresalloc;

import com.moallemi.minsum.*;
import com.moallemi.math.optim.*;

/* Class: IterativeLogUtilSolver 
 * -------------------------------
 * Place holder class for solvers that
 * solve continuous resource allocation problem.
 * NOTE: The update formulas and inOptimalRange()
 * are still utility-function-specific 
 * (ui = wi*ln(xi))
 * Have to make this more portable later.
 *
 */

public abstract class IterativeLogUtilSolver 
    implements ContRateControlIterativeSolver 
{

    //ivars
    protected ContRateControlProblem problem;
    protected double initialOptPtAlpha;
    protected double gradNormTolerance;
    protected String stepSizeOption;
    protected double decreasingStep_a;
    protected double decreasingStep_b;
    protected double constStepSize;

    protected int userCount;
    protected int linkCount;
    
    protected ContRateControlSolution operatingPt;
    protected ContRateControlSolution nextOperatingPt;
    protected double[] operatingPtIncrement;
    protected double operatingPtChangeNorm;

    protected double[] gradientAtOperatingPt;
    protected double[] diagHessianAtOperatingPt;
    protected double gradientNormAtOperatingPt;

    private double[] minConnectedLinkCap;
    
    protected int numIteration;
    protected static final double EPSILON_2 = 1e-8;
    
    //methods:
    
    public IterativeLogUtilSolver(double initialOptPtAlpha,
                                  double gradNormTolerance,
				  double decreasingStep_a,
				  double decreasingStep_b)
    {
	
        this.problem = null;
	this.initialOptPtAlpha = initialOptPtAlpha;
	this.gradNormTolerance = gradNormTolerance;
        this.decreasingStep_a = decreasingStep_a;
        this.decreasingStep_b = decreasingStep_b;
	
    }

    public void setProblem(ContRateControlProblem problem) {
        this.problem = problem;

	userCount = problem.getVariableCount();
        linkCount = problem.getFactorCount();
	operatingPt = new ContRateControlSolution(problem);
	nextOperatingPt = new ContRateControlSolution(problem);
        operatingPtIncrement = new double[userCount];
	gradientAtOperatingPt = new double[userCount];
        gradientNormAtOperatingPt = 0.0;
	diagHessianAtOperatingPt = new double[userCount];
	minConnectedLinkCap = new double[userCount];

	numIteration = 0;
        initialMinConnectedLinkCap();
	initializeOperatingPoint();
        computeGradientAtOptPt();
    }
    
    
    // compute the minimum capacity among all links connected
    // to each user
    private void initialMinConnectedLinkCap() {
        for (int u = 0; u < userCount; u++) {
            double minCapacity = Double.POSITIVE_INFINITY;
            int degree = problem.getVariableDegree(u);
            for (int lIndex = 0; lIndex < degree; lIndex++) {
                int l = problem.getVariableNeighbor(u, lIndex);
                double currentCap = problem.getLinkCapacity(l);
                if (currentCap < minCapacity)
                    minCapacity = currentCap;
            } 
            if (minCapacity == Double.POSITIVE_INFINITY)
                throw new IllegalStateException("user is not connected "
                                                + "to any links");
            minConnectedLinkCap[u] = minCapacity;
        }
    }
    

    // initialize the operating point to some where inside the feasible region
    protected void initializeOperatingPoint() {
        double[] init = new double [userCount];
        for (int u = 0; u < userCount; u++) {
            double x = Double.POSITIVE_INFINITY;
            int degree = problem.getVariableDegree(u);
            for (int lIndex = 0; lIndex < degree; lIndex++) {
                int l = problem.getVariableNeighbor(u, lIndex);
                double cap = problem.getLinkCapacity(l)
                    / ((double) problem.getFactorDegree(l));
                if (cap < x)
                    x = cap;
            } 
            if (x == Double.POSITIVE_INFINITY)
                throw new IllegalStateException("user is not connected "
                                                + "to any links");
            init[u] = initialOptPtAlpha * x;
        }
        operatingPt.set(init);
    }
    
    // depends on implementation
    // this method should fill the operatingPtIncrement array
    protected abstract void computeNextOperatingPoint();

    public void iterate() {
        if (problem == null)
            throw new IllegalStateException("must initialize with a problem");

        computeNextOperatingPoint();

        operatingPtChangeNorm = 0.0;
        
        // check first to see if we have an ascent direction
//         double v = 0.0;
//         for (int u = 0; u < userCount; u++)
//             v += operatingPtIncrement[u] * gradientAtOperatingPt[u];
//         if (v <= 0.0) {
//             System.out.println("no improvement 1");
//             goto end;
//         }
       


        // figure out how much to scale the increment to get feasiblity
        // users

        double scale = 1.0;

        for (int u = 0; u < userCount; u++) {
            double x = operatingPt.getUserAllocation(u);
            double dx = operatingPtIncrement[u];

	    if (x + scale*dx < 0){
		scale = Math.min(scale, (EPSILON_2-x)/dx);
		//scale = Math.max(0, (EPSILON_2-x)/dx);
	    }
        }
        // links
        for (int l = 0; l < linkCount; l++) {
            double capacity = problem.getLinkCapacity(l);
            double x = operatingPt.getLinkAllocation(l);
            double dx = 0.0;
            int degree = problem.getFactorDegree(l);
            for (int uIndex = 0; uIndex < degree; uIndex++) {
                int u = problem.getFactorNeighbor(l, uIndex);
                dx += operatingPtIncrement[u];
            }
	    if (x + scale * dx > capacity){
		//scale = Math.min(scale,scale* (capacity- EPSILON_2-x)/dx);
		//scale= Math.max(0, scale*(capacity-EPSILON_2-x)/dx);
                scale = Math.min(scale, (capacity - EPSILON_2 - x)/dx);
	    }
	    
        }
	
	if (scale < 0.0) {
            System.out.println("negative scaling");
            scale = 0.0;
        }
	
	double s = decreasingStep_a;
        if (decreasingStep_b > 0.0) 
	    s *= decreasingStep_b / (decreasingStep_b + numIteration);
	    
        if (s >= scale)
            s = scale;
	
        // set the new operating point
        nextOperatingPt.set(operatingPt, s, operatingPtIncrement);

	if (!nextOperatingPt.isFeasible())
	    throw new IllegalStateException("left feasible region");
    
        // make sure this was an improvement
	//         if (nextOperatingPt.getObjectiveValue() 
	//    <= operatingPt.getObjectiveValue()) {
	//   System.out.println(nextOperatingPt.getObjectiveValue());
	//    System.out.println("no improvement2");
	     //             goto end;
        // }
         
        operatingPtChangeNorm = 0.0;
        for (int u = 0; u < userCount; u++) {
            double dx = operatingPt.getUserAllocation(u)
                - nextOperatingPt.getUserAllocation(u);
            operatingPtChangeNorm += dx * dx;
        }
        operatingPtChangeNorm = Math.sqrt(operatingPtChangeNorm);

        ContRateControlSolution tmp = operatingPt;
        operatingPt = nextOperatingPt;
        nextOperatingPt = tmp;
        
        computeGradientAtOptPt();

        end:
	numIteration++;
    }

    
    public ContRateControlSolution getSolution(){ 
	return operatingPt; 
    }

    public boolean hasConverged(){ 
	return gradientNormAtOperatingPt <= gradNormTolerance;
    }

    public double getGradientNormAtOperatingPoint() {
        return gradientNormAtOperatingPt;
    }

    public double getOperatingPointChangeNorm() {
        return operatingPtChangeNorm;
    }

    /* Computes gradient and hessian of objective value at current 
     * operating point. Puts the gradient value of each
     * user u in gradientAtOperatingPt[u];
     * Note: uses hard-coded expression (for log util fn).
     */
    protected void computeGradientAtOptPt(){
        if (!operatingPt.isFeasible())
            throw new IllegalStateException("left feasible region");

        double beta = problem.getBarrierCoefficient();

        // initialize due to utility function
	for (int u = 0; u < userCount; u++) {
            double w = problem.getUserUtility(u);
            double x = operatingPt.getUserAllocation(u);
            gradientAtOperatingPt[u] = w / x;
            diagHessianAtOperatingPt[u] = - w / (x*x);
        }
            

        // adjust gradient for barrier functions
        for (int l = 0; l < linkCount; l++) {
            double z = problem.getLinkCapacity(l) 
                - operatingPt.getLinkAllocation(l);
            double dg =  - beta / z;
            double dh = - beta / (z*z);
	    int degree = problem.getFactorDegree(l);
            for (int uIndex = 0; uIndex < degree; uIndex++) {
                int u = problem.getFactorNeighbor(l, uIndex);
                gradientAtOperatingPt[u] += dg;
                diagHessianAtOperatingPt[u] += dh;
            }
        }

        // compute the norm of the gradient
        gradientNormAtOperatingPt = 0.0;
	for (int u = 0; u < userCount; u++) 
            gradientNormAtOperatingPt +=
                gradientAtOperatingPt[u] * gradientAtOperatingPt[u];
        gradientNormAtOperatingPt = Math.sqrt(gradientNormAtOperatingPt);
    }
    
}
 
