/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.distribution.broadcast;

import org.nakedobjects.distribution.RequestContext;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Category;


/**
 The Listener waits for a client to connect and then spawns a new thread to deal with it 
 independently.  The main thread that starts off a client threads for each client that connects
 */
public abstract class Listener implements Runnable {
    final static Category LOG = Category.getInstance(Listener.class);
    private ThreadGroup clientThreadGroup;
    private ServerSocket listenSocket;
    private boolean acceptConnections = true;
    private RequestContext server;

    /**
     Creates a listener for client connections.
     @param        port        the port the main server is to listen on.
     */
    public Listener(String host, int port, RequestContext server) throws IOException {
        this.server = server;

        if (host == null) {
            listenSocket = new ServerSocket(port);
        } else {
             listenSocket = new ServerSocket(port, 50, 
                                            InetAddress.getByName(host));
        }
        listenSocket.setSoTimeout(1000);
        clientThreadGroup = new ThreadGroup("clients");
    }

    protected void acceptConnection() {
        try {
            Socket clientSocket = listenSocket.accept();
            Runnable connection = createConnection(clientSocket, server);

            new Thread(clientThreadGroup, connection).start();
        } catch (InterruptedIOException ignore) {
        } catch (SocketException ignore) {
        } catch (IOException e) {
            LOG.error("Server failed to accept connection!", e);
        }
    }

    protected abstract Runnable createConnection(Socket clientSocket, 
                                                 RequestContext server);

    public abstract String getName();

    /**
     Wait for new connections.  When a new connection is made then create a ConnectionFromClient object
     to deal with it independently.
            
     Note - not normally called directly.
     */
    public void run() {
        LOG.debug("Status: Awaiting client connections");
        LOG.info("Listening on " + 
                 listenSocket.getInetAddress().getHostName() + "/" + 
                 listenSocket.getInetAddress().getHostAddress() + 
                 " on port " + listenSocket.getLocalPort());

        while (acceptConnections) {
            acceptConnection();
        }

        LOG.debug("Listener shutdown");
    }

    /**
     Stop all the threads that this thread has created and then stop itself.
     */
    public void shutdown() {
        acceptConnections = false; // stop the server thread
    }

    public void start() {
        new Thread(this, getName()).start();
    }
}
