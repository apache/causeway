/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.net;

import java.net.Socket;
import java.net.ServerSocket;

import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;


/**
 *  A simple {@link SocketNode} based server.
 *
   <pre>
   <b>Usage:</b> java org.apache.log4j.net.SimpleSocketServer port configFile

   where <em>port</em> is a part number where the server listens and
   <em>configFile</em> is a configuration file fed to the {@link
   PropertyConfigurator} or to {@link DOMConfigurator} if an XML file.
   </pre>
  *
  * @author  Ceki G&uuml;lc&uuml;
  *
  *  @since 0.8.4 
  * */
public class SimpleSocketServer  {

  static Category cat = Category.getInstance(SimpleSocketServer.class.getName());

  static int port;

  public
  static
  void main(String argv[]) {
    if(argv.length == 2) {
      init(argv[0], argv[1]);
    } else {
      usage("Wrong number of arguments.");
    }
    
    try {
      cat.info("Listening on port " + port);
      ServerSocket serverSocket = new ServerSocket(port);
      while(true) {
	cat.info("Waiting to accept a new client.");
	Socket socket = serverSocket.accept();
	cat.info("Connected to client at " + socket.getInetAddress());
	cat.info("Starting new socket node.");
	new Thread(new SocketNode(socket,
				  LogManager.getLoggerRepository())).start();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }


  static void  usage(String msg) {
    System.err.println(msg);
    System.err.println(
      "Usage: java " +SimpleSocketServer.class.getName() + " port configFile");
    System.exit(1);
  }

  static void init(String portStr, String configFile) {
    try {
      port = Integer.parseInt(portStr);
    } catch(java.lang.NumberFormatException e) {
      e.printStackTrace();
      usage("Could not interpret port number ["+ portStr +"].");
    }
   
    if(configFile.endsWith(".xml")) {
      new DOMConfigurator().configure(configFile);
    } else {
      new PropertyConfigurator().configure(configFile);
    }
  }
}
