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


package org.apache.isis.runtimes.dflt.objecstores.nosql.file.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.isis.runtimes.dflt.objecstores.nosql.NoSqlStoreException;


public class FileServer {

    private static final Logger LOG = Logger.getLogger(FileServer.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9012;
    private static final int BACKLOG = 0;

    public static void main(String[] args) throws IOException {
        new FileServer().start();
    }

    private FileServerProcessor server;
    private String host;
    private int port;
    private int connectionTimeout;
    private int readTimeout;

    public FileServer() {
        PropertyConfigurator.configure("config/logging.properties");

        try {
            CompositeConfiguration config = new CompositeConfiguration();
            config.addConfiguration(new SystemConfiguration());
            config.addConfiguration(new PropertiesConfiguration("config/server.properties"));

            host = config.getString("fileserver.host", DEFAULT_HOST);
            port = config.getInt("fileserver.port", DEFAULT_PORT);
            String data = config.getString("fileserver.data");
            String services = config.getString("fileserver.services");
            String logs = config.getString("fileserver.logs");
            connectionTimeout = config.getInt("fileserver.connection.timeout", 5000);
            readTimeout = config.getInt("fileserver.read.timeout", 5000);

            Util.setDirectory(data, services, logs);
            server = new FileServerProcessor();
        } catch (ConfigurationException e) {
            LOG.error("configuration failure", e);
            System.out.println(e.getMessage());
            System.exit(0);
        }

    }

    private void start() {
        ServerSocket socket = null;
        try {
            LOG.debug("setting up socket on " + host + ":" + port);
            InetAddress address = InetAddress.getByName(host);
            socket = new ServerSocket(port, BACKLOG, address);
            socket.setSoTimeout(connectionTimeout);
            LOG.info("listenting on " + socket.getInetAddress().getHostAddress() + " port " + socket.getLocalPort());
            LOG.debug("listenting on " + socket);
            server.startup();
        } catch (UnknownHostException e) {
            LOG.error("Unknown host " + host, e);
            System.exit(0);
        } catch (IOException e) {
            LOG.error("start failure - networking  not set up for " + host, e);
            System.exit(0);
        } catch (RuntimeException e) {
            LOG.error("start failure", e);
            System.exit(0);
        }
        boolean awaitConnections = true;
        do {
            try {
                Socket connection = socket.accept();
                LOG.info("connection from " + connection);
                connection.setSoTimeout(readTimeout);
                connection(connection);
            } catch (SocketTimeoutException expected) {
            } catch (IOException e) {
                LOG.error("networking problem", e);
            }
        } while (awaitConnections);
    }

    private void connection(Socket connection) {
        try {
            InputStream input = connection.getInputStream();
            OutputStream output = connection.getOutputStream();
            ServerConnection pipe = new ServerConnection(input, output);
            server.process(pipe);
            pipe.logComplete();
        } catch (NoSqlStoreException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                LOG.error("read timed out after " + (readTimeout / 1000.0) + " seconds", e);
            } else {
                LOG.error("file server failure", e);
            }
        } catch (IOException e) {
            LOG.error("networking failure", e);
        } catch (RuntimeException e) {
            LOG.error("request failure", e);
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                LOG.warn("failure to close connection", e);
            }
        }

    }

    private void shutdown() {
        server.shutdown();
    }
}

