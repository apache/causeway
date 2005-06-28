/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.APL file.  */

package org.apache.log4j.xml.test;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;
//import org.apache.log4j.xml.examples.ReportParserError;
//import org.apache.xerces.parsers.DOMParser;
//import java.io.FileInputStream;
//import org.xml.sax.InputSource;

/**
   @author Ceki G&uuml;lc&uuml;
*/
public class DOMTest {
  static Category cat = Category.getInstance(DOMTest.class.getName());


  public 
  static 
  void main(String argv[]) {

    if(argv.length == 1) 
      init(argv[0]);
    else 
      Usage("Wrong number of arguments.");

    test();
  }

  static
  void Usage(String msg) {
    System.err.println(msg);
    System.err.println( "Usage: java " + DOMTest.class.getName() +
			" configFile");
    System.exit(1);
  }
  
  static
  void init(String configFile) {
    DOMConfigurator.configure(configFile);
  }

  static
  void test() {
    int i = -1;
    Category root = Category.getRoot();
    
    cat.debug("Message " + ++i);
    root.debug("Message " + i);        

    cat.info ("Message " + ++i);
    root.info("Message " + i);        

    cat.warn ("Message " + ++i);
    root.warn("Message " + i);        

    cat.error("Message " + ++i);
    root.error("Message " + i);
    
    cat.log(Priority.FATAL, "Message " + ++i);
    root.log(Priority.FATAL, "Message " + i);    
    
    Exception e = new Exception("Just testing");
    cat.debug("Message " + ++i, e);
    root.debug("Message " + i, e);
    
    cat.error("Message " + ++i, e);
    root.error("Message " + i, e);    

    Category.shutdown();
  }
}
