/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.spi;

import org.apache.log4j.*;

/**
   Listen to events occuring within a {@link
   org.apache.log4j.Hierarchy Hierarchy}.

   @author Ceki G&uuml;lc&uuml;
   @since 1.2
   
 */
public interface HierarchyEventListener {

 
  //public
  //void categoryCreationEvent(Category cat);


  public
  void addAppenderEvent(Category cat, Appender appender);

  public
  void removeAppenderEvent(Category cat, Appender appender);


}
