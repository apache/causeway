/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.helpers;

/**
   This class used to output log statements from within the log4j package.

   <p>Log4j components cannot make log4j logging calls. However, it is
   sometimes useful for the user to learn about what log4j is
   doing. You can enable log4j internal logging by defining the
   <b>log4j.configDebug</b> variable.

   <p>All log4j internal debug calls go to <code>System.out</code>
   where as internal error messages are sent to
   <code>System.err</code>. All internal messages are prepended with
   the string "log4j: ".
   
   @since 0.8.2
   @author Ceki G&uuml;lc&uuml;
*/
public class LogLog {

  /**
     Defining this value makes log4j print log4j-internal debug
     statements to <code>System.out</code>.
     
    <p> The value of this string is <b>log4j.debug</b>.
    
    <p>Note that the search for all option names is case sensitive.  */
  public static final String DEBUG_KEY="log4j.debug";

 
  /**
     Defining this value makes log4j components print log4j-internal
     debug statements to <code>System.out</code>.
     
    <p> The value of this string is <b>log4j.configDebug</b>.
    
    <p>Note that the search for all option names is case sensitive.  

    @deprecated Use {@link #DEBUG_KEY} instead.
  */
  public static final String CONFIG_DEBUG_KEY="log4j.configDebug";

  protected static boolean debugEnabled = false;  

  /**
     In quietMode not even errors generate any output.
   */
  private static boolean quietMode = false;

  private static final String PREFIX = "log4j: ";
  private static final String ERR_PREFIX = "log4j:ERROR ";
  private static final String WARN_PREFIX = "log4j:WARN ";

  static {
    String key = OptionConverter.getSystemProperty(DEBUG_KEY, null);

    if(key == null) {
      key = OptionConverter.getSystemProperty(CONFIG_DEBUG_KEY, null);
    }

    if(key != null) { 
      debugEnabled = OptionConverter.toBoolean(key, true);
    }
  }

  /**
     Allows to enable/disable log4j internal logging.
   */
  static
  public
  void setInternalDebugging(boolean enabled) {
    debugEnabled = enabled;
  }

  /**
     This method is used to output log4j internal debug
     statements. Output goes to <code>System.out</code>.
  */
  public
  static
  void debug(String msg) {
    if(debugEnabled && !quietMode) {
      System.out.println(PREFIX+msg);
    }
  }

  /**
     This method is used to output log4j internal debug
     statements. Output goes to <code>System.out</code>.
  */
  public
  static
  void debug(String msg, Throwable t) {
    if(debugEnabled && !quietMode) {
      System.out.println(PREFIX+msg);
      if(t != null)
	t.printStackTrace(System.out);
    }
  }
  

  /**
     This method is used to output log4j internal error
     statements. There is no way to disable error statements.
     Output goes to <code>System.err</code>.
  */
  public
  static
  void error(String msg) {
    if(quietMode)
      return;
    System.err.println(ERR_PREFIX+msg);
  }  

  /**
     This method is used to output log4j internal error
     statements. There is no way to disable error statements.
     Output goes to <code>System.err</code>.  
  */
  public
  static
  void error(String msg, Throwable t) {
    if(quietMode)
      return;

    System.err.println(ERR_PREFIX+msg);
    if(t != null) {
      t.printStackTrace();
    }
  }  

  /**
     In quite mode no LogLog generates strictly no output, not even
     for errors. 

     @param quietMode A true for not
  */
  public
  static
  void setQuietMode(boolean quietMode) {
    LogLog.quietMode = quietMode;
  }

  /**
     This method is used to output log4j internal warning
     statements. There is no way to disable warning statements.
     Output goes to <code>System.err</code>.  */
  public
  static
  void warn(String msg) {
    if(quietMode)
      return;

    System.err.println(WARN_PREFIX+msg);
  }  

  /**
     This method is used to output log4j internal warnings. There is
     no way to disable warning statements.  Output goes to
     <code>System.err</code>.  */
  public
  static
  void warn(String msg, Throwable t) {
    if(quietMode)
      return;

    System.err.println(WARN_PREFIX+msg);
    if(t != null) {
      t.printStackTrace();
    }
  }  
}
