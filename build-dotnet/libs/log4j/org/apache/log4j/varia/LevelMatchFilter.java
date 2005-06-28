/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.varia;

import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.OptionConverter;

/**
   This is a very simple filter based on level matching.

   <p>The filter admits two options <b>LevelToMatch</b> and
   <b>AcceptOnMatch</b>. If there is an exact match between the value
   of the <b>LevelToMatch</b> option and the level of the {@link
   LoggingEvent}, then the {@link #decide} method returns {@link
   Filter#ACCEPT} in case the <b>AcceptOnMatch</b> option value is set
   to <code>true</code>, if it is <code>false</code> then {@link
   Filter#DENY} is returned. If there is no match, {@link
   Filter#NEUTRAL} is returned.

   @author Ceki G&uuml;lc&uuml;

   @since 1.2 */
public class LevelMatchFilter extends Filter {
  
  /**
     Do we return ACCEPT when a match occurs. Default is
     <code>true</code>.  */
  boolean acceptOnMatch = true;

  /**
   */
  Level levelToMatch;

 
  public
  void setLevelToMatch(String level) {
    levelToMatch = OptionConverter.toLevel(level, null);
  }
  
  public
  String getLevelToMatch() {
    return levelToMatch == null ? null : levelToMatch.toString();
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
     Return the decision of this filter.

     Returns {@link Filter#NEUTRAL} if the <b>LevelToMatch</b> option
     is not set or if there is not match.  Otherwise, if there is a
     match, then the returned decision is {@link Filter#ACCEPT} if the
     <b>AcceptOnMatch</b> property is set to <code>true</code>. The
     returned decision is {@link Filter#DENY} if the
     <b>AcceptOnMatch</b> property is set to false.

  */
  public
  int decide(LoggingEvent event) {
    if(this.levelToMatch == null) {
      return Filter.NEUTRAL;
    }
    
    boolean matchOccured = false;
    if(this.levelToMatch.equals(event.getLevel())) {
      matchOccured = true;
    } 

    if(matchOccured) {  
      if(this.acceptOnMatch)
	  return Filter.ACCEPT;
      else
	  return Filter.DENY;
    } else {
      return Filter.NEUTRAL;
    }
  }
}
