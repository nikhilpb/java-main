package com.moallemi.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class PropertySet extends Properties  {

    public PropertySet() {
        super();
    }

    public PropertySet(File file) throws IOException {
        BufferedInputStream in = 
            new BufferedInputStream(
                                    new FileInputStream(file));
        this.load(in);
        in.close();
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public long getLong(String key) {
        return Long.parseLong(getString(key));
    }

    public double getDouble(String key) {
        return Double.parseDouble(getString(key));
    }

    public double[] getDoubleArray(String key) {
        String str = getString(key);
        Pattern p = Pattern.compile("\\S+");
        Matcher m = p.matcher(str);
        ArrayList<String> list = new ArrayList<String>();
        while (m.find()) 
            list.add(str.substring(m.start(), m.end()));
        double[] values = new double [list.size()];
        for (int i = 0; i < values.length; i++) 
            values[i] = Double.parseDouble(list.get(i));
        return values;
    }

    public float getFloat(String key) {
        return Float.parseFloat(getString(key));
    }

    public boolean  getBoolean(String key) {
        return Boolean.valueOf(getString(key)).booleanValue();
    }

    public String  getString(String key) {
        String s = getProperty(key).trim();
        if (s == null)
            throw new RuntimeException("key " 
                                       + key + " not found in property set");
        return s;
    }

    public int getIntDefault(String key, int d) {
        if (this.containsKey(key))
            return Integer.parseInt(getString(key));
        else 
            return d;
    }

    public long getLongDefault(String key, long d) {
        if (this.containsKey(key))
            return Long.parseLong(getString(key));
        else 
            return d;
    }

    public double getDoubleDefault(String key, double d) {
        if (this.containsKey(key))
            return Double.parseDouble(getString(key));
        else 
            return d;
    }

    public float getFloatDefault(String key, float d) {
        if (this.containsKey(key))
            return Float.parseFloat(getString(key));
        else 
            return d;
    }

    public boolean  getBooleanDefault(String key, boolean d) {
        if (this.containsKey(key))
            return Boolean.valueOf(getString(key))
                .booleanValue();
        else 
            return d;
    }

    public String  getStringDefault(String key, String d) {
        if (this.containsKey(key))
            return getString(key);
        else 
            return d;
    }

}
	

