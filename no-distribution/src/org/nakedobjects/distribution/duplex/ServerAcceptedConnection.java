
package org.nakedobjects.distribution.duplex;

import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.Request;
import org.nakedobjects.distribution.RequestContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;


class ServerAcceptedConnection implements Runnable {
    private static final Logger LOG = Logger.getLogger(ServerAcceptedConnection.class);
	private static int nextId = 1;
	private int id = nextId++;
	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private RequestContext server;
	private Thread t;

	public ServerAcceptedConnection(Socket socket, RequestContext server) {
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
			LOG.error("Failed to close connection", e);
		}
	}

	public String getClient() {
		return socket.getInetAddress().getHostAddress();
	}

	public void postUpdate(ObjectUpdateMessage update) throws IOException {
		send(update);
	}

	private void send(Serializable msg) throws IOException {
		LOG.debug("Sending " + msg);
		output.writeObject(msg);
		output.flush();
	}
	
	public void start() {
		t = new Thread(this, "Connection " + id);
		t.start();
	}

	public void run() {
		while(true) {
			try {
				Request request = (Request) input.readObject();
				LOG.debug("Request recieved " + request);
				Serializable response = server.execute(request, getClient());
				send(response);
            } catch (SocketException e) {
                LOG.info("Shutting down reciever (" + e + ")");
                break;
			} catch (IOException e) {
				LOG.info("Connection exception; closing connection");
				break;
			} catch (ClassNotFoundException e) {
				LOG.error("Unknown class received; closing connection: " + e.getMessage());
				break;
			}
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
