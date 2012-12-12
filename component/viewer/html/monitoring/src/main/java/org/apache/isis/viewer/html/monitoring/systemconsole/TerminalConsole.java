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

package org.apache.isis.viewer.html.monitoring.systemconsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * Marked as final because starts a thread in the {@link #TerminalConsole()
 * constructor}.
 */
public final class TerminalConsole implements ServerConsole, Runnable {
    private static final Logger LOG = Logger.getLogger(TerminalConsole.class);
    private Server server;
    private boolean running = true;

    public TerminalConsole() {
        new Thread(this).start();
    }

    public void clear() {
    }

    @Override
    public void close() {
        running = false;
    }

    public void collections() {
    }

    @Override
    public void init(final Server server) {
        this.server = server;
        log("Control of " + server);
    }

    public void listClasses() {
        /*
         * try { Enumeration e = server.getObjectStore().classes();
         * 
         * log("Loaded classes:-"); while (e.hasMoreElements()) {
         * ObjectSpecification object = (ObjectSpecification) e.nextElement();
         * 
         * log(" " + object); } } catch (ObjectStoreException e) {
         * LOG.error("Error listing classes " + e.getMessage()); }
         */
    }

    @Override
    public void log() {
        log("");
    }

    @Override
    public void log(final String message) {
        LOG.info(message);
        System.out.println("> " + message);
    }

    public void objects() {
    }

    public void quit() {
        server.shutdown();
        server = null;
        running = false;
    }

    @Override
    public void run() {
        final BufferedReader dis = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (running) {
                final String readLine = dis.readLine();
                if (readLine == null) {
                    quit();
                    continue;
                }
                final String s = readLine.toLowerCase();

                if (s.equals("")) {
                    continue;
                } else if (s.equals("quit")) {
                    quit();
                } else if (s.equals("classes")) {
                    listClasses();
                } else {
                    System.out.println("Commands: classes, quit");
                }
            }
        } catch (final IOException e) {
            quit();
        }
        exitSystem();
    }

    private void exitSystem() {
        System.exit(0);
    }

    public void start() {
        new Thread(this).start();
    }
}
