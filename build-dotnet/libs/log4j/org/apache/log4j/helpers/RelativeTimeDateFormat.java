/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.helpers;

import java.util.Date;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.DateFormat;

/**
   Formats a {@link Date} by printing the number of milliseconds
   elapsed since the start of the application.  This is the fastest
   printing DateFormat in the package.
   
   @author Ceki G&uuml;lc&uuml;
   
   @since 0.7.5
*/
public class RelativeTimeDateFormat extends DateFormat {

  protected final long startTime;

  public
  RelativeTimeDateFormat() {
    this.startTime = System.currentTimeMillis();
  }
  
  /**
     Appends to <code>sbuf</code> the number of milliseconds elapsed
     since the start of the application. 
     
     @since 0.7.5
  */
  public
  StringBuffer format(Date date, StringBuffer sbuf,
		      FieldPosition fieldPosition) {
    //System.err.println(":"+ date.getTime() + " - " + startTime);
    return sbuf.append((date.getTime() - startTime));
  }

  /**
     This method does not do anything but return <code>null</code>.
   */
  public
  Date parse(java.lang.String s, ParsePosition pos) {
    return null;
  }  
}
