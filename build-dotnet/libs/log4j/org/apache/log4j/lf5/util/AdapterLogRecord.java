/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.util;

import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogRecord;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * <p>A LogRecord to be used with the LogMonitorAdapter</p>
 *
 * @author Richard Hurst
 */

// Contributed by ThoughtWorks Inc.

public class AdapterLogRecord extends LogRecord {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------
  private static LogLevel severeLevel = null;

  private static StringWriter sw = new StringWriter();
  private static PrintWriter pw = new PrintWriter(sw);

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------
  public AdapterLogRecord() {
    super();
  }

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------
  public void setCategory(String category) {
    super.setCategory(category);
    super.setLocation(getLocationInfo(category));
  }

  public boolean isSevereLevel() {
    if (severeLevel == null) return false;
    return severeLevel.equals(getLevel());
  }

  public static void setSevereLevel(LogLevel level) {
    severeLevel = level;
  }

  public static LogLevel getSevereLevel() {
    return severeLevel;
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------
  protected String getLocationInfo(String category) {
    String stackTrace = stackTraceToString(new Throwable());
    String line = parseLine(stackTrace, category);
    return line;
  }

  protected String stackTraceToString(Throwable t) {
    String s = null;

    synchronized (sw) {
      t.printStackTrace(pw);
      s = sw.toString();
      sw.getBuffer().setLength(0);
    }

    return s;
  }

  protected String parseLine(String trace, String category) {
    int index = trace.indexOf(category);
    if (index == -1) return null;
    trace = trace.substring(index);
    trace = trace.substring(0, trace.indexOf(")") + 1);
    return trace;
  }
  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces
  //--------------------------------------------------------------------------
}

