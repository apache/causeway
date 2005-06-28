/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.spi;

/**
   
   Implementions of this interface allow certain appenders to decide
   when to perform an appender specific action.

  <p>For example the {@link org.apache.log4j.net.SMTPAppender} sends
  an email when the {@link #isTriggeringEvent} method returns
  <code>true</code> and adds the event to an internal buffer when the
  returned result is <code>false</code>.

  @author Ceki G&uuml;lc&uuml;
  @since version 1.0
   
 */
public interface TriggeringEventEvaluator {
  
  /**
     Is this the triggering event?
   */
  public boolean isTriggeringEvent(LoggingEvent event);
}
