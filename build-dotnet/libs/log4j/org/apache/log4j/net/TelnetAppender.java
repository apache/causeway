/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.net;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;

/**
  <p>The TelnetAppender is a log4j appender that specializes in
  writing to a read-only socket.  The output is provided in a
  telnet-friendly way so that a log can be monitored over TCP/IP.
  Clients using telnet connect to the socket and receive log data.
  This is handy for remote monitoring, especially when monitoring a
  servlet.

  <p>Here is a list of the available configuration options:

  <table border=1>
   <tr>
   <th>Name</th>
   <th>Requirement</th>
   <th>Description</th>
   <th>Sample Value</th>
   </tr>

   <tr>
   <td>Port</td>
   <td>optional</td>
   <td>This parameter determines the port to use for announcing log events.  The default port is 23 (telnet).</td>
   <td>5875</td>
   </table>

   @author <a HREF="mailto:jay@v-wave.com">Jay Funnell</a>
*/

public class TelnetAppender extends AppenderSkeleton {

  private SocketHandler sh;
  private int port = 23;

  /** 
      This appender requires a layout to format the text to the
      attached client(s). */
  public boolean requiresLayout() {
    return true;
  }

  /** all of the options have been set, create the socket handler and
      wait for connections. */
  public void activateOptions() {
    try {
      sh = new SocketHandler(port);
      sh.start();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public
  int getPort() {
    return port;
  }

  public
  void setPort(int port) {
    this.port = port;
  }


  /** shuts down the appender. */
  public void close() {
    sh.finalize();
  }

  /** Handles a log event.  For this appender, that means writing the
    message to each connected client.  */
  protected void append(LoggingEvent event) {
    sh.send(this.layout.format(event));
    if(layout.ignoresThrowable()) {
      String[] s = event.getThrowableStrRep();
      if (s != null) {
	int len = s.length;
	for(int i = 0; i < len; i++) {
	  sh.send(s[i]);
	  sh.send(Layout.LINE_SEP);
	}
      }
    }
  }

  //---------------------------------------------------------- SocketHandler:

  /** The SocketHandler class is used to accept connections from
      clients.  It is threaded so that clients can connect/disconnect
      asynchronously. */
  protected class SocketHandler extends Thread {

    private boolean done = false;
    private Vector writers = new Vector();
    private Vector connections = new Vector();
    private ServerSocket serverSocket;
    private int MAX_CONNECTIONS = 20;

    /** make sure we close all network connections when this handler is destroyed. */
    public void finalize() {
      for(Enumeration e = connections.elements();e.hasMoreElements();) {
        try {
          ((Socket)e.nextElement()).close();
        } catch(Exception ex) {
        }
      }
      try {
        serverSocket.close();
      } catch(Exception ex) {
      }
      done = true;
    }

    /** sends a message to each of the clients in telnet-friendly output. */
    public void send(String message) {
      Enumeration ce = connections.elements();
      for(Enumeration e = writers.elements();e.hasMoreElements();) {
        Socket sock = (Socket)ce.nextElement();
        PrintWriter writer = (PrintWriter)e.nextElement();
        writer.print(message);
        if(writer.checkError()) {
          // The client has closed the connection, remove it from our list:
          connections.removeElement(sock);
          writers.removeElement(writer);
        }
      }
    }

    /** 
	Continually accepts client connections.  Client connections
        are refused when MAX_CONNECTIONS is reached. 
    */
    public void run() {
      while(!done) {
        try {
          Socket newClient = serverSocket.accept();
          PrintWriter pw = new PrintWriter(newClient.getOutputStream());
          if(connections.size() < MAX_CONNECTIONS) {
            connections.addElement(newClient);
            writers.addElement(pw);
            pw.print("TelnetAppender v1.0 (" + connections.size() 
		     + " active connections)\r\n\r\n");
            pw.flush();
          } else {
            pw.print("Too many connections.\r\n");
            pw.flush();
            newClient.close();
          }
        } catch(Exception e) {
          LogLog.error("Encountered error while in SocketHandler loop.", e);
        }
      }
    }

    public SocketHandler(int port) throws IOException {
      serverSocket = new ServerSocket(port);
    }

  }
}
