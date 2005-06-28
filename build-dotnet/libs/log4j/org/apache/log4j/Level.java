/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

// Contributors:  Kitching Simon <Simon.Kitching@orange.ch>
//                Nicholas Wolff

package org.apache.log4j;

/**
   Defines the minimum set of levels recognized by the system, that is
   <code>OFF</code>, <code>FATAL</code>, <code>ERROR</code>,
   <code>WARN</code>, <code>INFO</code, <code>DEBUG</code> and
   <code>ALL</code>.

   <p>The <code>Level</code> class may be subclassed to define a larger
   level set.

   @author Ceki G&uuml;lc&uuml;

 */
public class Level extends Priority {


  /**
     The <code>OFF</code> has the highest possible rank and is
     intended to turn off logging.  */
  final static public Level OFF = new Level(OFF_INT, "OFF", 0);

  /**
     The <code>FATAL</code> level designates very severe error
     events that will presumably lead the application to abort.
   */
  final static public Level FATAL = new Level(FATAL_INT, "FATAL", 0);

  /**
     The <code>ERROR</code> level designates error events that
     might still allow the application to continue running.  */
  final static public Level ERROR = new Level(ERROR_INT, "ERROR", 3);

  /**
     The <code>WARN</code> level designates potentially harmful situations.
  */
  final static public Level WARN  = new Level(WARN_INT, "WARN",  4);

  /**
     The <code>INFO</code> level designates informational messages
     that highlight the progress of the application at coarse-grained
     level.  */
  final static public Level INFO  = new Level(INFO_INT, "INFO",  6);

  /**
     The <code>DEBUG</code> Level designates fine-grained
     informational events that are most useful to debug an
     application.  */
  final static public Level DEBUG = new Level(DEBUG_INT, "DEBUG", 7);

  /**
     The <code>ALL</code> has the lowest possible rank and is intended to
     turn on all logging.  */
  final static public Level ALL = new Level(ALL_INT, "ALL", 7);

  /**
     Instantiate a Level object.
   */
  protected
  Level(int level, String levelStr, int syslogEquivalent) {
    super(level, levelStr, syslogEquivalent);
  }


  /**
     Convert the string passed as argument to a level. If the
     conversion fails, then this method returns {@link #DEBUG}. 
  */
  public
  static
  Level toLevel(String sArg) {
    return (Level) toLevel(sArg, Level.DEBUG);
  }

  /**
    Convert an integer passed as argument to a level. If the
    conversion fails, then this method returns {@link #DEBUG}.

  */
  public
  static
  Level toLevel(int val) {
    return (Level) toLevel(val, Level.DEBUG);
  }

  /**
    Convert an integer passed as argument to a level. If the
    conversion fails, then this method returns the specified default.
  */
  public
  static
  Level toLevel(int val, Level defaultLevel) {
    switch(val) {
    case ALL_INT: return ALL;
    case DEBUG_INT: return Level.DEBUG;
    case INFO_INT: return Level.INFO;
    case WARN_INT: return Level.WARN;
    case ERROR_INT: return Level.ERROR;
    case FATAL_INT: return Level.FATAL;
    case OFF_INT: return OFF;
    default: return defaultLevel;
    }
  }

  /**
     Convert the string passed as argument to a level. If the
     conversion fails, then this method returns the value of
     <code>defaultLevel</code>.  
  */
  public
  static
  Level toLevel(String sArg, Level defaultLevel) {                  
    if(sArg == null)
       return defaultLevel;
    
    String s = sArg.toUpperCase();

    if(s.equals("ALL")) return Level.ALL; 
    if(s.equals("DEBUG")) return Level.DEBUG; 
    //if(s.equals("FINE")) return Level.FINE; 
    if(s.equals("INFO"))  return Level.INFO;
    if(s.equals("WARN"))  return Level.WARN;  
    if(s.equals("ERROR")) return Level.ERROR;
    if(s.equals("FATAL")) return Level.FATAL;
    if(s.equals("OFF")) return Level.OFF;
    return defaultLevel;
  }
}
