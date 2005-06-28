/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */
package org.apache.log4j.chainsaw;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A daemon thread the processes connections from a
 * <code>org.apache.log4j.net.SocketAppender.html</code>.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
class LoggingReceiver extends Thread {
    /** used to log messages **/
    private static final Logger LOG = Logger.getLogger(LoggingReceiver.class);

    /**
     * Helper that actually processes a client connection. It receives events
     * and adds them to the supplied model.
     *
     * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
     */
    private class Slurper implements Runnable {
        /** socket connection to read events from **/
        private final Socket mClient;

        /**
         * Creates a new <code>Slurper</code> instance.
         *
         * @param aClient socket to receive events from
         */
        Slurper(Socket aClient) {
            mClient = aClient;
        }

        /** loops getting the events **/
        public void run() {
            LOG.debug("Starting to get data");
            try {
                final ObjectInputStream ois =
                    new ObjectInputStream(mClient.getInputStream());
                while (true) {
                    final LoggingEvent event = (LoggingEvent) ois.readObject();
                    mModel.addEvent(new EventDetails(event));
                }
            } catch (EOFException e) {
                LOG.info("Reached EOF, closing connection");
            } catch (SocketException e) {
                LOG.info("Caught SocketException, closing connection");
            } catch (IOException e) {
                LOG.warn("Got IOException, closing connection", e);
            } catch (ClassNotFoundException e) {
                LOG.warn("Got ClassNotFoundException, closing connection", e);
            }

            try {
                mClient.close();
            } catch (IOException e) {
                LOG.warn("Error closing connection", e);
            }
        }
    }

    /** where to put the events **/
    private final MyTableModel mModel;

    /** server for listening for connections **/
    private final ServerSocket mSvrSock;

    /**
     * Creates a new <code>LoggingReceiver</code> instance.
     *
     * @param aModel model to place put received into
     * @param aPort port to listen on
     * @throws IOException if an error occurs
     */
    LoggingReceiver(MyTableModel aModel, int aPort) throws IOException {
        setDaemon(true);
        mModel = aModel;
        mSvrSock = new ServerSocket(aPort);
    }

    /** Listens for client connections **/
    public void run() {
        LOG.info("Thread started");
        try {
            while (true) {
                LOG.debug("Waiting for a connection");
                final Socket client = mSvrSock.accept();
                LOG.debug("Got a connection from " +
                          client.getInetAddress().getHostName());
                final Thread t = new Thread(new Slurper(client));
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException e) {
            LOG.error("Error in accepting connections, stopping.", e);
        }
    }
}
