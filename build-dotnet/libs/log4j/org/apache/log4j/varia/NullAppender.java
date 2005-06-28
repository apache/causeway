/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.varia;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.AppenderSkeleton;

/**
  * A NullAppender merely exists, it never outputs a message to any
  * device.  
  * @author Ceki G&uuml;lc&uml;
  */
public class NullAppender extends AppenderSkeleton {

  private static NullAppender instance = new NullAppender();

  public NullAppender() {
  }

  /** 
   * There are no options to acticate.
   * */
  public void activateOptions() {
  }

  /**
   * Whenever you can, use this method to retreive an instance instead
   * of instantiating a new one with <code>new</code>.
   * */
  public NullAppender getInstance() {
    return instance;
  }

  public void close() {
  }

  /**
   * Does not do anything. 
   * */
  public void doAppend(LoggingEvent event) {
  }

  /**
   * Does not do anything. 
   * */
  protected void append(LoggingEvent event) {
  }

  /**
    * NullAppenders do not need a layout.  
    * */
  public boolean requiresLayout() {
    return false;
  }
}
