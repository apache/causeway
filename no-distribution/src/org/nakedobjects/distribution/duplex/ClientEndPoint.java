
package org.nakedobjects.distribution.duplex;

import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.Request;
import org.nakedobjects.distribution.RequestFowarder;
import org.nakedobjects.object.NakedObjectRuntimeException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


public class ClientEndPoint implements RequestFowarder {
    private static final Logger LOG = Logger.getLogger(ClientEndPoint.class);
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private ClientUpdateReceiver receiver;
	private Socket socket;

    public void init() {
        try {
            DuplexParameters params = new DuplexParameters();
			LOG.debug("Duplex params " + params.server());
            socket = new Socket(params.host(), params.port());
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        	LOG.debug("Connnection established " + socket);
        	receiver = new ClientUpdateReceiver(input);
        	LOG.debug("Reciever waiting " + receiver);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    
    public ObjectUpdateMessage receive() {
    	if(receiver == null) { 
    		return null; 
    	} else {
    		return receiver.getUpdate();
    	}
    }

    public Serializable executeRemotely(Request request) {
        try {
        	output.writeObject(request);
        	output.flush();
            Serializable response = receiver.awaitResponse();
            LOG.debug("response for send: " + response);
			return response;
        } catch (IOException e) {
			throw new NakedObjectRuntimeException(e.getMessage(), e);
		}
    }

    public void shutdown() {
    	try {
    		receiver.shutdown();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public String title() {
        return "Duplex Connection";
    }

 
}


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