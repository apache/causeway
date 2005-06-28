/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.net.test;

import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.net.SocketAppender;
import org.apache.log4j.Priority;
import org.apache.log4j.NDC;
import java.io.InputStreamReader;

public class SocketMin {

  static Category cat = Category.getInstance(SyslogMin.class.getName());
  static SocketAppender s;

  public
  static
  void main(String argv[]) {
    if(argv.length == 3)
      init(argv[0], argv[1]);
    else
      usage("Wrong number of arguments.");

    NDC.push("some context");
    if(argv[2].equals("true"))
      loop();
    else
      test();

    s.close();
  }

  static
  void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + SocketMin.class
		       + " host port true|false");
    System.exit(1);
  }

  static
  void init(String host, String portStr) {
    Category root = Category.getRoot();
    BasicConfigurator.configure();
    try {
      int port   = Integer.parseInt(portStr);
      cat.info("Creating socket appender ("+host+","+port+").");
      s = new SocketAppender(host, port);
      s.setName("S");
      root.addAppender(s);
    }
    catch(java.lang.NumberFormatException e) {
      e.printStackTrace();
      usage("Could not interpret port number ["+ portStr +"].");
    }
    catch(Exception e) {
      System.err.println("Could not start!");
      e.printStackTrace();
      System.exit(1);
    }
  }

  static
  void loop() {
    Category root = Category.getRoot();
    InputStreamReader in = new InputStreamReader(System.in);
    System.out.println("Type 'q' to quit");
    int i;
    int k = 0;
    while (true) {
      cat.debug("Message " + k++);
      cat.info("Message " + k++);
      cat.warn("Message " + k++);
      cat.error("Message " + k++, new Exception("Just testing"));
      try {i = in.read(); }
      catch(Exception e) { return; }
      if(i == -1) break;
      if(i == 'q') break;
      if(i == 'r') {
	System.out.println("Removing appender S");
	root.removeAppender("S");
      }
    }
  }

  static
  void test() {
    int i  = 0;
    cat.debug( "Message " + i++);
    cat.info( "Message " + i++);
    cat.warn( "Message " + i++);
    cat.error( "Message " + i++);
    cat.log(Priority.FATAL, "Message " + i++);
    cat.debug("Message " + i++,  new Exception("Just testing."));
  }
}
