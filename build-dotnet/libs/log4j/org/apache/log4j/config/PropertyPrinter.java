/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */

package org.apache.log4j.config;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;

/**
   Prints the configuration of the log4j default hierarchy
   (which needs to be auto-initialized) as a propoperties file
   on a {@link PrintWriter}.
   
   @author  Anders Kristensen
 */
public class PropertyPrinter implements PropertyGetter.PropertyCallback {
  protected int numAppenders = 0;
  protected Hashtable appenderNames = new Hashtable();
  protected Hashtable layoutNames   = new Hashtable();
  protected PrintWriter out;
  protected boolean doCapitalize;
  
  public
  PropertyPrinter(PrintWriter out) {
    this(out, false);
  }
  
  public
  PropertyPrinter(PrintWriter out, boolean doCapitalize) {
    this.out = out;
    this.doCapitalize = doCapitalize;
    
    print(out);
    out.flush();
  }
  
  protected
  String genAppName() {
    return "A" + numAppenders++;
  }
  
  /**
     Returns true if the specified appender name is considered to have
     been generated, i.e. if it is of the form A[0-9]+.
  */
  protected
  boolean isGenAppName(String name) {
    if (name.length() < 2 || name.charAt(0) != 'A') return false;
    
    for (int i = 0; i < name.length(); i++) {
      if (name.charAt(i) < '0' || name.charAt(i) > '9') return false;
    }
    return true;
  }
  
  /**
   * Prints the configuration of the default log4j hierarchy as a Java
   * properties file on the specified Writer.
   * 
   * <p>N.B. print() can be invoked only once!
   */
  public
  void print(PrintWriter out) {
    printOptions(out, Category.getRoot());
    
    Enumeration cats = Category.getCurrentCategories();
    while (cats.hasMoreElements()) {
      printOptions(out, (Category) cats.nextElement());
    }
  }
  
  protected
  void printOptions(PrintWriter out, Category cat) {
    Enumeration appenders = cat.getAllAppenders();
    Level prio = cat.getLevel();
    String appenderString = (prio == null ? "" : prio.toString());
    
    while (appenders.hasMoreElements()) {
      Appender app = (Appender) appenders.nextElement();
      String name;
      
      if ((name = (String) appenderNames.get(app)) == null) {
      
        // first assign name to the appender
        if ((name = app.getName()) == null || isGenAppName(name)) {
            name = genAppName();
        }
        appenderNames.put(app, name);
        
        printOptions(out, app, "log4j.appender."+name);
        if (app.getLayout() != null) {
          printOptions(out, app.getLayout(), "log4j.appender."+name+".layout");
        }
      }
      appenderString += ", " + name;
    }
    String catKey = (cat == Category.getRoot())
        ? "log4j.rootCategory"
        : "log4j.category." + cat.getName();
    if (appenderString != "") {
      out.println(catKey + "=" + appenderString);
    }
  }
  
  protected
  void printOptions(PrintWriter out, Object obj, String fullname) {
    out.println(fullname + "=" + obj.getClass().getName());
    PropertyGetter.getProperties(obj, this, fullname + ".");
  }
  
  public void foundProperty(Object obj, String prefix, String name, Object value) {
    // XXX: Properties encode value.toString()
    if (obj instanceof Appender && "name".equals(name)) {
      return;
    }
    if (doCapitalize) {
      name = capitalize(name);
    }
    out.println(prefix + name + "=" + value.toString());
  }
  
  public static String capitalize(String name) {
    if (Character.isLowerCase(name.charAt(0))) {
      if (name.length() == 1 || Character.isLowerCase(name.charAt(1))) {
        StringBuffer newname = new StringBuffer(name);
        newname.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return newname.toString();
      }
    }
    return name;
  }
  
  // for testing
  public static void main(String[] args) {
    new PropertyPrinter(new PrintWriter(System.out));
  }
}
