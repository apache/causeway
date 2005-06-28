/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5;

import org.apache.log4j.lf5.viewer.LogBrokerMonitor;

/**
 * Starts an instance of the LogFactor5 console for off-line viewing.
 *
 * @author Brad Marlborough
 * @author Richard Hurst
 */

// Contributed by ThoughtWorks Inc.

public class StartLogFactor5 {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  /**
   * Main - starts a an instance of the LogFactor5 console and configures
   * the console settings.
   */
  public final static void main(String[] args) {

    LogBrokerMonitor monitor = new LogBrokerMonitor(
        LogLevel.getLog4JLevels());

    monitor.setFrameSize(LF5Appender.getDefaultMonitorWidth(),
        LF5Appender.getDefaultMonitorHeight());
    monitor.setFontSize(12);
    monitor.show();

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


