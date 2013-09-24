package com.moallemi.contresalloc;

import com.moallemi.minsum.*;
import Jama.Matrix;


/* Class: GradientDescentSolver 
 * -------------------------------
 * Solves continuous resource allocation problem 
 * using Gradient Descent Algorithm.
 * NOTE: The update formulas and inOptimalRange()
 * are still utility-function-specific 
 * (ui = wi*ln(xi))
 * Have to make this more portable later.
 *
 */

public class GradientDescentSolver extends IterativeLogUtilSolver 
{

    //methods:
    
    public GradientDescentSolver(double initialOptPtAlpha,
                                 double gradNormTolerance,
				 double decreasingStep_a,
				 double decreasingStep_b)
    {
	super(initialOptPtAlpha,
              gradNormTolerance,
	      decreasingStep_a,
	      decreasingStep_b);
    }
 
    protected void computeNextOperatingPoint() {
        for (int u = 0; u < userCount; u++)
            operatingPtIncrement[u] = gradientAtOperatingPt[u];
    }
}
 
