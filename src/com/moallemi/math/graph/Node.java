package com.moallemi.math.graph;

import com.moallemi.util.data.AttributeMap;

/**
 * A generic class for graph nodes or vertices.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class Node {
    private AttributeMap attributeMap = new AttributeMap();

    /**
     * Get attributes of this node.
     *
     * @return the attibute map
     */
    public AttributeMap getAttributeMap() { return attributeMap; }

}
