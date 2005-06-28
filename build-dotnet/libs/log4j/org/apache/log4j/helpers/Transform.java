/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.helpers;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.LocationInfo;

/**
   Utility class for transforming strings.

   @author Ceki G&uuml;lc&uuml;
   @author Michael A. McAngus 
 */
public class Transform {

   private static final String CDATA_START  = "<![CDATA[";
   private static final String CDATA_END    = "]]>";
   private static final String CDATA_PSEUDO_END = "]]&gt;";
   private static final String CDATA_EMBEDED_END = CDATA_END + CDATA_PSEUDO_END + CDATA_START;
   private static final int CDATA_END_LEN = CDATA_END.length();

  /**
   * This method takes a string which may contain HTML tags (ie,
   * &lt;b&gt;, &lt;table&gt;, etc) and replaces any '<' and '>'
   * characters with respective predefined entity references.
   *
   * @param input The text to be converted.
   * @return The input string with the characters '<' and '>' replaced with
   *  &amp;lt; and &amp;gt; respectively.  
   * */
  static public String escapeTags(String input) {
    //Check if the string is null or zero length -- if so, return
    //what was sent in.

    if( input == null || input.length() == 0 ) {
      return input;
    }

    //Use a StringBuffer in lieu of String concatenation -- it is
    //much more efficient this way.

    StringBuffer buf = new StringBuffer(input.length() + 6);
    char ch = ' ';

    int len = input.length();
    for(int i=0; i < len; i++) {
      ch = input.charAt(i);
      if(ch == '<') {
	buf.append("&lt;");
      } else if(ch == '>') {
	buf.append("&gt;");
      } else {
	buf.append(ch);
      }
    }
    return buf.toString();
  }

  /**
  * Ensures that embeded CDEnd strings (]]>) are handled properly
  * within message, NDC and throwable tag text.
  *
  * @param buf StringBuffer holding the XML data to this point.  The
  * initial CDStart (<![CDATA[) and final CDEnd (]]>) of the CDATA
  * section are the responsibility of the calling method.
  * @param str The String that is inserted into an existing CDATA Section within buf.  
  * */
  static public void appendEscapingCDATA(StringBuffer buf, String str) {
    int end = str.indexOf(CDATA_END);
    
    if (end < 0) {
      buf.append(str);
      return;
    }
    
    int start = 0;
    while (end > -1) {
      buf.append(str.substring(start,end));
      buf.append(CDATA_EMBEDED_END);
      start = end + CDATA_END_LEN;
      if (start < str.length()) {
	end = str.indexOf(CDATA_END, start);
      } else {
	return;
      }
    }
    
    buf.append(str.substring(start));
  }
}
