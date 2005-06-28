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
 * <code>AppenderFinalizer</code> has a single method that will finalize
 * resources associated with a <code>LogBrokerMonitor</code> in the event
 * that the <code>LF5Appender</code> class is destroyed, and the class loader
 * is garbage collected.
 *
 * @author Brent Sprecher
 */

// Contributed by ThoughtWorks Inc.

public class AppenderFinalizer {
  //--------------------------------------------------------------------------
  // Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  // Protected Variables:
  //--------------------------------------------------------------------------

  protected LogBrokerMonitor _defaultMonitor = null;

  //--------------------------------------------------------------------------
  // Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  // Constructors:
  //--------------------------------------------------------------------------

  public AppenderFinalizer(LogBrokerMonitor defaultMonitor) {
    _defaultMonitor = defaultMonitor;
  }
  //--------------------------------------------------------------------------
  // Public Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  // Protected Methods:
  //--------------------------------------------------------------------------

  /**
   * @throws java.lang.Throwable
   */
  protected void finalize() throws Throwable {
    System.out.println("Disposing of the default LogBrokerMonitor instance");
    _defaultMonitor.dispose();
  }

  //--------------------------------------------------------------------------
  // Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  // Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}