
package org.nakedobjects.distribution.broadcast;


import org.nakedobjects.distribution.Request;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.ObjectStoreException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Category;


/**
 */
class RequestConnection implements Runnable {
    final static Category LOG = Category.getInstance(RequestConnection.class);
    private Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
	private OutputStream os;
	private InputStream is;
	private RequestContext server;

    /**
     Creates a ClientConnection object and maintains the connection.

     @param	clientSocket		the socket that has been set up for this connection
     */
    public RequestConnection(Socket clientSocket, RequestContext server) {
        this.clientSocket = clientSocket;
  //      requestResponse = new Response(server);
        this.server = server;
    }

    private void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            	LOG.error("Error while closing socket input stream", ignore);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException ignore) {
            	LOG.error("Error while closing socket output stream", ignore);
            }
        }
    }

    protected String getClient() {
        return clientSocket.getInetAddress().getHostName();
    }

    protected Request receiveRequest() throws ServerRequestException {
        try {
            // read in message from client
            in = new ObjectInputStream(is);
            Request request = (Request) in.readObject();
            LOG.debug("received request " +request);
            return request;
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        	
        	throw new ServerRequestException(e);
        } catch (IOException e) {
        	e.printStackTrace();
        	
        	throw new ServerRequestException(e);
        }
        
    }

    protected void returnResponse(Object response) {
        try {
        	CounterOutputStream cos = new CounterOutputStream(os);

            out = new ObjectOutputStream(cos);
            out.writeObject(response);
            LOG.debug(cos.getSize() + " bytes sent in response "+ response);
        } catch (IOException e) {
            LOG.error("Failed to return response to caller", e);
        } finally {
            close();
        }
    }

    public void run() {
        try {
        	os = clientSocket.getOutputStream();
        	is = clientSocket.getInputStream();
        	
	       	String client = getClient();
	       	Request request = receiveRequest();
//        	Object response = requestResponse.generateResponse(request, client);
        	Object response = server.execute(request, client);
        	LOG.debug("response " + response + " to request " + request);
        	returnResponse(response);
        } catch (ObjectStoreException e) {
        	e.printStackTrace();
        	
            returnResponse(e);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            close();
        }
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
