/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */

package org.apache.log4j.lf5;

import org.apache.log4j.spi.ThrowableInformation;

/**
 * A <code>Log4JLogRecord</code> encapsulates
 * the details of your log4j <code>LoggingEvent</code> in a format usable
 * by the <code>LogBrokerMonitor</code>.
 *
 * @author Brent Sprecher
 */

// Contributed by ThoughtWorks Inc.

public class Log4JLogRecord extends LogRecord {
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

  /**
   * Constructs an instance of a <code>Log4JLogRecord</code>.
   */
  public Log4JLogRecord() {
  }

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------
  /**
   * Determines which <code>Priority</code> levels will
   * be displayed in colored font when the <code>LogMonitorAppender</code>
   * renders this log message. By default, messages will be colored
   * red if they are of <code>Priority</code> ERROR or FATAL.
   *
   * @return true if the log level is ERROR or FATAL.
   */
  public boolean isSevereLevel() {
    boolean isSevere = false;

    if (LogLevel.ERROR.equals(getLevel()) ||
        LogLevel.FATAL.equals(getLevel())) {
      isSevere = true;
    }

    return isSevere;
  }

  /**
   * Set stack trace information associated with this Log4JLogRecord.
   * When this method is called, the stack trace in a
   * String-based format is made
   * available via the getThrownStackTrace() method.
   *
   * @param throwableInfo An org.apache.log4j.spi.ThrowableInformation to
   * associate with this Log4JLogRecord.
   * @see #getThrownStackTrace()
   */
  public void setThrownStackTrace(ThrowableInformation throwableInfo) {
    String[] stackTraceArray = throwableInfo.getThrowableStrRep();

    StringBuffer stackTrace = new StringBuffer();
    String nextLine;

    for (int i = 0; i < stackTraceArray.length; i++) {
      nextLine = stackTraceArray[i] + "\n";
      stackTrace.append(nextLine);
    }

    _thrownStackTrace = stackTrace.toString();
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}



