/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j;

import java.io.OutputStreamWriter;
import org.apache.log4j.helpers.LogLog;

/**
  * ConsoleAppender appends log events to <code>System.out</code> or
  * <code>System.err</code> using a layout specified by the user. The
  * default target is <code>System.out</code>.
  *
  * @author Ceki G&uuml;lc&uuml; 
  * @since 1.1 */
public class ConsoleAppender extends WriterAppender {

  public static final String SYSTEM_OUT = "System.out";
  public static final String SYSTEM_ERR = "System.err";

  protected String target = SYSTEM_OUT;

  /**
     The default constructor does nothing.
   */
  public ConsoleAppender() {
  }

  public ConsoleAppender(Layout layout) {
    this(layout, SYSTEM_OUT);
  }

  public ConsoleAppender(Layout layout, String target) {
    this.layout = layout;

    if (SYSTEM_OUT.equals(target)) {
      setWriter(new OutputStreamWriter(System.out));
    } else if (SYSTEM_ERR.equalsIgnoreCase(target)) {
      setWriter(new OutputStreamWriter(System.err));
    } else {
      targetWarn(target);
    }
  }

  /**
   *  Sets the value of the <b>Target</b> option. Recognized values
   *  are "System.out" and "System.err". Any other value will be
   *  ignored.  
   * */
  public
  void setTarget(String value) {
    String v = value.trim();

    if (SYSTEM_OUT.equalsIgnoreCase(v)) {
      target = SYSTEM_OUT;
    } else if (SYSTEM_ERR.equalsIgnoreCase(v)) {
      target = SYSTEM_ERR;
    } else {
      targetWarn(value);
    }
  }

  /**
   * Returns the current value of the <b>Target</b> property. The
   * default value of the option is "System.out".
   *
   * See also {@link #setTarget}.
   * */
  public
  String getTarget() {
    return target;
  }

  void targetWarn(String val) {
    LogLog.warn("["+val+"] should be System.out or System.err.");
    LogLog.warn("Using previously set target, System.out by default.");
  }

  public
  void activateOptions() {
    if(target.equals(SYSTEM_OUT)) {
      setWriter(new OutputStreamWriter(System.out));
    } else {
      setWriter(new OutputStreamWriter(System.err));
    }
  }

  /**
   *  This method overrides the parent {@link
   *  WriterAppender#closeWriter} implementation to do nothing because
   *  the console stream is not ours to close.
   * */
  protected
  final
  void closeWriter() {
  }
}
