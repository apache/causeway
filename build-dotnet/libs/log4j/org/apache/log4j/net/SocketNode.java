/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.net;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;


import org.apache.log4j.*;
import org.apache.log4j.spi.*;

// Contributors:  Moses Hohman <mmhohman@rainbow.uchicago.edu>

/**
   Read {@link LoggingEvent} objects sent from a remote client using
   Sockets (TCP). These logging events are logged according to local
   policy, as if they were generated locally.

   <p>For example, the socket node might decide to log events to a
   local file and also resent them to a second socket node.

    @author  Ceki G&uuml;lc&uuml;

    @since 0.8.4
*/
public class SocketNode implements Runnable {

  Socket socket;
  LoggerRepository hierarchy;
  ObjectInputStream ois;

  static Logger logger = Logger.getLogger(SocketNode.class);

  public SocketNode(Socket socket, LoggerRepository hierarchy) {
    this.socket = socket;
    this.hierarchy = hierarchy;
    try {
      ois = new ObjectInputStream(
                         new BufferedInputStream(socket.getInputStream()));
    }
    catch(Exception e) {
      logger.error("Could not open ObjectInputStream to "+socket, e);
    }
  }

  //public
  //void finalize() {
  //System.err.println("-------------------------Finalize called");
  // System.err.flush();
  //}

  public void run() {
    LoggingEvent event;
    Logger remoteLogger;

    try {
      while(true) {
	// read an event from the wire
	event = (LoggingEvent) ois.readObject();
	// get a logger from the hierarchy. The name of the logger is taken to be the name contained in the event.
	remoteLogger = hierarchy.getLogger(event.getLoggerName());
	//event.logger = remoteLogger;
	// apply the logger-level filter
	if(event.getLevel().isGreaterOrEqual(remoteLogger.getEffectiveLevel())) {
	  // finally log the event as if was generated locally
	  remoteLogger.callAppenders(event);
	}
      }
    } catch(java.io.EOFException e) {
      logger.info("Caught java.io.EOFException closing conneciton.");
    } catch(java.net.SocketException e) {
      logger.info("Caught java.net.SocketException closing conneciton.");
    } catch(IOException e) {
      logger.info("Caught java.io.IOException: "+e);
      logger.info("Closing connection.");
    } catch(Exception e) {
      logger.error("Unexpected exception. Closing conneciton.", e);
    }

    try {
      ois.close();
    } catch(Exception e) {
      logger.info("Could not close connection.", e);
    }
  }
}
