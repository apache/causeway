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
package org.nakedobjects.distribution.duplex;

import org.nakedobjects.distribution.ObjectUpdateMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.SocketException;

import org.apache.log4j.Logger;


final class ClientUpdateReceiver implements Runnable {
    private static final Logger LOG = Logger.getLogger(ClientUpdateReceiver.class);
    private boolean receiving;
    private ObjectInputStream input;

    private ReceivedUpdates updates = new ReceivedUpdates();
    private Response response = new Response();
    
    public ClientUpdateReceiver(ObjectInputStream input) {
        this.input = input;
        receiving = true;
        new Thread(this, "Duplex-Receiver").start();
    }

   Serializable awaitResponse() {
        return response.awaitResponse();
    }

	public ObjectUpdateMessage getUpdate() {
		return updates.getNextUpdate();
	}
	
	void shutdown() {
		receiving = false;
		updates.shutdown();
	}


    public void run() {
        while (receiving) {
            Object object;

            try {
                object = input.readObject();
                LOG.debug("Recieved " + object);
                if (object instanceof ObjectUpdateMessage) {
                    ObjectUpdateMessage update = (ObjectUpdateMessage) object;
					LOG.debug("Received update " + update);
					updates.addUpdate(update);
                } else {
                    response.setResponse(object);
                }
            } catch (SocketException e) {
                LOG.info("Socket failure (" + e + ")");
                break;
            } catch (IOException e) {
                LOG.error("Receive problem; shutting down reciever", e);
                break;
            } catch (ClassNotFoundException e) {
                LOG.warn("Unexpected object received", e);
            }
        }
        LOG.info("Thread ending");
    }
}
