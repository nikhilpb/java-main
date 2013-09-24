package com.moallemi.contresalloc;

import com.moallemi.minsum.*;
import Jama.Matrix;
import java.util.Arrays;


/* Class: CRCMinSumSolver 
 * -------------------------------
 * Solves continuous resource allocation problem 
 * using message passing algorithm.  Assume 
 * quadratic message.
 * NOTE: The update formulas and inOptimalRange()
 * are still utility-function-specific 
 * (ui = wi*ln(xi))
 * Have to make this more portable later.
 *
 */

public class CRCMinSumSolver extends IterativeLogUtilSolver 
{

    //ivars
    private double messageDamp;
    private double bellmanError;

    // indexed by [user][link_adj_index]
    private double[][] userIncomingMsg_k;
    private double[][] userIncomingMsg_h;
    private double[] sumUserIncomingMsg_k;
    private double[] sumUserIncomingMsg_h;

    // indexed by [link][user_adj_index]
   
    private double[][]linkIncomingMsg_k;
    private double[][]linkIncomingMsg_h;
    private double [] sumLinkIncomingMsg_k;
    private double [] sumLinkIncomingMsg_h;
    
    private double[] barrierGradientAtOptPt;
    private double[] barrierHessianAtOptPt;


    //methods:
    
    public CRCMinSumSolver(double messageDamp,
			   double initialOptPtAlpha,
			   double gradNormTolerance,
			   double decreasingStep_a,
			   double decreasingStep_b)
    {

	super(initialOptPtAlpha,
              gradNormTolerance,
	      decreasingStep_a,
	      decreasingStep_b);

        this.messageDamp = messageDamp;
    }

    public void setProblem(ContRateControlProblem problem) {
        super.setProblem(problem);
        barrierGradientAtOptPt = new double[linkCount];
	barrierHessianAtOptPt = new double[linkCount];
	initializeUsers();
	initializeLinks();
    }

    private void initializeUsers(){
	userIncomingMsg_k = new double[userCount][];
	userIncomingMsg_h = new double[userCount][];
	sumUserIncomingMsg_k = new double[userCount];
	sumUserIncomingMsg_h = new double[userCount];
	for (int u = 0; u < userCount; u++) {
	    userIncomingMsg_k[u] = new double[problem.getVariableDegree(u)];
	    userIncomingMsg_h[u] = new double[problem.getVariableDegree(u)];
        }
    }

    
    private void initializeLinks(){
	linkIncomingMsg_k = new double[linkCount][];
	linkIncomingMsg_h = new double[linkCount][];
	sumLinkIncomingMsg_k = new double[linkCount];
	sumLinkIncomingMsg_h = new double[linkCount];
	for (int l = 0; l < linkCount; l++) {
            int degree = problem.getFactorDegree(l);
            linkIncomingMsg_k[l] = new double[degree];
	    linkIncomingMsg_h[l] = new double[degree];
	}
    }

    protected void computeNextOperatingPoint() {
        if (!operatingPt.isFeasible())
            throw new IllegalStateException("infeasible operating point");

        bellmanError = 0.0;
	
	computeUserToLinkMessage();

	updateSumOfUserToLinkMessages();

       	computeLinkToUserMessage();

	updateSumOfLinkToUserMessages();

	updateOperatingPoint();
        
    }

   
    private void computeUserToLinkMessage(){
	// compute new messages to links
        // u(x_u) + \sum_{m \neq l} V_{m\ra u}(x_u)
        for (int l = 0; l < linkCount; l++) {
            int degree = linkIncomingMsg_k[l].length;
            for (int uIndex = 0; uIndex < degree; uIndex++) {
                int u = problem.getFactorNeighbor(l, uIndex);
               
                int lIndex = problem.getFactorNeighborOffset(l, uIndex);
		//compute, for u (which sits at uIndex from l, 
		//what is the index that l sits from it.

                double x = operatingPt.getUserAllocation(u);
                double newValue_k = 
                    - problem.getUserUtility(u) / (x*x)
                    + sumUserIncomingMsg_k[u]
                    - userIncomingMsg_k[u][lIndex];
		//System.out.println("newValue_k "+newValue_k);
		
		double oldValue_k = linkIncomingMsg_k[l][uIndex];
		//update user->link for k
		linkIncomingMsg_k[l][uIndex] = messageDamp * newValue_k
                    + (1.0 - messageDamp) * oldValue_k;

		double newValue_h = 
		    2.0 * problem.getUserUtility(u) / x
                    + sumUserIncomingMsg_h[u]
                    - userIncomingMsg_h[u][lIndex];
		//System.out.println("newValue_h "+newValue_h);
		
		double oldValue_h = linkIncomingMsg_h[l][uIndex];
		//update user->link for h
		linkIncomingMsg_h[l][uIndex] = messageDamp * newValue_h
                    + (1.0 - messageDamp) * oldValue_h;


                bellmanError += Math.abs(newValue_k - oldValue_k)
		    + Math.abs(newValue_h - oldValue_h);
               
            }
        }
    }
    
    /* Since the values of elements in Dxxf, Dxyf, Dyxf, 
     * and Dyyf(Hessian of Barrier fundtion) are the same for each link
     * The update formulas for k and h can be simplified as follows:
     * k = barrierHess - (barrierHess*BarrierHess)*sum of elements of A_inv
     * h = barrierGrad -barrierHess*{
     * (sum of opt pt BW's assigned to user connected to link)-tmp_x*sum of elements 
     * of A_inv -1/2*sigma(hi, sum_colum_A_inv_i)-1/2*sigma(hi,sum_row_A_inv_i))
     * 
     */
    private void computeLinkToUserMessage(){

	// compute new messages to users
        // max_{x_v} \sum_{v\neq u} V_{r\ra l}(x_v}
        // s.t. \sum_{v\neq u} b_v x_v <= c_l - b_u x_u
 
	computeBarrierGradientHessianAtOptPt();

	for (int u = 0; u < userCount; u++) {
            int degree = userIncomingMsg_k[u].length;
            for (int lIndex = 0; lIndex < degree; lIndex++) {
			
		int l = problem.getVariableNeighbor(u, lIndex);
		double barrierGrad = barrierGradientAtOptPt[l];
		double barrierHess = barrierHessianAtOptPt[l];
		int lDegree = problem.getFactorDegree(l);
		int uIndex = problem.getVariableNeighborOffset(u, lIndex);
                int size = lDegree - 1;
		//find : for link l (the link that sits at lIndex from u)
		//what index does u sit from it.

                // y = allocation to user u
                // {x_i} = allocations to other users
 
                // STEP 1:
                // compute a Taylor expansion for the barrier function
                // g(C - y - \sum_i x_i)
                // around the operating point.

                // This will be represented as
                // (1/2) Gamma_yy y^2 + b_y y
                // + y Gamma_yx^T x 
                // + (1/2) x^T Gamma_xx x + b_x x
                double linkAllocation = operatingPt.getLinkAllocation(l);
                double Gamma_yy = barrierHess;
                double b_y = barrierGrad - barrierHess * linkAllocation;
                // Gamma_yx = Gamma_yx_scale * 1
                double Gamma_yx_scale = barrierHess;
                // Gamma_xx = Gamma_xx_scale * 11^T
                double Gamma_xx_scale = barrierHess;
                // b_x = b_x_scale * 1
                double b_x_scale = b_y;
                
                // STEP 2:
                // collect incoming messages as
                // (1/2) x^T K x + h^T x

                // diagonal entriies of K
                double[] K = new double [size];
                double[] h = new double [size];
                constructMatrixKH(l, uIndex, K, h);

                // STEP 3:
                // minimize over x
                // x^* = - [Gamma_xx + K]^(-1) (y Gamma_yx + b_x + h)
                // represent this as
                // x^* = y Delta_yx + Delta_x

                // A_inv = [Gamma_xx + K]^(-1)]^(-1)
                double[][] A_inv = new double [size][size];
                computeRankOneInverse(K, Gamma_xx_scale, A_inv);
                // A_inv1 = A_inv * 1
                double[] A_inv1 = new double [size];
                // sum_A_inv = 1^T * A_inv * 1
                double sum_A_inv = 0.0;
                for (int i = 0; i < size; i++) {
                    A_inv1[i] = 0.0;
                    for (int j = 0; j < size; j++)
                        A_inv1[i] += A_inv[i][j];
                    sum_A_inv += A_inv1[i];
                }

                // Delta_yx = - A_inv * Gamma_yx
                //          = - Gamma_yx_scale * A_inv1
                double[] Delta_yx = new double [size];
                for (int i = 0; i < size; i++) 
                    Delta_yx[i] = - A_inv1[i] * Gamma_yx_scale;

                // Delta_x = - A_inv * (b_x + h)
                //         = - b_x_scale * A_inv1 - A_inv * h
                double[] Delta_x = new double [size];
                for (int i = 0; i < size; i++) {
                    Delta_x[i] = - b_x_scale * A_inv1[i];
                    for (int j = 0; j < size; j++) 
                        Delta_x[i] -= A_inv[i][j] * h[j];
                }

                // STEP 4:
                // collect coefficients of y to compute final message
                // (1/2) K_y y^2 + h_y y

                // STEP 4(a):
                // first from the barrier function

                // K_y = Gamma_yy + 2 Gamma_yx^T Delta_yx 
                //       + Delta_yx^T Gamma_xx Delta_yx
                //     = Gamma_yy + 2 Gamma_yx_scale 1^T Delta_yx
                //       + Gamma_xx_scale * Delta_yx^T 1 1^T Delta_yx
                double K_y = Gamma_yy;
                double sum_Delta_yx = 0.0;
                for (int i = 0; i < size; i++)
                    sum_Delta_yx += Delta_yx[i];
                K_y += 2.0 * Gamma_yx_scale * sum_Delta_yx
                    + Gamma_xx_scale * sum_Delta_yx * sum_Delta_yx;

                // h_y = b_y + Gamma_yx^T Delta_x 
                //       + Delta_yx^T \Gamma_xx Delta_x
                //       + b_x^T Delta_yx
                //     = b_y + Gamma_yx_scale 1^T Delta_x 
                //       + Gamma_xx_scale Delta_yx^T 11^T Delta_x
                //       + b_x_scale 1^T Delta_yx
                double h_y = b_y;
                double sum_Delta_x = 0.0;
                for (int i = 0; i < size; i++)
                    sum_Delta_x += Delta_x[i];
                h_y += Gamma_yx_scale * sum_Delta_x
                    + Gamma_xx_scale * sum_Delta_yx * sum_Delta_x
                    + b_x_scale * sum_Delta_yx;
                
                // STEP 4(b):
                // now from the incoming messages

                // K_y = Delta_yx^T K Delta_yx
                for (int i = 0; i < size; i++)
                    K_y += Delta_yx[i] * K[i] * Delta_yx[i];

                // h_y = Delta_yx^T K Delta_x + h^T Delta_yx
                for (int i = 0; i < size; i++)
                    h_y += Delta_yx[i] * K[i] * Delta_x[i]
                        + h[i] * Delta_yx[i];

		
		//find update value for k
		double oldValue_k = userIncomingMsg_k[u][lIndex];
		double newValue_k = K_y;
                    
		
		userIncomingMsg_k[u][lIndex] = messageDamp * newValue_k
                    + (1.0 - messageDamp) * oldValue_k;
		

		//find update value for h
		double oldValue_h = userIncomingMsg_h[u][lIndex];
		double newValue_h = h_y;
		userIncomingMsg_h[u][lIndex] = messageDamp*newValue_h +
		    (1.0-messageDamp)*oldValue_h;
		
               	bellmanError += Math.abs(newValue_k - oldValue_k)
		    + Math.abs(newValue_h - oldValue_h);
	    }
	}
    }

    private void updateSumOfLinkToUserMessages() {
	for (int i = 0; i < userCount; i++){
	    int degree = userIncomingMsg_k[i].length;
	    double sum_h = 0.0; 
	    double sum_k = 0.0;
	    for (int j = 0; j < degree; j++){
		sum_k += userIncomingMsg_k[i][j];
		sum_h += userIncomingMsg_h[i][j];
	    }
	    sumUserIncomingMsg_k[i] = sum_k;
	    sumUserIncomingMsg_h[i] = sum_h;
	}
    }

    private void updateSumOfUserToLinkMessages() {
	for (int i = 0; i < linkCount; i++){
	    int degree = linkIncomingMsg_k[i].length;
	    double sum_h = 0.0;
	    double sum_k = 0.0;
	    for (int j = 0; j < degree; j++){
		sum_k += linkIncomingMsg_k[i][j];
		sum_h += linkIncomingMsg_h[i][j];
	    }
	    sumLinkIncomingMsg_k[i] = sum_k;
	    sumLinkIncomingMsg_h[i] = sum_h;
	}
    }

    private void updateOperatingPoint(){
	for (int u = 0; u < userCount; u++){
	    // double A = sumUserIncomingMsg_k[u];
 	    //double B = sumUserIncomingMsg_h[u];
 	    //double w = problem.getUserUtility(u);
 	    //double arg = B*B - 4*A*w;
 	    //if (arg < 0.0) 
	    //throw new IllegalStateException("Negative argument to "
	    //					+ "quadratic equation");
	    //double newOperatingPt = (-B + Math.sqrt(arg))/(2*A);
	    // operatingPtIncrement[u] = optPtDamp 
	    //     * (newOperatingPt - operatingPt.getUserAllocation(u));
	    
  	    double K = sumUserIncomingMsg_k[u];
  	    double h = sumUserIncomingMsg_h[u];
  	    double w = problem.getUserUtility(u);
            double x = operatingPt.getUserAllocation(u);
            double H = w / (x*x) - K;
            operatingPtIncrement[u] = 
                (w / x + K * x + h) / H;
	}
    }
    
    /* Computes the gradient of barrier function for link linkIndex with
     * respect to any user connected to that link,  according to:
     * fr(xa, a connected to r) = B*log(br-sigma(xa, a connected to r)),
     * and populate the array barrierGradientAtOptPt with the values
     * for each link.
     * Note: For this barrier function, the value of each component
     * of gradient (with respect to any xa) is
     * grad(fr(xa)) = -B/(br-sigma(xa, a connected to r)), so we return
     * constant appropriat to the linkIndex.
     */
    /* Computes the Hessian of barrier function for link linkIndex with
     * respect any two users that are connected to it according to:
     * fr(xa, a connected to r) = B*log(br-sigma(xa, a connected to r));
     * Note: For this barrier function, the value of each component
     * of gradient (with respect to any two users) is
     * hess(fr(xa)) = -B/(br-sigma(xa, a connected to r))^2, so we populate
     * the array barrierHessianAtOptPt with the value for each link.
     */      
    private void computeBarrierGradientHessianAtOptPt(){
        double beta = problem.getBarrierCoefficient();
	for (int l = 0; l < linkCount; l++) {
            double excessCapacity = 
                problem.getLinkCapacity(l) 
                - operatingPt.getLinkAllocation(l);
            barrierGradientAtOptPt[l] = - beta / excessCapacity;
            barrierHessianAtOptPt[l] = - beta
                / (excessCapacity * excessCapacity);
        }
    }

    /* Compute the inversion of input using rank one update
     * in the form (A+ alpha*e*eT)^(-1), where A is diagonal matrix.
     */
    private void computeRankOneInverse(double[] A, 
                                       double alpha,
                                       double[][] A_inv) 
    {
	int size = A.length;

	// set diagnoal entries and compute scaling factor
	double z = 1.0 / alpha;
	for (int i = 0; i < size; i++) {
	    if (A[i] >= 0.0) 
		throw new IllegalStateException("zero entry in K matrix");
	    double val = 1.0 / A[i];
	    A_inv[i][i] = val;
	    z += val;
	}

	// adjust diagonal entries
	for (int i = 0; i < size; i++) 
            A_inv[i][i] -= 1.0 / (z * A[i] * A[i]);

	// compute the off-diagonal entries
	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < i; j++) {
		double val = - 1.0 / (z * A[i] * A[j]);
		A_inv[i][j] = val;
		A_inv[j][i] = val;
	    }
	}
    }
    
    private void constructMatrixKH(int linkIndex, int userIndex, 
                                  double[] K, double[] h)
    {
        int degree = linkIncomingMsg_k[linkIndex].length;
	int size = degree - 1;
	int count = 0;
	for (int u = 0; u < degree; u++) {
	    if (u != userIndex) {
                K[count] = linkIncomingMsg_k[linkIndex][u];
                h[count] = linkIncomingMsg_h[linkIndex][u];
                count++;
            }
	}
        if (count != size)
            throw new IllegalStateException("could not compute K and h");
    }

    public double getBellmanError() {
        return bellmanError;
    }
}
 
