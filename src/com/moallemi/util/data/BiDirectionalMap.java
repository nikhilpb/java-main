package com.moallemi.util.data;

import java.util.*;

/**
 * Provides a map from <code>key</code> to <code>value</code> and 
 * <code>value</code> to <code>key</code>. This class does not
 * permit <code>null</code> keys or values.
 * 
 *
 * @author $Author: ciamac $
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class BiDirectionalMap implements Map {
    private HashMap key2value = new HashMap();
    private HashMap value2key = new HashMap();
    

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {	return key2value.size(); }

    
    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() { return key2value.isEmpty(); }



    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key
     * @throws NullPointerException if the key is <tt>null</tt>
     */
    public boolean containsKey(Object key) {
	if (key == null)
	    throw new NullPointerException();
	return key2value.containsKey(key); 
    }
	
   /**
     * Returns <tt>true</tt> if this map maps a key to the
     * specified value. Runs in constant time.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     * @throws NullPointerException if the value is <tt>null</tt>
     */
    public boolean containsValue(Object value) {
	if (value == null)
	    throw new NullPointerException();
	return value2key.containsKey(value);
    }

   /**
     * Returns the value to which this map maps the specified key.
     * The same as <code>getByKey(Object)</code>.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *	       <tt>null</tt> if the map contains no mapping for this key.
     * @throws NullPointerException if the key is <tt>null</tt>
     * @see #getByKey(Object)
     */
    public Object get(Object key) {
	return getByKey(key);
    }

   /**
     * Returns the value to which this map maps the specified key.
     * The same as <code>get(Object)</code>.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *	       <tt>null</tt> if the map contains no mapping for this key.
     * @throws NullPointerException if the key is <tt>null</tt>
     * @see #get(Object)
     */
    public Object getByKey(Object key) {
	if (key == null)
	    throw new NullPointerException();
	return key2value.get(key);
    }

   /**
     * Returns the key to which this map maps the specified value.
     * The same as <code>get(Object)</code>.
     *
     * @param value value whose associated key is to be returned.
     * @return the value to which this map maps the specified value, or
     *	       <tt>null</tt> if the map contains no mapping for this value.
     * @throws NullPointerException if the value is <tt>null</tt>
     * @see #get(Object)
     */
    public Object getByValue(Object value) {
	if (value == null)
	    throw new NullPointerException();
	return value2key.get(value);
    }

    /**
     * Associates the specified value with the specified key in this map
     * If the map previously contained a mapping for
     * this key (or value), the old key (or value) is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key. 
     * @throws NullPointerException if the key or value is <tt>null</tt>
     */
    public Object put(Object key, Object value) {
	if (key == null || value == null)
	    throw new NullPointerException();
	value2key.put(value, key);
	return key2value.put(key, value);
    }

    
    /**
     * Associates the specified value with the specified key in this map
     * If the map previously contained a mapping for
     * this key (or value), the old key (or value) is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return a key-value pair where the key (value) is the previous
     * associated key (value) contained in the map, or <code>null</code>
     * otherwise. If there was no prior key or value associated, 
     * <code>null</code> is returned. This pair cannot be modified!
     * @throws NullPointerException if the key is <tt>null</tt>
     */
    public Map.Entry putPair(Object key, Object value) {
	if (key == null || value == null)
	    throw new NullPointerException();
	Object old_key = value2key.put(value, key);
	Object old_value = key2value.put(key, value);
	return old_key != null || old_value != null
	    ? new Entry(old_key, old_value) : null;
    }
    
    /**
     * Removes the mapping for this key from this map if present.
     * Identical to <code>removeKey(Object)</code>.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key
     * @throws NullPointerException if the key is <tt>null</tt>
     * @see #removeKey(Object)
     */
    public Object remove(Object key) {
	return removeKey(key);
    }

    /**
     * Removes the mapping for this key from this map if present.
     * Identical to <code>remove(Object)</code>.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key
     * @see #remove(Object)
     * @throws NullPointerException if the key is <tt>null</tt>
     */
    public Object removeKey(Object key) {
	if (key == null)
	    throw new NullPointerException();
	Object value = key2value.remove(key);
	if (value != null)
	    value2key.remove(value);
	return value;
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * These mappings will replace any mappings that
     * this map had for any of the keys currently in the specified map.
     *
     * @param t Mappings to be stored in this map.
     * @throws NullPointerException if the mappings contain
     * a key or value that is <tt>null</tt>
     */
    public void putAll(Map t) {
	for (Iterator i = t.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry e = (Map.Entry) i.next();
	    put(e.getKey(), e.getValue());
	}
    }


    /**
     * Removes all mappings from this map.
     */
    public void clear() {
	key2value.clear();
	value2key.clear();
    }

    /**
     * Returns a set view of the keys contained in this map.
     * Behavior is undefined if this set is modified.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set keySet() { 
	return Collections.unmodifiableSet(key2value.keySet()); 
    }


    /**
     * Returns a collection view of the values contained in this map.
     * Behavior is undefined if this collection is modified.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection values() { 
	return Collections.unmodifiableSet(value2key.keySet()); 
    }

    /**
     * Returns a set view of the values contained in this map.
     * Behavior is undefined if this set is modified.
     *
     * @return a set view of the values contained in this map.
     */
    public Set valueSet() { 
	return Collections.unmodifiableSet(value2key.keySet()); 
    }

    /**
     * Returns a set view of the key-value pairs contained in this map.
     * Behavior is undefined if this set is modified.
     * Note that if the <code>Map.Entry</code> objects are modified, behavior
     * is also undefined!
     *
     * @return a set view of the values contained in this map.
     */
    public Set entrySet() { 
	return Collections.unmodifiableSet(key2value.entrySet()); 
    }


    /**
     * Compares the specified object with this map for equality.  Returns
     * <tt>true</tt> if the given object is also a map and the two Maps
     * represent the same mappings.  More formally, two maps <tt>t1</tt> and
     * <tt>t2</tt> represent the same mappings if
     * <tt>t1.entrySet().equals(t2.entrySet())</tt>.  This ensures that the
     * <tt>equals</tt> method works properly across different implementations
     * of the <tt>Map</tt> interface.
     *
     * @param o object to be compared for equality with this map.
     * @return <tt>true</tt> if the specified object is equal to this map.
     */
    public boolean equals(Object o) {
	if (o == this)
	    return true;

	if (!(o instanceof Map))
	    return false;
	Map t = (Map) o;
	if (t.size() != size())
	    return false;

	Iterator i = t.entrySet().iterator();
	while (i.hasNext()) {
	    Entry e = (Entry) i.next();
	    Object key = e.getKey();
	    Object value = e.getValue();
	    if (key == null || value == null)
		return false;
	    if (!getByKey(key).equals(value))
		return false;
	}
	return true;
    }


    /**
     * Returns the hash code value for this map.  The hash code of a map
     * is defined to be the sum of the hashCodes of each entry in the map's
     * entrySet view.  This ensures that <tt>t1.equals(t2)</tt> implies
     * that <tt>t1.hashCode()==t2.hashCode()</tt> for any two maps
     * <tt>t1</tt> and <tt>t2</tt>, as required by the general
     * contract of Object.hashCode.
     *
     * @return the hash code value for this map.
     * @see Map.Entry#hashCode()
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() { return key2value.hashCode(); }


    // map entry class
    private static class Entry implements Map.Entry {
	private Object key, value;

	private Entry(Object key, Object value) {
	    if (key == null || value == null)
		throw new NullPointerException();
	    this.key = key;
	    this.value = value;
	}

	// Map.Entry Ops 

	public Object getKey() {
	    return key;
	}

	public Object getValue() {
	    return value;
	}

	public Object setValue(Object value) {
	    throw new UnsupportedOperationException();
	}

	public boolean equals(Object o) {
	    if (!(o instanceof Map.Entry))
		return false;
	    Map.Entry e = (Map.Entry)o;
	    return key.equals(e.getKey()) && value.equals(e.getValue());
	}

	public int hashCode() {
	    return key.hashCode() ^ value.hashCode();
	}

	public String toString() {
	    return key+"="+value;
	}
    }
}
