
package org.apache.log4j.varia.test; 

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/**
   This test program reads a config file and attempts to log to the
   appenders specified as many times as specified by the second
   loopLength parameter.
   
   @author Ceki G&uuml;lc&uuml; */
public class Loop {

  static Category cat = Category.getInstance(Loop.class.getName());
  static int loopLength;

  public 
  static 
  void main(String argv[]) {

    if(argv.length == 2) 
      init(argv[0], argv[1]);
    else 
      usage("Wrong number of arguments.");
    test();
  }


  static
  void usage(String msg) {
    System.err.println(msg);
    System.err.println( "Usage: java " + Loop.class.getName() +
			"configFile loopLength");
    System.exit(1);
  }

  
  static
  void init(String configFile, String loopStr) {
    PropertyConfigurator.configure(configFile);
    try {
      loopLength   = Integer.parseInt(loopStr);      
    }
    catch(java.lang.NumberFormatException e) {
      e.printStackTrace();
      usage("Could not interpret loopLength ["+ loopStr +"].");
    }
  }

  static
  void test() {
    for(int i=0; i < loopLength; i++) {
      Thread.yield();
      cat.debug("MSG "+i);
    }
  }
}
