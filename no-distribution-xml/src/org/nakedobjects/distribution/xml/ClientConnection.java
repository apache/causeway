package org.nakedobjects.distribution.xml;

import org.nakedobjects.object.NakedObjectRuntimeException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


public class ClientConnection {
    private static final Logger LOG = Logger.getLogger(ClientConnection.class);
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket socket;

    public void init() {
        try {
            LOG.debug("Client end-point ");
            socket = new Socket("localhost", 9567);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            LOG.debug("Connnection established " + socket);
            //  	receiver = new ClientUpdateReceiver(input);
            //  	LOG.debug("Reciever waiting " + receiver);
        } catch (MalformedURLException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (UnknownHostException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IOException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public String request(String requestData) {
        try {
            LOG.debug("Sending request \n" + requestData);
            output.writeObject(requestData);
            output.flush();
            String response = (String) input.readObject();
            LOG.debug("response recieved: \n" + response);
            return response;
        } catch (StreamCorruptedException e) {
            try {
                int available = input.available();
                LOG.debug("error in reading; skipping bytes: " + available);
	            input.skip(available);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            throw new NakedObjectRuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new NakedObjectRuntimeException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new NakedObjectRuntimeException(e.getMessage(), e);
        }
    }

    public void shutdown() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */