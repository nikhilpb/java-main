package com.nikhilpb.matching;

import com.moallemi.util.PropertySet;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class OnlineMatchingModel extends MatchingModel {
    // number of supply attributes

    /**
     * Constructor.
     *
     * @param props set of properties
     */
    public OnlineMatchingModel(PropertySet props) {
        modelType = "online";
        init(props);
    }
}