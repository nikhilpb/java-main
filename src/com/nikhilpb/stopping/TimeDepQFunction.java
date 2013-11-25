package com.nikhilpb.stopping;

import com.nikhilpb.adp.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/3/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeDepQFunction implements QFunction {
    private ArrayList<StateFunction> contValues;

    public TimeDepQFunction(ArrayList<StateFunction> contValues) {
        this.contValues = contValues;
    }

    /**
     *
     * @param state
     * @param action
     * @return Estimated value at state and action.
     */
    @Override
    public double value(State state, Action action) {
        StoppingAction stoppingAction = (StoppingAction)action;
        if (stoppingAction == StoppingAction.STOP) {
            return 0.;
        }
        StoppingState stoppingState = (StoppingState)state;
        return contValues.get(stoppingState.time).value(state);
    }

    /**
     * Prints the QFunction to a file. Only valid for 1-d model.
     *
     * @param model Must be 1 dimensional
     * @param fileName Will be overwritten
     * @param lowPrice
     * @param highPrice
     * @param delta
     */
    public void printQFunction(final StoppingModel model,
                               final String fileName,
                               double lowPrice,
                               double highPrice,
                               double delta) {
        final int timePeriods = model.getTimePeriods();
        int dimension = model.getDimension();
        if (dimension > 1) {
            throw new RuntimeException("dimension should be 1");
        }
        try {
            PrintWriter writer = new PrintWriter("results/" + fileName, "UTF-8");
            int pointCount = (int)Math.floor((highPrice - lowPrice) / delta);
            for (int t = 1; t < timePeriods - 1; ++t) {
                for (int p = 0; p <= pointCount; ++p) {
                    double price = lowPrice + p * delta;
                    double [] stateVec = { Math.log(price) };
                    StoppingState stoppingState = new StoppingState(stateVec, t);
                    double val = contValues.get(t).value(stoppingState);
                    writer.println(t + "," + price + "," + val);
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
