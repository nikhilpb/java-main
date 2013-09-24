package com.moallemi.matching;

import com.moallemi.util.PropertySet;

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

