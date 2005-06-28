/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.net;

import java.util.Vector;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.AppenderSkeleton;

/**
  Sends {@link LoggingEvent} objects to a set of remote log servers,
  usually a {@link SocketNode SocketNodes}.
    
  <p>Acts just like {@link SocketAppender} except that instead of
  connecting to a given remote log server,
  <code>SocketHubAppender</code> accepts connections from the remote
  log servers as clients.  It can accept more than one connection.
  When a log event is received, the event is sent to the set of
  currently connected remote log servers. Implemented this way it does
  not require any update to the configuration file to send data to
  another remote log server. The remote log server simply connects to
  the host and port the <code>SocketHubAppender</code> is running on.
  
  <p>The <code>SocketHubAppender</code> does not store events such
  that the remote side will events that arrived after the
  establishment of its connection. Once connected, events arrive in
  order as guaranteed by the TCP protocol.

  <p>This implementation borrows heavily from the {@link
  SocketAppender}.

  <p>The SocketHubAppender has the following characteristics:
  
  <ul>
  
  <p><li>If sent to a {@link SocketNode}, logging is non-intrusive as
  far as the log event is concerned. In other words, the event will be
  logged with the same time stamp, {@link org.apache.log4j.NDC},
  location info as if it were logged locally.
  
  <p><li><code>SocketHubAppender</code> does not use a layout. It
  ships a serialized {@link LoggingEvent} object to the remote side.
  
  <p><li><code>SocketHubAppender</code> relies on the TCP
  protocol. Consequently, if the remote side is reachable, then log
  events will eventually arrive at remote client.
  
  <p><li>If no remote clients are attached, the logging requests are
  simply dropped.
  
  <p><li>Logging events are automatically <em>buffered</em> by the
  native TCP implementation. This means that if the link to remote
  client is slow but still faster than the rate of (log) event
  production, the application will not be affected by the slow network
  connection. However, if the network connection is slower then the
  rate of event production, then the local application can only
  progress at the network rate. In particular, if the network link to
  the the remote client is down, the application will be blocked.
  
  <p>On the other hand, if the network link is up, but the remote
  client is down, the client will not be blocked when making log
  requests but the log events will be lost due to client
  unavailability. 

  <p>The single remote client case extends to multiple clients
  connections. The rate of logging will be determined by the slowest
  link.
    
  <p><li>If the JVM hosting the <code>SocketHubAppender</code> exits
  before the <code>SocketHubAppender</code> is closed either
  explicitly or subsequent to garbage collection, then there might
  be untransmitted data in the pipe which might be lost. This is a
  common problem on Windows based systems.
  
  <p>To avoid lost data, it is usually sufficient to {@link #close}
  the <code>SocketHubAppender</code> either explicitly or by calling
  the {@link org.apache.log4j.LogManager#shutdown} method before
  exiting the application.
  
  </ul>
     
  @author Mark Womack */

public class SocketHubAppender extends AppenderSkeleton {

  /**
     The default port number of the ServerSocket will be created on. */
  static final int DEFAULT_PORT = 4560;
  
  private int port = DEFAULT_PORT;
  private Vector oosList = new Vector();
  private ServerMonitor serverMonitor = null;
  private boolean locationInfo = false;
  
  public SocketHubAppender() { }

  /**
     Connects to remote server at <code>address</code> and <code>port</code>. */
  public
  SocketHubAppender(int _port) {
    port = _port;
    startServer();
  }

  /**
     Set up the socket server on the specified port.  */
  public
  void activateOptions() {
    startServer();
  }

  /**
     Close this appender. 
     <p>This will mark the appender as closed and
     call then {@link #cleanUp} method. */
  synchronized
  public
  void close() {
    if(closed)
      return;

	LogLog.debug("closing SocketHubAppender " + getName());
    this.closed = true;
    cleanUp();
	LogLog.debug("SocketHubAppender " + getName() + " closed");
  }

  /**
     Release the underlying ServerMonitor thread, and drop the connections
     to all connected remote servers. */
  public 
  void cleanUp() {
    // stop the monitor thread
	LogLog.debug("stopping ServerSocket");
    serverMonitor.stopMonitor();
    serverMonitor = null;
    
    // close all of the connections
	LogLog.debug("closing client connections");
    while (oosList.size() != 0) {
      ObjectOutputStream oos = (ObjectOutputStream)oosList.elementAt(0);
      if(oos != null) {
        try {
        	oos.close();
        }
        catch(IOException e) {
        	LogLog.error("could not close oos.", e);
        }
        
        oosList.removeElementAt(0);     
      }
    }
  }

  /**
    Append an event to all of current connections. */
  public
  void append(LoggingEvent event) {
	// if no event or no open connections, exit now
    if(event == null || oosList.size() == 0)
      return;

    // set up location info if requested
    if (locationInfo) {
    	event.getLocationInformation();	
    } 

	// loop through the current set of open connections, appending the event to each
    for (int streamCount = 0; streamCount < oosList.size(); streamCount++) {    	

      ObjectOutputStream oos = null;
      try {
        oos = (ObjectOutputStream)oosList.elementAt(streamCount);
      }
      catch (ArrayIndexOutOfBoundsException e) {
        // catch this, but just don't assign a value
        // this should not really occur as this method is
        // the only one that can remove oos's (besides cleanUp).
      }
      
      // list size changed unexpectedly? Just exit the append.
      if (oos == null)
        break;
        
      try {
      	oos.writeObject(event);
      	oos.flush();
    	// Failing to reset the object output stream every now and
    	// then creates a serious memory leak.
    	// right now we always reset. TODO - set up frequency counter per oos?
    	oos.reset();
      }
      catch(IOException e) {
      	// there was an io exception so just drop the connection
      	oosList.removeElementAt(streamCount);
      	LogLog.debug("dropped connection");
      	
      	// decrement to keep the counter in place (for loop always increments)
      	streamCount--;
      }
    }
  }
  
  /**
     The SocketHubAppender does not use a layout. Hence, this method returns
     <code>false</code>. */
  public
  boolean requiresLayout() {
    return false;
  }
  
  /**
     The <b>Port</b> option takes a positive integer representing
     the port where the server is waiting for connections. */
  public
  void setPort(int _port) {
    port = _port;
  }
  
  /**
     Returns value of the <b>Port</b> option. */
  public
  int getPort() {
    return port;
  }
  
  /**
     The <b>LocationInfo</b> option takes a boolean value. If true,
     the information sent to the remote host will include location
     information. By default no location information is sent to the server. */
  public
  void setLocationInfo(boolean _locationInfo) {
    locationInfo = _locationInfo;
  }
  
  /**
     Returns value of the <b>LocationInfo</b> option. */
  public
  boolean getLocationInfo() {
    return locationInfo;
  }
  
  /**
    Start the ServerMonitor thread. */
  private
  void startServer() {
    serverMonitor = new ServerMonitor(port, oosList);
  }
  
  /**
    This class is used internally to monitor a ServerSocket
    and register new connections in a vector passed in the
    constructor. */
  private
  class ServerMonitor implements Runnable {
    private int port;
    private Vector oosList;
    private boolean keepRunning;
    private Thread monitorThread;
    
    /**
      Create a thread and start the monitor. */
    public
    ServerMonitor(int _port, Vector _oosList) {
      port = _port;
      oosList = _oosList;
      keepRunning = true;
      monitorThread = new Thread(this);
      monitorThread.setDaemon(true);
      monitorThread.start();
    }
    
    /**
      Stops the monitor. This method will not return until
      the thread has finished executing. */
    public
    synchronized
    void stopMonitor() {
      if (keepRunning) {
    	LogLog.debug("server monitor thread shutting down");
        keepRunning = false;
        try {
          monitorThread.join();
        }
        catch (InterruptedException e) {
          // do nothing?
        }
        
        // release the thread
        monitorThread = null;
    	LogLog.debug("server monitor thread shut down");
      }
    }
    
    /**
      Method that runs, monitoring the ServerSocket and adding connections as
      they connect to the socket. */
    public
    void run() {
      ServerSocket serverSocket = null;
      try {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000);
      }
      catch (Exception e) {
        LogLog.error("exception setting timeout, shutting down server socket.", e);
        keepRunning = false;
        return;
      }

      try {
    	try {
        	serverSocket.setSoTimeout(1000);
    	}
    	catch (SocketException e) {
          LogLog.error("exception setting timeout, shutting down server socket.", e);
          return;
    	}
      
    	while (keepRunning) {
          Socket socket = null;
          try {
            socket = serverSocket.accept();
          }
          catch (InterruptedIOException e) {
            // timeout occurred, so just loop
          }
          catch (SocketException e) {
            LogLog.error("exception accepting socket, shutting down server socket.", e);
            keepRunning = false;
          }
          catch (IOException e) {
            LogLog.error("exception accepting socket.", e);
          }
	        
          // if there was a socket accepted
          if (socket != null) {
            try {
              InetAddress remoteAddress = socket.getInetAddress();
              LogLog.debug("accepting connection from " + remoteAddress.getHostName() 
			   + " (" + remoteAddress.getHostAddress() + ")");
	        	
              // create an ObjectOutputStream
              ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
	            
              // add it to the oosList.  OK since Vector is synchronized.
              oosList.addElement(oos);
            }
            catch (IOException e) {
              LogLog.error("exception creating output stream on socket.", e);
            }
          }
        }
      }
      finally {
    	// close the socket
    	try {
    		serverSocket.close();
    	}
    	catch (IOException e) {
    		// do nothing with it?
    	}
      }
    }
  }
}

