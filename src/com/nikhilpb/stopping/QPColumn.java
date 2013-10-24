package com.nikhilpb.stopping;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/24/13
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class QPColumn {
    public double[] prevC;
    public double[] curS, curC;
    public double[] nextS, nextC;

    @Override
    public String toString() {
        String string = "";
        string += "curStop: " + arrayToString(curS);
        string += "curCont: " + arrayToString(curC);
        if (prevC != null) {
            string += "prevCont: " + arrayToString(prevC);
        }
        if (nextS != null) {
            string += "nextStop: " + arrayToString(nextS);
        }
        if (nextC != null) {
            string += "nextCont: " + arrayToString(nextC);
        }
        return string;
    }

    private static String arrayToString(double[] arr) {
        String string = "[";
        for (int i = 0; i < arr.length; ++i) {
            string += arr[i];
            if (i < arr.length - 1)
                string += " ";
        }
        string += "]\n";
        return string;
    }
}