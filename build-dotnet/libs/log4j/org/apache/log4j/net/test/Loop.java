/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.net.test;

import org.apache.log4j.*;
import org.apache.log4j.net.SocketAppender;

public class Loop {

  public static void main(String[] args) {
    
    
    Category root = Category.getRoot();
    Category cat = Category.getInstance(Loop.class.getName());

    if(args.length != 2) 
      usage("Wrong number of arguments.");     

    String host = args[0];
    int port = 0;

    try {
      port = Integer.valueOf(args[1]).intValue();
    }
    catch (NumberFormatException e) {
        usage("Argument [" + args[1]  + "] is not in proper int form.");
    }

    SocketAppender sa = new SocketAppender(host, port);
    Layout layout = new PatternLayout("%5p [%t] %x %c - %m\n");
    Appender so = new ConsoleAppender(layout, "System.out");
    root.addAppender(sa);
    root.addAppender(so);

    int i = 0;

    while(true) {
      NDC.push(""+ (i++));
      cat.debug("Debug message.");
      root.info("Info message.");
      NDC.pop();
    }

  }

  static
  void  usage(String msg) {
    System.err.println(msg);
    System.err.println(
      "Usage: java " +Loop.class.getName() + " host port");
    System.exit(1);
  }
    

}
