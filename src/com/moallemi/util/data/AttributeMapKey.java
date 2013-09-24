package com.moallemi.util.data;

/**
 * A class for attribute keys.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class AttributeMapKey {
    private String name;
    private Class valueClass;

    /**
     * Constructor.
     *
     * @param name the key name (used only for debugging purposes)
     * @param valueClass the class of values for this key
     */
    public AttributeMapKey(String name, Class valueClass) {
	this.name = name;
	this.valueClass = valueClass;
    }

    /**
     * Get the name of this key.
     *
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Get the class for values associated with this key.
     *
     * @return the value class
     */
    public Class getValueClass() { return valueClass; }

    /**
     * Get a string representation of this key.
     *
     * @return a string representation
     */
    public String toString() { return name; }
}
