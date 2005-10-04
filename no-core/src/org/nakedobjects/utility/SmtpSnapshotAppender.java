package org.nakedobjects.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.log4j.Logger;


public class SmtpSnapshotAppender extends SnapshotAppender {
    private static final Logger LOG = Logger.getLogger(SmtpSnapshotAppender.class);
    private String server;
    private String recipient;
    private int port = 25;
    private String senderDomain = "domain";

    public void setServer(String mailServer) {
        Assert.assertNotNull(mailServer);

        this.server = mailServer;
    }

    public void setRecipient(String recipient) {
        Assert.assertNotNull(recipient);

        this.recipient = recipient;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setSenderDomain(String senderDomain) {
        Assert.assertNotNull(senderDomain);

        this.senderDomain = senderDomain;
    }

    protected void writeSnapshot(String message, String details) {
        try {
            Assert.assertNotNull("server", server);
            Assert.assertNotNull("recipient", recipient);
            
            Socket s = new Socket(server, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "8859_1"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "8859_1"));

            send(in, out, "HELO " + senderDomain );
            // warning : some mail server validate the sender address
            // in the MAIL FROm command, put your real address here

            send(in, out, "MAIL FROM: <no-reply@nakedobjects.org>");
            send(in, out, "RCPT TO: " + recipient);
            send(in, out, "DATA");
            send(out, "Subject: " + message);
            send(out, "From: Autosend");
            send(out, "\r\n");

            // message body
            send(out, details);
            send(out, "\r\n.\r\n");
            send(in, out, "QUIT");
            s.close();
        } catch (Exception e) {
            LOG.info("failed to send email with log", e);
        }
    }

    private void send(BufferedReader in, BufferedWriter out, String s) throws IOException {
        out.write(s + "\r\n");
        out.flush();
//        System.out.println(s);
        s = in.readLine();
//        System.out.println(s);
    }

    private void send(BufferedWriter out, String s) throws IOException {
        out.write(s + "\r\n");
        out.flush();
//        System.out.println(s);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */