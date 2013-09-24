package com.moallemi.util;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class for iterating through a single command.
 *
 * @author Ciamac C. Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-11-02 01:01:56 $
 */
public class CommandLineIterator implements Iterator<String> {
    private String[] cmd;
    private int index;

    public CommandLineIterator(String[] cmd) {
        this.cmd = cmd;
        this.index = -1;
    }

    public void reset() {
        this.index = -1;
    }

    public boolean hasNext() {
        return (index+1) < cmd.length;
    }

    public String next() {
        index++;
        if (index >= cmd.length) 
            throw new CommandLineException("command too short");
        return cmd[index];
    }

    public int nextInt() {
        String s = next();
        if (Pattern.matches("[-\\+]?[0-9]+", s))
            return Integer.parseInt(s);
        throw new CommandLineException("string is not an integer: " + s);
    }

    public long nextLong() {
        String s = next();
        if (Pattern.matches("[-\\+]?[0-9]+", s))
            return Long.parseLong(s);
        throw new CommandLineException("string is not a long: " + s);
    }

    public double nextDouble() {
        String s = next();
        return Double.parseDouble(s);
    }

    public boolean nextBoolean() {
        String s = next();
        if (s.toLowerCase().equals("true"))
            return true;
        if (s.toLowerCase().equals("false"))
            return false;
        throw new CommandLineException("string is not a boolean: " + s);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public String nextAll() {
        StringBuffer sb = new StringBuffer();
        while (true) {
            sb.append(next());
            if (!hasNext())
                break;
            sb.append(" ");
        }
        return sb.toString();
    }
}

