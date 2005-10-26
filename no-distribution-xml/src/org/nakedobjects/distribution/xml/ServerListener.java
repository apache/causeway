package org.nakedobjects.distribution.xml;

import org.nakedobjects.distribution.ServerDistribution;
import org.nakedobjects.object.NakedObjectRuntimeException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public class ServerListener implements Runnable {
	private static final Logger LOG = Logger.getLogger(ServerListener.class);
	private Vector clients = new Vector();
	private ServerDistribution server;
	private ServerSocket socket;
	private boolean acceptConnection;
	
	public void setServerDistribution(ServerDistribution server) {
		this.server = server;
	}
	
	public void run() {
		try {
			socket.setSoTimeout(1000);
	    	while(acceptConnection) {
	    		try {
					ServerConnection connection = new ServerConnection(socket.accept(), server);
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

	public void start() {
    	LOG.info("creating listener on localhost");
    	try{
    		socket = new ServerSocket(9567);
    	} catch (IOException e) {
    	    throw new NakedObjectRuntimeException(e);
    	}
		acceptConnection = true;

		new Thread(this, "Listener").start();
	}

	public synchronized void shutdown() {
		acceptConnection = false;
//		wait();
		Enumeration e = clients.elements();
		while (e.hasMoreElements()) {
			 ServerConnection client = (ServerConnection) e.nextElement();
			 client.shutdown();
		}		
	}
	
    public String toString() {
		return "Listener " + clients.size() + " connections  ";
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

