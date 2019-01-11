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

package org.apache.isis.core.runtime.logging;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class SnapshotServer {
    private static final String SNAPSHOT_PROPERTIES = "snapshot.properties";
    private static final org.apache.log4j.Logger LOG4J = org.apache.log4j.Logger.getLogger(SnapshotServer.class);

    public static void main(final String[] args) {
        org.apache.log4j.BasicConfigurator.configure();

        int port;
        String directoryPath;
        String fileName;
        String extension;

        final Properties prop = new Properties();
        FileInputStream propIn = null;
        try {
            propIn = new FileInputStream(SNAPSHOT_PROPERTIES);
            prop.load(propIn);
        } catch (final FileNotFoundException e) {
            LOG4J.error("failed to load properties file, " + SNAPSHOT_PROPERTIES);
            return;
        } catch (final IOException e) {
            LOG4J.error("failed to read properties file, " + SNAPSHOT_PROPERTIES, e);
            return;
        } finally {
            if (propIn != null) {
                try {
                    propIn.close();
                } catch (final IOException e) {
                    LOG4J.error("failed to close properties file, " + SNAPSHOT_PROPERTIES, e);
                    return;
                }
            }
        }

        final String prefix = "isis.snapshot.";
        final String portString = prop.getProperty(prefix + "port", "9289");
        port = Integer.valueOf(portString).intValue();
        directoryPath = prop.getProperty(prefix + "directory", ".");
        fileName = prop.getProperty(prefix + "filename", "log-snapshot-");
        extension = prop.getProperty(prefix + "extension", "txt");

        try (ServerSocket server = new ServerSocket(port)) {
            writeSnapshot(directoryPath, fileName, extension, server);
        } catch (final IOException e) {
            LOG4J.error("failed to start server", e);
        }
    }

    private static void writeSnapshot(
            final String directoryPath,
            final String fileName,
            final String extension,
            final ServerSocket server) {
        while (true) {
            try {
                final Socket s = server.accept();

                LOG4J.info("receiving log from " + s.getInetAddress().getHostName());

                final BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "8859_1"));

                final String message = in.readLine();
                final SnapshotWriter w = new SnapshotWriter(directoryPath, fileName, extension, message);
                String line;
                while ((line = in.readLine()) != null) {
                    w.appendLog(line);
                }
                s.close();

                in.close();
                return;
            } catch (final IOException e) {
                LOG4J.error("failed to log", e);
            }
        }
    }
}
