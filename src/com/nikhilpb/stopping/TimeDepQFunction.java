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
     * @param model
     * @param fileName
     */
    public void printQFunction(final StoppingModel model, final String fileName) {
        final int timePeriods = model.getTimePeriods();
        int dimension = model.getDimension();
        if (dimension > 1) {
            throw new RuntimeException("dimension should be 1");
        }
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.printf("Time,Price,Value");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
