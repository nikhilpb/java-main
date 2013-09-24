package com.moallemi.util.data;

/**
 * An exception generated when trying to fetch an unknown attribute.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class AttributeUnknownException extends RuntimeException {
    public AttributeUnknownException() { super(); }
    public AttributeUnknownException(Throwable t) { super(t); }
    public AttributeUnknownException(String m, Throwable t) { super(m, t); }
    public AttributeUnknownException(String m) { super(m); }
}
