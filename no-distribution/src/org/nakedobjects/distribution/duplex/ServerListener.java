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

package org.nakedobjects.distribution.duplex;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.RequestContext;


class ServerListener implements Runnable {
	private static final Logger LOG = Logger.getLogger(ServerListener.class);
	private Vector clients = new Vector();
	private RequestContext server;
	private ServerSocket socket;
	private boolean acceptConnection;
	private DuplexParameters rmi;
	
	public ServerListener(RequestContext server) {
		this.server = server;
		rmi = new DuplexParameters();
    	LOG.info("Creating listener on " + rmi.host() + ":" + rmi.port());
    	try{
    		socket = new ServerSocket(rmi.port());
    	} catch (IOException e) {
    	}
		acceptConnection = true;
	}

	public void run() {
		try {
			socket.setSoTimeout(1000);
	    	while(acceptConnection) {
	    		try {
					ServerAcceptedConnection connection = new ServerAcceptedConnection(socket.accept(), server);
					LOG.debug("Connection requested from "  + connection.getClient());
					clients.addElement(connection);
					connection.start();
				} catch (InterruptedIOException ignore) {
					continue;
				} catch (IOException e) {
					LOG.warn("Connection exception", e);
				}
	    	}
		} catch (SocketException e) {
			LOG.error("Listener failure", e);
		}
	}
	
	void update(ObjectUpdateMessage update) {
		Vector c;
		synchronized(clients) {
			c = (Vector) clients.clone();
		}
		Enumeration e = c.elements();
		while (e.hasMoreElements()) {
			ServerAcceptedConnection client = (ServerAcceptedConnection) e.nextElement();
			try {
				client.postUpdate(update);
			} catch (IOException ex) {
				LOG.warn("Connection problem to " + client.getClient() + "; closing connection (" + ex.getMessage() + ")");
				client.shutdown();
			}
		}
	}

	public void start() {
		new Thread(this, "Listener").start();
	}

	public synchronized void shutdown() {
		acceptConnection = false;
//		wait();
		Enumeration e = clients.elements();
		while (e.hasMoreElements()) {
			 ServerAcceptedConnection client = (ServerAcceptedConnection) e.nextElement();
			 client.shutdown();
		}		
	}
	
    public String toString() {
		return "Listener " + clients.size() + " connections  on " + rmi.host() + ":" + rmi.port();
	}

}
