package com.moallemi.util.data;

import java.util.*;
import java.io.*;
import java.util.regex.*;

/**
 * A loader for the <code>AttributeMap</code> class.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class AttributeMapLoader {
    // class for storing possible keys
    private static class KeyEntry {
	public AttributeMapKey key;
	public boolean isRequired;
	public KeyEntry(AttributeMapKey key, boolean isRequired) {
	    this.key = key;
	    this.isRequired = isRequired;
	}
    }

    // string -> keyentry map
    private Map keyMap = new HashMap(11);

    private static final Pattern whitespacePattern = Pattern.compile("\\s");


    /**
     * Add an optional key to be loaded.
     *
     * @param key the key to be loaded
     * @throws DuplicateKeyException if a key with the same name
     * already exists
     * @throws IllegalArgumentException if a key name contains a
     * whitespace (this will break the loader)
     */
    public void addKey(AttributeMapKey key)
	throws DuplicateKeyException, IllegalArgumentException
    {
	addKey(key, false);
    }

    /**
     * Add a key to be loaded.
     *
     * @param key the key to be loaded
     * @param isRequired <code>true</code> if the key is required to be present
     * @throws DuplicateKeyException if a key with the same name
     * already exists
     * @throws IllegalArgumentException if a key name contains a
     * whitespace (this will break the loader)
     */
    public void addKey(AttributeMapKey key, boolean isRequired) 
	throws DuplicateKeyException, IllegalArgumentException
    {
	String name = key.getName();
	if (whitespacePattern.matcher(name).matches())
	    throw new IllegalArgumentException("key contains whitespace: "
					       + name);
	if (keyMap.containsKey(name))
	    throw new DuplicateKeyException("duplicate key: " + key.getName());
	
	KeyEntry entry = new KeyEntry(key, isRequired);
	keyMap.put(name, entry);
    }
    
    private static final Pattern commentPattern 
	= Pattern.compile("^\\s*#");
    private static final Pattern keyValuePattern 
	= Pattern.compile("^\\s*(\\S+)\\s*=(.+)");
    private static final Pattern quotedStringPattern 
	= Pattern.compile("^\"(.+)\"$");
    
    /**
     * Load an attribute map from a reader.
     *
     * @param reader the reader
     * @throws IOException if there is an IO error
     * @throws AttributeUnknownException if a required attribute is missing
     * @throws KeyUnknownException if an unknown key is found
     * @throws DuplicateKeyException if duplicate keys are found
     * @throws ClassCastException if we are unable to cast a value into the
     * proper class
     */
    public AttributeMap load(BufferedReader reader)
	throws IOException, AttributeUnknownException, KeyUnknownException,
	       DuplicateKeyException, ClassCastException
    {
	AttributeMap map = new AttributeMap();
	String line;

	while ((line = reader.readLine()) != null) {
	    line = line.trim();

	    // skip comments or empty lines
	    if (line.length() == 0 || commentPattern.matcher(line).matches())
		continue;

	    Matcher matcher = keyValuePattern.matcher(line);
	    if (!matcher.matches())
		throw new IOException("bad input line: " + line);
	    String keyString = matcher.group(1);
	    String valueString = matcher.group(2).trim();

	    if (!keyMap.containsKey(keyString))
		throw new KeyUnknownException("unknown key: " + keyString);
	    AttributeMapKey key = ((KeyEntry) keyMap.get(keyString)).key;

	    if (map.containsKey(key))
		throw new DuplicateKeyException("duplicate key: " + keyString);
	    
	    Class valueClass = key.getValueClass();
	    Object value;
	    if (valueClass == Boolean.class) 
		value = new Boolean(valueString);
	    else if (valueClass == Byte.class)
		value = new Byte(valueString);
	    else if (valueClass == Double.class)
		value = new Double(valueString);
	    else if (valueClass == Float.class)
		value = new Float(valueString);
	    else if (valueClass == Integer.class)
		value = new Integer(valueString);
	    else if (valueClass == Short.class)
		value = new Short(valueString);
	    else if (valueClass == String.class) {
		Matcher quotedStringMatcher 
		    = quotedStringPattern.matcher(valueString);
		if (quotedStringMatcher.matches())
		    value = quotedStringMatcher.group(1);
		else
		    value = valueString;
	    }
	    else 
		throw new ClassCastException("unable to cast value for key "
					     + keyString
					     + " into class "
					     + valueClass.toString());

	    map.set(key, value);
	}

	// make sure all required keys are loaded
	ArrayList missingKeys = new ArrayList();
	for (Iterator i = keyMap.entrySet().iterator(); i.hasNext(); ) {
	    KeyEntry entry = (KeyEntry) ((Map.Entry) i.next()).getValue();
	    if (entry.isRequired && !map.containsKey(entry.key))
		missingKeys.add(entry.key.getName());
	}
	if (missingKeys.size() > 0) {
	    String[] sortedMissingKeys = 
		(String[]) missingKeys.toArray(new String [0]);
	    Arrays.sort(sortedMissingKeys);
	    StringBuffer sb = new StringBuffer();
	    sb.append("missing attributes: ");
	    for (int i = 0; i < sortedMissingKeys.length; i++) {
		if (i > 0) sb.append(", ");
		sb.append(sortedMissingKeys[i]);
	    }
	    throw new AttributeUnknownException(sb.toString());
	}			

	return map;
    }
}
