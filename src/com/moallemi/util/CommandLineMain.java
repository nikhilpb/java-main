package com.moallemi.util;

import java.io.*;
import java.util.*;

import com.moallemi.math.CplexFactory;

/**
 * Command line processor.
 *
 * @author Ciamac C. Moallemi
 * @version $Revision: 1.3 $, $Date: 2007-02-26 22:33:07 $
 */
public abstract class CommandLineMain {
    private long seed = -1L;
    private String[] argv;
    private int argvIndex;
    private boolean debug;
    private CplexFactory cplexFactory = new CplexFactory();

    public CplexFactory getCplexFactory() { return cplexFactory; }

    public Random getRandom() {
        return new Random(seed >= 0 ? seed : System.currentTimeMillis());
    }

    public Random getChildRandom(Random r) {
        return new Random(r.nextLong());
    }

    public boolean isDebug() { return debug; }

    public PrintStream openOutput(String fileName) throws IOException {
        return fileName.equals("-") 
            ? System.out
            : new PrintStream(new 
                              BufferedOutputStream(new 
                                                   FileOutputStream(fileName)));
    }

    public void closeOutput(PrintStream out) throws IOException {
        if (out != System.out)
            out.close();
    }

    public boolean hasNext() { return argvIndex < argv.length; }

    public void processNext() throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        while (argvIndex < argv.length) {
            if (argv[argvIndex].equals(";")) {
                argvIndex++;
                break;
            }
            list.add(argv[argvIndex++]);
        }
        String[] cmdArray = list.toArray(new String [0]);

        // output running command
        StringBuffer sb = new StringBuffer();
        sb.append(">>>");
        for (int i = 0; i < cmdArray.length; i++) 
            sb.append(" ").append(cmdArray[i]);
        String cmdStr = sb.toString();
        System.out.println();
        System.out.println(cmdStr);
        

        CommandLineIterator cmd = new CommandLineIterator(cmdArray);
        boolean handled = false;

        // process it locally
        if (!handled) {
            cmd.reset();
            if (processCommandLocal(cmd))
                handled = true;
        }

        // pass to subclass
        if (!handled) {
            cmd.reset();
            if (processCommand(cmd))
                handled = true;
        }

        if (!handled) 
            throw new CommandLineException("command not understood:\n" 
                                           + cmdStr);
        if (cmd.hasNext()) 
            throw new CommandLineException("command not fully processed,"
                                           + " remaining args:\n"
                                           + cmd.nextAll());
    }

    protected abstract boolean processCommand(CommandLineIterator cmd) 
        throws Exception;

    protected  boolean processCommandLocal(CommandLineIterator cmd) 
        throws Exception
    {
        String base = cmd.next();
        
        if (base.equals("seed")) {
            seed = cmd.nextLong();
        }
        else if (base.equals("debug")) {
            debug = true;
        }
        else if (base.equals("cplexprop")) {
            String key = cmd.next();
            String value = cmd.next();
            cplexFactory.setProperty(key, value);
        }
        else
            return false;

        return true;
    }


    public void run(String[] argv) throws Exception {
        this.argv = argv;
        argvIndex = 0;

        TicTocTimer t = new TicTocTimer();
        TicTocTimer t2 = new TicTocTimer();
        t2.tic();
        while (hasNext()) {
            t.tic();
            processNext();
            t.tocPrint();
        }

        System.out.println();
        System.out.println("COMMAND SUCCESSFUL");
        t2.tocPrint("TOTAL ELAPSED TIME");
        System.exit(0);
    }

}



