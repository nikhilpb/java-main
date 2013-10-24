package com.nikhilpb.stopping;

import com.nikhilpb.adp.StateKernel;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/24/13
 * Time: 10:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class QPColumn {
    public double[] thisQS, thisQC;
    public double[] nextQS, nextQC;
    public double[] prevQC;

    public QPColumn() { }
}
