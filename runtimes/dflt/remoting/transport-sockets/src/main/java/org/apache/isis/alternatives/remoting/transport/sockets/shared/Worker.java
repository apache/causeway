/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.alternatives.remoting.transport.sockets.shared;

import java.io.IOException;
import java.net.SocketException;

import org.apache.isis.alternatives.remoting.server.ServerConnection;
import org.apache.isis.alternatives.remoting.transport.ServerConnectionHandler;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.log4j.Logger;

public class Worker implements Runnable {
    private static final Logger LOG = Logger.getLogger(Worker.class);
    private static int nextId = 1;
    
    private final WorkerPool poolToReturnTo;
    private final int id = nextId++;

    private boolean running = true;
    
    private ServerConnection connection;
	private ServerConnectionHandler serverDelegate;

    public Worker(final WorkerPool pool) {
        this.poolToReturnTo = pool;
    }

    public synchronized void gracefulStop() {
        running = false;
    }

    public boolean isAvailable() {
        return connection == null;
    }

    ///////////////////////////////////////////////////////
    // run, setIncomingConnection, wait
    ///////////////////////////////////////////////////////

    public synchronized void run() {
        while (running) {
        	waitForIncomingConnection();
            if (connection == null) {
            	break;
            }

            handleRequest();
        }
        LOG.info("Stopping: " + toString());
    }

    /**
     * @see #waitForIncomingConnection()
     */
    public synchronized void setIncomingConnection(final ServerConnection connection) {
        this.connection = connection;
        this.serverDelegate = new ServerConnectionHandler(connection);
        notify();
    }

    /**
     * @see #setIncomingConnection(ServerConnection)
     */
	private void waitForIncomingConnection() {
		while (connection == null) {
		    try {
		        wait();
		    } catch (final InterruptedException e) {
		        if (!running) {
		            LOG.info("Request to stop : " + toString());
		            break;
		        }
		    }
		}
	}


    ///////////////////////////////////////////////////////
    // handleRequest
    ///////////////////////////////////////////////////////

	private void handleRequest() {
		try {
			serverDelegate.handleRequest();
		} catch (final SocketException e) {
		    LOG.info("shutting down receiver (" + e + ")");
		} catch (final IOException e) {
		    LOG.info("connection exception; closing connection", e);
		} finally {
		    end();
		}
	}

    private void end() {
    	serverDelegate = null;
        connection = null;
        poolToReturnTo.returnWorker(this);
    }

    
    
    ///////////////////////////////////////////////////////
    // Debug
    ///////////////////////////////////////////////////////
    
    public void debug(final DebugString debug) {
    	serverDelegate.debug(debug);
    }

    
    ///////////////////////////////////////////////////////
    // toString
    ///////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "Worker#" + id;
    }
}

