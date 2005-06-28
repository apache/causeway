/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.helpers;


/**
   FormattingInfo instances contain the information obtained when parsing
   formatting modifiers in conversion modifiers.

   @author <a href=mailto:jim_cakalic@na.biomerieux.com>Jim Cakalic</a>
   @author Ceki G&uuml;lc&uuml;

   @since 0.8.2   
 */
public class FormattingInfo {
  int min = -1;
  int max = 0x7FFFFFFF;
  boolean leftAlign = false;

  void reset() {
    min = -1;
    max = 0x7FFFFFFF;
    leftAlign = false;      
  }

  void dump() {
    LogLog.debug("min="+min+", max="+max+", leftAlign="+leftAlign);
  }
}
 
