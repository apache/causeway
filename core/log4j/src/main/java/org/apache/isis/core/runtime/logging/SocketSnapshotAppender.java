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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.spi.LoggerFactory;

public class SocketSnapshotAppender extends SnapshotAppender {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SmtpSnapshotAppender.class);
    private int port = 9289;
    private String server;

    public SocketSnapshotAppender(final org.apache.log4j.spi.TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public SocketSnapshotAppender() {
        super();
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setServer(final String mailServer) {
        if (mailServer == null) {
            throw new IllegalArgumentException("mail server not specified");
        }
        this.server = mailServer;
    }

    @Override
    protected void writeSnapshot(final String message, final String details) {
        try {
            if (server == null) {
                throw new IllegalStateException("mail server not specified");
            }

            final Socket s = new Socket(server, port);

            final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "8859_1"));

            out.write(message + "\n");
            out.write(details + "\n");

            out.flush();

            s.close();
        } catch (final ConnectException e) {
            LOG.info("failed to connect to server " + server);
        } catch (final Exception e) {
            LOG.info("failed to send email with log", e);
        }
    }
}
