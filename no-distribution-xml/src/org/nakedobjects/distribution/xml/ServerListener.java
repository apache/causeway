package org.nakedobjects.distribution.xml;

import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedObjectsComponent;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class ServerListener implements Runnable, NakedObjectsComponent {
	private static final Logger LOG = Logger.getLogger(ServerListener.class);
    private static final int PORT = 9567;
    private static final String SERVER_PORT = "server.port";
	private boolean acceptConnection;
	private Vector clients = new Vector();
	private ServerSocket socket;
	
	protected abstract ServerConnection connect(Socket socket) throws IOException;
    
	public void run() {
		try {
			socket.setSoTimeout(1000);
            LOG.debug("listening for connection on "  + socket);
	    	while(acceptConnection) {
	    		try {
					ServerConnection connection = connect(socket.accept());
					LOG.debug("connection requested from "  + connection.getClient());
					clients.addElement(connection);
					connection.start();
				} catch (InterruptedIOException ignore) {
					continue;
				} catch (IOException e) {
					LOG.warn("connection exception", e);
				}
	    	}
		} catch (SocketException e) {
			LOG.error("listener failure", e);
		}
	}
    
	public synchronized void shutdown() {
		acceptConnection = false;
		Enumeration e = clients.elements();
		while (e.hasMoreElements()) {
			 ServerConnection client = (ServerConnection) e.nextElement();
			 client.shutdown();
		}		
	}

	public void start() {
    	LOG.info("creating listener on localhost");
    	try{
            int port = NakedObjects.getConfiguration().getInteger(SERVER_PORT, PORT);
    		socket = new ServerSocket(port);
    	} catch (IOException e) {
    	    throw new NakedObjectRuntimeException(e);
    	}
		acceptConnection = true;

		new Thread(this, "Listener").start();
	}
	
    public String toString() {
		return "Listener " + clients.size() + " connections  ";
	}
    
    public void init() {
        start();
    }
}



/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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

