/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * An AdjustmentListener which ensures that an Adjustable (e.g. a Scrollbar)
 * will "track" when the Adjustable expands.
 * For example, when a vertical scroll bar is at its bottom anchor,
 * the scrollbar will remain at the bottom.  When the vertical scroll bar
 * is at any other location, then no tracking will happen.
 * An instance of this class should only listen to one Adjustable as
 * it retains state information about the Adjustable it listens to.
 *
 * @author Richard Wan
 */

// Contributed by ThoughtWorks Inc.

public class TrackingAdjustmentListener implements AdjustmentListener {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------

  protected int _lastMaximum = -1;

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  public void adjustmentValueChanged(AdjustmentEvent e) {
    Adjustable bar = e.getAdjustable();
    int currentMaximum = bar.getMaximum();
    if (bar.getMaximum() == _lastMaximum) {
      return; // nothing to do, the adjustable has not expanded
    }
    int bottom = bar.getValue() + bar.getVisibleAmount();

    if (bottom + bar.getUnitIncrement() >= _lastMaximum) {
      bar.setValue(bar.getMaximum()); // use the most recent maximum
    }
    _lastMaximum = currentMaximum;
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces
  //--------------------------------------------------------------------------
}

