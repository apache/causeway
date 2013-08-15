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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SmtpSnapshotAppender extends SnapshotAppender {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SmtpSnapshotAppender.class);
    private String server;
    private String recipient;
    private int port = 25;
    private String senderDomain = "localhost";

    public SmtpSnapshotAppender(final org.apache.log4j.spi.TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public SmtpSnapshotAppender() {
        super();
    }

    public void setServer(final String mailServer) {
        if (mailServer == null) {
            throw new NullPointerException("mail server cannot be null");
        }
        this.server = mailServer;
    }

    public void setRecipient(final String recipient) {
        if (recipient == null) {
            throw new NullPointerException("recipient cannot be null");
        }
        this.recipient = recipient;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setSenderDomain(final String senderDomain) {
        if (senderDomain == null) {
            throw new NullPointerException("sender domain cannot be null");
        }
        this.senderDomain = senderDomain;
    }

    @Override
    protected void writeSnapshot(final String message, final String details) {
        try {
            if (server == null) {
                throw new NullPointerException("mail server must be specified");
            }
            if (recipient == null) {
                throw new NullPointerException("recipient must be specified");
            }

            final Socket s = new Socket(server, port);
            final BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "8859_1"));
            final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "8859_1"));

            send(in, out, "HELO " + senderDomain);
            // warning : some mail server validate the sender address
            // in the MAIL FROm command, put your real address here

            send(in, out, "MAIL FROM: <no-reply@" + senderDomain + ">");
            send(in, out, "RCPT TO: " + recipient);
            send(in, out, "DATA");
            send(out, "Subject: " + message);
            send(out, "From: Autosend");
            send(out, "Content-Type: " + layout.getContentType());

            send(out, "\r\n");

            // message body
            send(out, details);
            send(in, out, "\r\n.\r\n");
            send(in, out, "QUIT");
            s.close();
        } catch (final Exception e) {
            LOG.info("failed to send email with log", e);
        }
    }

    private void send(final BufferedReader in, final BufferedWriter out, final String s) throws IOException {
        out.write(s + "\r\n");
        out.flush();
        System.out.println(">  " + s);
        final String r = in.readLine();
        System.out.println("<  " + r);
    }

    private void send(final BufferedWriter out, final String s) throws IOException {
        out.write(s + "\r\n");
        out.flush();
        System.out.println(">> " + s);
    }
}
