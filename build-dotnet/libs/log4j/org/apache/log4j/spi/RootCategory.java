/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.spi;

import  org.apache.log4j.*;
import  org.apache.log4j.helpers.LogLog;

// Contibutors: Mathias Bogaert

/**
   RootCategory sits at the top of the category hierachy. It is a
   regular category except that it provides several guarantees.

   <p>First, it cannot be assigned a <code>null</code>
   priority. Second, since root category cannot have a parent, the
   {@link #getChainedLevel} method always returns the value of the
   level field without walking the hierarchy.

   @author Ceki G&uuml;lc&uuml;

 */
final public class RootCategory extends Logger {

  /**
     The root category names itself as "root". However, the root
     category cannot be retrieved by name.  
  */
  public
  RootCategory(Level level) {
    super("root");
    setLevel(level);
  }

  
  /**
     Return the assigned level value without walking the category
     hierarchy.
  */
  final
  public 
  Level getChainedLevel() {
    return level;
  }

  /**
     Setting a null value to the level of the root category may have catastrophic
     results. We prevent this here.

     @since 0.8.3 */
  final  
  public
  void setLevel(Level level) {
    if(level == null) {
      LogLog.error("You have tried to set a null level to root.",
		   new Throwable());
    }
    else {
      this.level = level;
    }
  }

  final  
  public
  void setPriority(Level level) {
    setLevel(level);
  }

  
}
