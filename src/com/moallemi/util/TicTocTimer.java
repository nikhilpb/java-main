package com.moallemi.util;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * A basic timer class.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-10-29 17:39:48 $
 */
public class TicTocTimer {
    private long t;
    private DecimalFormat df = new DecimalFormat("0.00");

    /**
     * Constructor. Performs implicit <code>tic()</code>.
     */
    public TicTocTimer() { tic(); }

    /**
     * Reset the timer start.
     */
    public void tic() { t = System.currentTimeMillis(); }

    /**
     * Return time since the last <code>tic()</code>.
     *
     * @return time since the last tic, in seconds
     */
    public double toc() { return (System.currentTimeMillis() - t)/1000.0; }

    /**
     * Print the time since the last tic to standard out.
     */
    public void tocPrint() {
	System.out.println("Elapsed time: " + df.format(toc()) + " (secs)");
    }

    /**
     * Print a timestamp.
     */
    public void timeStampPrint() {
	System.out.println("Current time: " 
                           + (new Date()).toString());
    }

    /**
     * Print the time since the last tic to standard out.
     *
     * @param text text to put in front of time
     */
    public void tocPrint(String text) {
	System.out.println(text + ": " + df.format(toc()) + " (secs)");
    }

}
