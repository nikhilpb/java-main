package com.moallemi.util.data;

/**
 * A listener for attribute map operations.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public interface AttributeMapListener extends java.util.EventListener
{
    /**
     * Called when a value has been modified.
     *
     * @param map the attribute map
     * @param key the key
     * @param oldValue the original value
     * @param newValue the new value
     */
    public void attributeModified(AttributeMap map,
				  AttributeMapKey key,
				  Object oldValue,
				  Object newValue);
}
