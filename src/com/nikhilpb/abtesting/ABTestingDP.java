package com.nikhilpb.abtesting;

import java.util.Arrays;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class ABTestingDP {
  private DataModel model;

  public ABTestingDP(DataModel model) {
    this.model = model;
  }

  public ABState getBase() {
    ABState state = new ABState();
    state.setDelta(new double[model.dim() - 1]);
    state.setDiffCount(0);
    state.setDp(model.next());
    return state;
  }

  public ABState next(ABState prev, ABAction action) {
    final int actInt = action.toInt();
    ABState state = new ABState();
    state.setDiffCount(prev.getDiffCount() + actInt);
    final int len = model.dim() - 1;
    double delta[] = Arrays.copyOf(prev.getDelta(), len);
    state.setDelta(delta);
    for (int i = 0; i < len; ++i) {
      state.setDelta(i, state.getDelta(i) + actInt * prev.getDp().get(i));
    }
    state.setDp(model.next());
    return state;
  }
}
