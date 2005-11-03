package org.nakedobjects.utility.logging;

import org.nakedobjects.utility.Assert;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.Logger;


public class SocketSnapshotAppender extends SnapshotAppender {
    private static final Logger LOG = Logger.getLogger(SmtpSnapshotAppender.class);
    private int port = 9289;
    private String server;

    public void setPort(int port) {
        this.port = port;
    }

    public void setServer(String mailServer) {
        Assert.assertNotNull(mailServer);

        this.server = mailServer;
    }

    protected void writeSnapshot(String message, String details) {
        try {
            Assert.assertNotNull("server", server);

            Socket s = new Socket(server, port);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "8859_1"));

            out.write(message + "\n");
            out.write(details + "\n");

            out.flush();

            s.close();
        } catch (ConnectException e) {
            LOG.info("failed to connect to server " + server);
        } catch (Exception e) {
            LOG.info("failed to send email with log", e);
        }
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