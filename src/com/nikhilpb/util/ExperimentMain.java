package com.nikhilpb.util;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 4/15/14
 * Time: 4:54 PM
 * To change this template use File | Settings | File Templates.
 */

import com.nikhilpb.doe.DoeExperiment;
import com.nikhilpb.matching.MatchingExperiment;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 4/15/14
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExperimentMain {

    public static void main(String[] args) {
        Experiment experiment;
        String experimentName = args[0];
        if (experimentName.equals("doe")) {
            experiment = DoeExperiment.getInstance();
        } else if (experimentName.equals("matching")) {
            experiment = MatchingExperiment.getInstance();
        } else {
            throw new IllegalArgumentException("no experiment called " + experimentName);
        }
        String configFile = "config/" +  experimentName + "/" + args[1] + ".xml";
        experiment.parseXml(configFile);
        experiment.executeCommands();
    }
}
