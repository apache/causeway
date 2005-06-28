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
import org.apache.log4j.helpers.OptionConverter;

/**
   This is a very simple filter based on string matching.
   

   <p>The filter admits two options <b>StringToMatch</b> and
   <b>AcceptOnMatch</b>. If there is a match between the value of the
   StringToMatch option and the message of the {@link LoggingEvent},
   then the {@link #decide} method returns {@link Filter#ACCEPT} if
   the <b>AcceptOnMatch</b> option value is true, if it is false then
   {@link Filter#DENY} is returned. If there is no match, {@link
   Filter#NEUTRAL} is returned.

   <p>See configuration files <a
   href="../xml/doc-files/test6.xml">test6.xml</a>, <a
   href="../xml/doc-files/test7.xml">test7.xml</a>, <a
   href="../xml/doc-files/test8.xml">test8.xml</a>, <a
   href="../xml/doc-files/test9.xml">test9.xml</a>, and <a
   href="../xml/doc-files/test10.xml">test10.xml</a> for examples of
   seeting up a <code>StringMatchFilter</code>.


   @author Ceki G&uuml;lc&uuml;

   @since 0.9.0 */
public class StringMatchFilter extends Filter {
  
  /**
     @deprecated Options are now handled using the JavaBeans paradigm.
     This constant is not longer needed and will be removed in the
     <em>near</em> term.
   */
  public static final String STRING_TO_MATCH_OPTION = "StringToMatch";

  /**
     @deprecated Options are now handled using the JavaBeans paradigm.
     This constant is not longer needed and will be removed in the
     <em>near</em> term.
   */
  public static final String ACCEPT_ON_MATCH_OPTION = "AcceptOnMatch";
  
  boolean acceptOnMatch = true;
  String stringToMatch;
  
  /**
     @deprecated We now use JavaBeans introspection to configure
     components. Options strings are no longer needed.
  */
  public
  String[] getOptionStrings() {
    return new String[] {STRING_TO_MATCH_OPTION, ACCEPT_ON_MATCH_OPTION};
  }

  /**
     @deprecated Use the setter method for the option directly instead
     of the generic <code>setOption</code> method. 
  */
  public
  void setOption(String key, String value) { 
    
    if(key.equalsIgnoreCase(STRING_TO_MATCH_OPTION)) {
      stringToMatch = value;
    } else if (key.equalsIgnoreCase(ACCEPT_ON_MATCH_OPTION)) {
      acceptOnMatch = OptionConverter.toBoolean(value, acceptOnMatch);
    }
  }
  
  public
  void setStringToMatch(String s) {
    stringToMatch = s;
  }
  
  public
  String getStringToMatch() {
    return stringToMatch;
  }
  
  public
  void setAcceptOnMatch(boolean acceptOnMatch) {
    this.acceptOnMatch = acceptOnMatch;
  }
  
  public
  boolean getAcceptOnMatch() {
    return acceptOnMatch;
  }

  /**
     Returns {@link Filter#NEUTRAL} is there is no string match.
   */
  public
  int decide(LoggingEvent event) {
    String msg = event.getRenderedMessage();

    if(msg == null ||  stringToMatch == null)
      return Filter.NEUTRAL;
    

    if( msg.indexOf(stringToMatch) == -1 ) {
      return Filter.NEUTRAL;
    } else { // we've got a match
      if(acceptOnMatch) {
	return Filter.ACCEPT;
      } else {
	return Filter.DENY;
      }
    }
  }
}
