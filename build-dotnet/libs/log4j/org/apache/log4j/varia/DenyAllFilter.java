/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.varia;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;


/**
   This filter drops all logging events. 

   <p>You can add this filter to the end of a filter chain to
   switch from the default "accept all unless instructed otherwise"
   filtering behaviour to a "deny all unless instructed otherwise"
   behaviour.


   @author Ceki G&uuml;lc&uuml;

   @since 0.9.0 */
public class DenyAllFilter extends Filter {

  /**
     Returns <code>null</code> as there are no options.
     
     @deprecated We now use JavaBeans introspection to configure
     components. Options strings are no longer needed.
  */
  public
  String[] getOptionStrings() {
    return null;
  }

  
  /**
     No options to set.
     
     @deprecated Use the setter method for the option directly instead
     of the generic <code>setOption</code> method. 
  */
  public
  void setOption(String key, String value) {
  }
  
  /**
     Always returns the integer constant {@link Filter#DENY}
     regardless of the {@link LoggingEvent} parameter.

     @param event The LoggingEvent to filter.
     @return Always returns {@link Filter#DENY}.
  */
  public
  int decide(LoggingEvent event) {
    return Filter.DENY;
  }
}

