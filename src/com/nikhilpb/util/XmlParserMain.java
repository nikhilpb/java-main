package com.nikhilpb.util;

import com.moallemi.util.data.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/24/13
 * Time: 12:21 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class XmlParserMain {
    protected static ArrayList<Pair<String, Properties>> parseTree;

    protected interface CommandProcessor {
        boolean processCommand(Properties props) throws Exception;
    }

    protected interface CommandLineHandler {
        public boolean handleCommandLine(String[] args) throws Exception;
    }

    protected static boolean parseCommandLine(String[] args, CommandLineHandler handler) {
        String fileName = args[0];
        File configFile = new File(args[0]);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();
            parseTree = new ArrayList<Pair<String, Properties>>();
            NodeList nl = doc.getElementsByTagName("config");
            if (nl.getLength() == 0) {
                new RuntimeException("config file must have config as a root node");
            }

            Node configNd = nl.item(0);
            NodeList cmds = configNd.getChildNodes();
            for (int i = 0; i < cmds.getLength(); ++i) {
                Node nd = cmds.item(i);
                if (nd.getNodeType() == Node.ELEMENT_NODE) {
                    String cmdName = nd.getNodeName();
                    NodeList params = nd.getChildNodes();
                    Properties props = new Properties();
                    for (int j = 0; j < params.getLength(); ++j) {
                        Node propNode = params.item(j);
                        if (propNode.getNodeType() == Node.ELEMENT_NODE) {
                            props.setProperty(params.item(j).getNodeName(), params.item(j).getTextContent());
                        }
                    }
                    Pair<String, Properties> p = new Pair<String, Properties>(cmdName, props);
                    parseTree.add(p);
                }
            }
            if (handler != null) {
                handler.handleCommandLine(args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    protected static boolean executeCommands(HashMap<String, CommandProcessor> cmdMap) {
        long startTime = System.currentTimeMillis();
        ArrayList<String> failedCommands = new ArrayList<String>();
        for (Pair<String, Properties> p : parseTree) {
            String cmd = p.getFirst();
            long cmdStartTime = System.currentTimeMillis();
            try {
                if (cmdMap.containsKey(cmd)) {
                    CommandProcessor cp = cmdMap.get(cmd);
                    System.out.println("EXECUTING COMMAND: " + cmd);
                    if (!cp.processCommand(p.getSecond())) {
                        failedCommands.add(cmd);
                    }
                } else {
                    failedCommands.add(cmd);
                    System.err.println("UNKNOWN COMMAND: " + cmd);
                }
            } catch (Exception e) {
                System.err.println("Exception in " + cmd);
                e.printStackTrace();
                failedCommands.add(cmd);
            }
            long endTime = System.currentTimeMillis();
            System.out.printf("Command executing time: %.3f s\n\n", 0.001 * ((double) (endTime - cmdStartTime)));
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Total elapsed time: %.3f s\n\n", 0.001 * ((double) (endTime - startTime)));
        if (!failedCommands.isEmpty()) {
            System.out.printf("These commands failed: %s\n\n", failedCommands);
        }
        return failedCommands.isEmpty();
    }

    protected static String getPropertyOrDie(Properties props, String tag) {
        String prop = props.getProperty(tag);
        if (prop == null) {
            throw new RuntimeException(tag + " is required");
        }
        return prop;
    }

    protected static int getIntPropertyOrDie(Properties props, String tag) {
        return Integer.parseInt(getPropertyOrDie(props, tag));
    }

    protected static long getLongPropertyOrDie(Properties props, String tag) {
        return Long.parseLong(getPropertyOrDie(props, tag));
    }

    protected static double getDoublePropertyOrDie(Properties props, String tag) {
        return Double.parseDouble(getPropertyOrDie(props, tag));
    }
}
