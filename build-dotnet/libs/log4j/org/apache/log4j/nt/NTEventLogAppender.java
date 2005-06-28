/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.nt;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;

import java.io.*;


/**
   Append to the NT event log system.

   <p><b>WARNING</b> This appender can only be installed and used on a
   Windows system.

   <p>Do not forget to place the file NTEventLogAppender.dll in a
   directory that is on the PATH of the Windows system. Otherwise, you
   will get a java.lang.UnsatisfiedLinkError.

   @author <a href="mailto:cstaylor@pacbell.net">Chris Taylor</a>
   @author <a href="mailto:jim_cakalic@na.biomerieux.com">Jim Cakalic</a> */
public class NTEventLogAppender extends AppenderSkeleton {
  private int _handle = 0;

  private String source = null;
  private String server = null;

  private static final int FATAL  = Level.FATAL.toInt();
  private static final int ERROR  = Level.ERROR.toInt();
  private static final int WARN   = Level.WARN.toInt();
  private static final int INFO   = Level.INFO.toInt();
  private static final int DEBUG  = Level.DEBUG.toInt();

  public NTEventLogAppender() {
    this(null, null, null);
  }

  public NTEventLogAppender(String source) {
    this(null, source, null);
  }

  public NTEventLogAppender(String server, String source) {
    this(server, source, null);
  }

  public NTEventLogAppender(Layout layout) {
    this(null, null, layout);
  }

  public NTEventLogAppender(String source, Layout layout) {
    this(null, source, layout);
  }

  public NTEventLogAppender(String server, String source, Layout layout) {
    if (source == null) {
      source = "Log4j";
    }
    if (layout == null) {
      this.layout = new TTCCLayout();
    } else {
      this.layout = layout;
    }

    try {
      _handle = registerEventSource(server, source);
    } catch (Exception e) {
      e.printStackTrace();
      _handle = 0;
    }
  }

  public
  void close() {
    // unregister ...
  }

  public
  void activateOptions() {
    if (source != null) {
      try {
	_handle = registerEventSource(server, source);
      } catch (Exception e) {
	LogLog.error("Could not register event source.", e);
	_handle = 0;
      }
    }
  }


  public void append(LoggingEvent event) {

    StringBuffer sbuf = new StringBuffer();

    sbuf.append(layout.format(event));
    if(layout.ignoresThrowable()) {
      String[] s = event.getThrowableStrRep();
      if (s != null) {
	int len = s.length;
	for(int i = 0; i < len; i++) {
	  sbuf.append(s[i]);
	}
      }
    }
    // Normalize the log message level into the supported categories
    int nt_category = event.getLevel().toInt();

    // Anything above FATAL or below DEBUG is labeled as INFO.
    //if (nt_category > FATAL || nt_category < DEBUG) {
    //  nt_category = INFO;
    //}
    reportEvent(_handle, sbuf.toString(), nt_category);
  }


  public
  void finalize() {
    deregisterEventSource(_handle);
    _handle = 0;
  }

  /**
     The <b>Source</b> option which names the source of the event. The
     current value of this constant is <b>Source</b>.
   */
  public
  void setSource(String source) {
    this.source = source.trim();
  }

  public
  String getSource() {
    return source;
  }

/**
     The <code>NTEventLogAppender</code> requires a layout. Hence,
     this method always returns <code>true</code>. */
  public
  boolean requiresLayout() {
    return true;
  }

  native private int registerEventSource(String server, String source);
  native private void reportEvent(int handle, String message, int level);
  native private void deregisterEventSource(int handle);

  static {
    System.loadLibrary("NTEventLogAppender");
  }
}
