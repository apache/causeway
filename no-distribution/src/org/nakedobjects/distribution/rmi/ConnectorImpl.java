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

package org.nakedobjects.distribution.rmi;

import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.RequestContext;


public class ConnectorImpl extends UnicastRemoteObject implements Connector{
	private static final Logger LOG = Logger.getLogger(ConnectorImpl.class);
	private Vector clients = new Vector();
	private RequestContext server;
	
	public ConnectorImpl(RequestContext server) throws RemoteException {
		this.server = server;
	}

	public void addUpdateNotifier(UpdateCallback callback) throws RemoteException {
		clients.addElement(callback);
	}
	
	void update(ObjectUpdateMessage update) {
		Vector c;
		synchronized(clients) {
			c = (Vector) clients.clone();
		}
		Enumeration e = c.elements();
		while (e.hasMoreElements()) {
			UpdateCallback client = (UpdateCallback) e.nextElement();
			try {
				client.update(update);
			} catch (RemoteException e1) {
				LOG.error("Problem notifying client " + client, e1);
				// TODO should this reference then be removed from list?
			}
		}
	}

	public Connection getConnection() throws RemoteException {
		try {
			LOG.debug("Connection request from "  + getClientHost());
			Connection connection = new ConnectionImpl(server);
			LOG.info("Connection for "  + getClientHost() + " " + connection);
			return connection;
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
}
