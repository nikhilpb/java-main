package com.nikhilpb.util;

import com.nikhilpb.abtesting.ABTestingExperiment;
import com.nikhilpb.doe.DoeExperiment;
import com.nikhilpb.matching.MatchingExperiment;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * An experiment takes as an input a configuration file. Does computations in the
 * Created by nikhilpb on 2/21/14.
 */
public abstract class Experiment {

  static protected HashMap<String, Experiment> experiments = new HashMap<String, Experiment>();

  /**
   * Registers an experiment with a name.
   * @param name
   * @param experiment
   */
  static protected void registerExperiment(String name, Experiment experiment) {
    if (experiments.containsKey(name)) throw new RuntimeException("name " + name + " already exists");
    experiments.put(name, experiment);
  }

  public Experiment() {
    cmdMap = new HashMap<String, CommandProcessor>();
    parseTree = new ArrayList<Pair<String, Properties>>();
  }

  /**
   * Callback used to associate logic with a command.
   */
  protected interface CommandProcessor {
    boolean processCommand(Properties props) throws Exception;
  }

  private HashMap<String, CommandProcessor> cmdMap;
  private ArrayList<Pair<String, Properties>> parseTree;
  private PrintStream oStream, eStream;

  /**
   * From the filename parse the commands to be executed.
   * @param xmlFileName
   */
  public void parseXml(String xmlFileName) {
    File configFile = new File(xmlFileName);
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(configFile);
      doc.getDocumentElement().normalize();
      NodeList nl = doc.getElementsByTagName("config");
      if (nl.getLength() == 0) {
        new RuntimeException("config file must have config as a root node");
      }

      Node configNd = nl.item(0);
      NodeList cmds = configNd.getChildNodes();
      for (int i = 0; i < cmds.getLength(); ++ i) {
        Node nd = cmds.item(i);
        if (nd.getNodeType() == Node.ELEMENT_NODE) {
          String cmdName = nd.getNodeName();
          NodeList params = nd.getChildNodes();
          Properties props = new Properties();
          for (int j = 0; j < params.getLength(); ++ j) {
            Node propNode = params.item(j);
            if (propNode.getNodeType() == Node.ELEMENT_NODE) {
              props.setProperty(params.item(j).getNodeName(), params.item(j).getTextContent());
            }
          }
          Pair<String, Properties> p = new Pair<String, Properties>(cmdName, props);
          parseTree.add(p);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }

  /**
   * Associates a name with the callback processor.
   * @param name
   * @param processor
   */
  protected void registerCommand(String name, CommandProcessor processor) {
    cmdMap.put(name, processor);
  }

  /**
   * Runs all commands given in the config file.
   * @return
   */
  protected boolean executeCommands() {
    if (oStream == null) {
      oStream = System.out;
    }
    if (eStream == null) {
      eStream = System.err;
    }
    long startTime = System.currentTimeMillis();
    ArrayList<String> failedCommands = new ArrayList<String>();
    for (Pair<String, Properties> p : parseTree) {
      String cmd = p.getFirst();
      long cmdStartTime = System.currentTimeMillis();
      try {
        if (cmdMap.containsKey(cmd)) {
          CommandProcessor cp = cmdMap.get(cmd);
          oStream.println("EXECUTING COMMAND: " + cmd);
          if (! cp.processCommand(p.getSecond())) {
            failedCommands.add(cmd);
          }
        } else {
          failedCommands.add(cmd);
          eStream.println("UNKNOWN COMMAND: " + cmd);
        }
      } catch (Exception e) {
        eStream.println("Exception in " + cmd);
        e.printStackTrace();
        failedCommands.add(cmd);
      }
      long endTime = System.currentTimeMillis();
      oStream.printf("Command executing time: %.3f s\n\n", 0.001 * ((double) (endTime - cmdStartTime)));
    }
    long endTime = System.currentTimeMillis();
    oStream.printf("Total elapsed time: %.3f s\n\n", 0.001 * ((double) (endTime - startTime)));
    if (! failedCommands.isEmpty()) {
      oStream.printf("These commands failed: %s\n\n", failedCommands);
    }
    return failedCommands.isEmpty();
  }

  /**
   * From the Properties retrieve tag or fail.
   * @param props
   * @param tag
   * @return
   */
  protected String getPropertyOrDie(Properties props, String tag) {
    String prop = props.getProperty(tag);
    if (prop == null) {
      throw new RuntimeException(tag + " is required");
    }
    return prop;
  }

  /**
   *
   * @param args Two arguments, first is the experiment to be performed and second is the path to the xml file.
   */
  public static void main(String[] args) {
    // Register all subclasses
    DoeExperiment.register();
    MatchingExperiment.register();
    ABTestingExperiment.register();

    String experimentName = args[0];
    Experiment experiment = experiments.get(experimentName);
    if (experiment == null) throw new RuntimeException("No experiment named: " + experimentName);
    String configFile = "config/" + experimentName + "/" + args[1] + ".xml";
    experiment.parseXml(configFile);
    experiment.executeCommands();
  }
}
