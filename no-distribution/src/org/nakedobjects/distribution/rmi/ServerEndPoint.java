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
package org.nakedobjects.distribution.rmi;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.UpdateNotifier;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.Logger;


public class ServerEndPoint implements ConnectionFromClient {
	private static final Logger LOG = Logger.getLogger(ServerEndPoint.class);
    private ConnectorImpl connector;
	private String rmiName;
	private RmiParameters rmi;

    public void broadcast(ObjectUpdateMessage msg) {
    	LOG.debug("Broadcasting update " + msg);
        connector.update(msg);
    }

    public UpdateNotifier getNotifier() {
        return null;
    }

    public void init(RequestContext server) throws ComponentException {
        try {
            connector = new ConnectorImpl(server);

            rmi = new RmiParameters();

            System.setProperty("java.rmi.server.hostname", rmi.host());
            
        	LOG.info("Creating RMI registery on port " + rmi.port());
        	LocateRegistry.createRegistry(rmi.port());
        	
        	LOG.info("Registering " + connector + " as " + rmi.name());
            Naming.rebind(rmi.url(), connector);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
        	LOG.info("Deregistering " + connector);
           Naming.unbind(rmiName);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
    
    public String toString() {
		return "RMI connection " + rmi.url();
	}
}
