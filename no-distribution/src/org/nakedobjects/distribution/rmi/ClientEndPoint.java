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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.Request;
import org.nakedobjects.distribution.server.ConnectionToServer;


public class ClientEndPoint implements ConnectionToServer {
    private static final Logger LOG = Logger.getLogger(ClientEndPoint.class);
    private Connection connection;
    private Vector updates = new Vector();

    public void init() {
        try {
            LOG.debug("RMI params " + new RmiParameters().url());
        	Connector connector = (Connector) Naming.lookup(new RmiParameters().url());
        	LOG.debug("RMI connector object " + connector);
            connection = connector.getConnection();
            LOG.info("Established connection to server " + connection);
            connector.addUpdateNotifier(new UpdateCallbackImpl(this));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized ObjectUpdateMessage receive() {
        while (updates.size() == 0) {
            try {
                wait();
            } catch (InterruptedException ignore) {
            }
        }

        ObjectUpdateMessage update = (ObjectUpdateMessage) updates.remove(0);

        return update;
    }

    public Serializable executeRemotely(Request request) {
        try {
            return connection.request(request);
        } catch (RemoteException e) {
        	e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void shutdown() {
    }

    public String title() {
        return "RMI Connection";
    }

    public synchronized void update(ObjectUpdateMessage update) {
        LOG.debug("Received update " + update);
        updates.addElement(update);
        notify();
    }
}
