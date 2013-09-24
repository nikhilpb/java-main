package com.moallemi.util.data;

import java.util.*;
import javax.swing.event.EventListenerList;

/**
 * A set of key/value properties.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class AttributeMap implements Cloneable {
    private Map properties = new HashMap(11);

    /**
     * Perform a shallow copy.
     *
     * @return a shallow copy
     */
    public Object clone() {
	try {
	    AttributeMap clone = (AttributeMap) super.clone();

	    clone.properties = new HashMap(11);
	    clone.properties.putAll(this.properties);
	    return clone;
	}
	catch (CloneNotSupportedException e) {
	    // should never get here, since clone is supported
	    throw new RuntimeException("clone not supported");
	}
    }

    /**
     * Determine if a attribute is set.
     *
     * @param key the key
     * @return <code>true</code> if the attribute is set, <code>false</code>
     * otherwise
     */
    public boolean containsKey(AttributeMapKey key) {
	return properties.containsKey(key);
    }


    /**
     * Remove a attribute.
     *
     * @param key the key
     * @return the former value of the attribute, or <code>null</code>
     * if it was not set.
     */
    public Object removeKey(AttributeMapKey key) {
	Object oldValue = properties.remove(key);
	notifyListeners(key, oldValue, null);
	return oldValue;
    }

    /**
     * Get a attribute by key.
     *
     * @param key the key
     * @return the value
     * @throws AttributeUnknownException if the attribute does not exist
     */
    public Object get(AttributeMapKey key) 
	throws AttributeUnknownException, ClassCastException 
    {
	Object value = properties.get(key);
	if (value == null)
	    throw new AttributeUnknownException("unknown attribute: " 
					       + key.toString());


	// this exception should never get thrown since
	// class would have already been checked by the set()
	// method
	if (!key.getValueClass().isInstance(value))
	    throw new ClassCastException("bad attribute class, "
					 + "cannot cast "
					 + value.getClass().toString()
					 + " to "
					 + key.getValueClass().toString());

	return value;
    }

    /**
     * Set a attribute.
     *
     * @param key the key
     * @param value the value
     * @throws ClassCastException if there is key/value class inconsistency
     */
    public void set(AttributeMapKey key, Object value) 
	throws ClassCastException
    {
	if (value == null) {
	    removeKey(key);
	}
	else {
	    if (!key.getValueClass().isInstance(value))
		throw new ClassCastException("bad attribute class, "
					     + "cannot cast "
					     + value.getClass().toString()
					     + " to "
					     + key.getValueClass().toString());
	    Object oldValue = properties.put(key, value);
	    notifyListeners(key, oldValue, value);
	}
    }


    /**
     * Copy all the attributes from another map. Note that this is a 
     * shallow copy!
     *
     * @param other the other map
     */
    public void setAll(AttributeMap other) {
	for (Iterator i = other.properties.entrySet().iterator();
	     i.hasNext(); ) {
	    Map.Entry e = (Map.Entry) i.next();
	    set((AttributeMapKey) e.getKey(),
		e.getValue());
	}
    }

    /**
     * Clear all attributes.
     */
    public void clear() {
	AttributeMapKey[] keys = 
	    (AttributeMapKey[]) properties.keySet()
	    .toArray(new AttributeMapKey [0]);
	for (int i = 0; i < keys.length; i++)
	    removeKey(keys[i]);
    }

    /**
     * Get an integer attribute.
     *
     * @param key the key
     * @return the value
     * @throws AttributeUnknownException if the attribute does not exist
     * @throws ClassCastException if the attribute cannot be cast to an integer
     */
    public int getInt(AttributeMapKey key) 
	throws AttributeUnknownException, ClassCastException
    {
	if (!Integer.class.isAssignableFrom(key.getValueClass()))
	    throw new ClassCastException("cannot cast "
					 + key.getValueClass().toString()
					 + " to integer");
	Object value = get(key);
	return ((Integer) value).intValue();
    }

    /**
     * Get a float attribute.
     *
     * @param key the key
     * @return the value
     * @throws AttributeUnknownException if the attribute does not exist
     * @throws ClassCastException if the attribute cannot be cast to a float
     */
    public float getFloat(AttributeMapKey key) 
	throws AttributeUnknownException, ClassCastException

    {
	if (!Float.class.isAssignableFrom(key.getValueClass()))
	    throw new ClassCastException("cannot cast "
					 + key.getValueClass().toString()
					 + " to float");
	Object value = properties.get(key);
	return ((Float) value).floatValue();
    }

    /**
     * Get a double attribute.
     *
     * @param key the key
     * @return the value
     * @throws AttributeUnknownException if the attribute does not exist
     * @throws ClassCastException if the attribute cannot be cast to a double
     */
    public double getDouble(AttributeMapKey key) 
	throws AttributeUnknownException, ClassCastException
    {
	if (!Double.class.isAssignableFrom(key.getValueClass()))
	    throw new ClassCastException("cannot cast "
					 + key.getValueClass().toString()
					 + " to double");
	Object value = get(key);
	return ((Double) value).doubleValue();
    }

    /**
     * Get a string attribute.
     *
     * @param key the key
     * @return the value
     * @throws AttributeUnknownException if the attribute does not exist
     * @throws ClassCastException if the attribute cannot be cast to string
     */
    public String getString(AttributeMapKey key) 
	throws AttributeUnknownException, ClassCastException
    {
	if (!String.class.isAssignableFrom(key.getValueClass()))
	    throw new ClassCastException("cannot cast "
					 + key.getValueClass().toString()
					 + " to string");
	Object value = get(key);
	return (String) value;
    }


    /**
     * Get a boolean attribute.
     *
     * @param key the key
     * @return the value
     * @throws AttributeUnknownException if the attribute does not exist
     * @throws ClassCastException if the attribute cannot be cast to boolean
     */
    public boolean getBoolean(AttributeMapKey key) 
	throws AttributeUnknownException, ClassCastException
    {
	if (!Boolean.class.isAssignableFrom(key.getValueClass()))
	    throw new ClassCastException("cannot cast "
					 + key.getValueClass().toString()
					 + " to boolean");
	Object value = get(key);
	return ((Boolean) value).booleanValue(); 
    }

    /**
     * Set an integer attribute.
     *
     * @param key the key
     * @param value the value
     * @throws ClassCastException if there is key/value class inconsistency
     */
    public void set(AttributeMapKey key, int value) 
	throws ClassCastException
    {
	set(key, new Integer(value));
    }

    /**
     * Set a float attribute.
     *
     * @param key the key
     * @param value the value
     * @throws ClassCastException if there is key/value class inconsistency
     */
    public void set(AttributeMapKey key, float value) 
	throws ClassCastException
    {
	set(key, new Float(value));
    }

    /**
     * Set a double attribute.
     *
     * @param key the key
     * @param value the value
     * @throws ClassCastException if there is key/value class inconsistency
     */
    public void set(AttributeMapKey key, double value) 
	throws ClassCastException
    {
	set(key, new Double(value));
    }

    /**
     * Set a boolean attribute.
     *
     * @param key the key
     * @param value the value
     * @throws ClassCastException if there is key/value class inconsistency
     */
    public void set(AttributeMapKey key, boolean value) 
	throws ClassCastException
    {
	set(key, new Boolean(value));
    }


    // listener code
    private EventListenerList listenerList = new EventListenerList();

    
    /**
     * Add a attribute set listener.
     *
     * @param listener the listener
     */
    public void addAttributeMapListener(AttributeMapListener listener) {
	listenerList.add(AttributeMapListener.class, listener);
    }


    /**
     * Remove a attribute set listener.
     *
     * @param listener the listener
     */
    public void removeAttributeMapListener(AttributeMapListener listener) {
	listenerList.remove(AttributeMapListener.class, listener);
    }

    // utility
    private void notifyListeners(AttributeMapKey key, 
				 Object oldValue, 
				 Object newValue) 
    {
	if (listenerList.getListenerCount() > 0) {
	    Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i += 2) {
		if (listeners[i] == AttributeMapListener.class) 
		    ((AttributeMapListener) listeners[i+1])
			.attributeModified(this, key, oldValue, newValue);
	    }
	}
    }
    
}
    

