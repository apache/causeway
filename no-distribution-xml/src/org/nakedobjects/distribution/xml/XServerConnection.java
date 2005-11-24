package org.nakedobjects.distribution.xml;

import org.nakedobjects.distribution.ServerDistribution;
import org.nakedobjects.distribution.command.Request;
import org.nakedobjects.distribution.command.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;


class XServerConnection implements ServerConnection, Runnable {
    private static final Logger LOG = Logger.getLogger(XServerConnection.class);
	private static int nextId = 1;
	private int id = nextId++;
	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerDistribution server;
	private Thread t;

	public XServerConnection(Socket socket, ServerDistribution server) {
		this.socket = socket;
		this.server = server;
		try {
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			t.interrupt();
			socket.close();
		} catch (IOException e) {
			LOG.error("failed to close connection", e);
		}
	}

	public String getClient() {
		return socket.getInetAddress().getHostAddress();
	}
	
	public void start() {
		t = new Thread(this, "Connection " + id);
		t.start();
	}

	public void run() {
		while(true) {
			try {
				String requestData = (String) input.readObject();
				LOG.debug("request received \n" + requestData);
				String responseData = respond(requestData);
                LOG.debug("send response \n" + responseData);
				output.writeObject(responseData);
                output.flush();
            } catch (SocketException e) {
                LOG.info("shutting down receiver (" + e + ")");
                break;
			} catch (IOException e) {
				LOG.info("connection exception; closing connection", e);
				break;
			} catch (ClassNotFoundException e) {
				LOG.error("unknown class received; closing connection: " + e.getMessage(), e);
				break;
			}
		}
	}

    private String respond(String requestData) {
        XStream xstream = new XStream();
        Request request = (Request) xstream.fromXML(requestData);
        LOG.debug("request received " + request);
        String responseData;
        try {
            request.execute(server);
        	Response response;
            response = new Response(request);
            response.setUpdates(server.getUpdates());
        	LOG.debug("sending " + response);
        	responseData = xstream.toXML(response);
        } catch(Exception e) {
            LOG.debug("sending exception " + e);
            responseData = xstream.toXML(e);
        }
        return responseData;
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
