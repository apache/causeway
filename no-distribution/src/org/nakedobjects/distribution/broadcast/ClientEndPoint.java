
package org.nakedobjects.distribution.broadcast;

import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.Request;
import org.nakedobjects.distribution.RequestFowarder;
import org.nakedobjects.object.NakedObjectException;
import org.nakedobjects.object.NakedObjectRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


public class ClientEndPoint implements RequestFowarder {
    final static Logger LOG = Logger.getLogger(ClientEndPoint.class);
    private boolean acceptConnections;
    private MulticastSocket mcs;
    private int packageSize;
    private DatagramPacket packet;
    private Parameters params;

    /*    public static final String DEFAULT_ADDRESS = "localhost";
        private static final String base = "connection.simple";
        public static final String ADDRESS = base + ".address";
        public static final String MESSAGE_PORT = base + ".message-port";
        public static final int DEFAULT_PORT = 2401;
    */
    private int port;
    private String server;

    public void init() {
        params = new Parameters();

        port = params.getRequestPort();
        server = params.getHost();
        LOG.info("Configuring for connection to " + server + ":" + port);

        try {
            String addr = params.getUpdateAddress();
            InetAddress inetAddress = InetAddress.getByName(addr);
            int port = params.getUpdatePort();
            packageSize = params.getUpdatePackageSize();

            LOG.debug("Opening multicast port " + port);
            mcs = new MulticastSocket(port);
            mcs.setSoTimeout(1000);
            LOG.debug("Joining multicast group on " + inetAddress);
            mcs.joinGroup(inetAddress);
            LOG.info("Listening for updates on " + mcs + "/" + port);
            acceptConnections = true;
        } catch (SocketException e) {
            LOG.error(e);
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            LOG.error(e);
            throw new RuntimeException(e.getMessage());
        }

        byte[] buffer = new byte[packageSize];

        packet = new DatagramPacket(buffer, buffer.length);
    }

    public ObjectUpdateMessage receive() {
        ObjectInputStream ois = null;

        while (acceptConnections) {
            try {
                packet.setLength(packageSize);
                mcs.receive(packet);

                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());

                ois = new ObjectInputStream(bais);

                return (ObjectUpdateMessage) ois.readObject();
            } catch (InterruptedIOException ignore) {
                // LOG.debug("IO timeout; reseting");
            } catch (SocketException e) {
                /*
                     * When the shutdown method closes the socket an IOException is thrown.
                     * We ignore it, as we will break out of the loop on the next iteration.
                     */
                continue;
            } catch (IOException e) {
                LOG.error("IO failure receiving update notification ", e);

                continue;
            } catch (ClassNotFoundException e) {
                LOG.error("Invalid class received during update notification ",
                    e);

                continue;
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException ignore) {
                        LOG.error("Error while closing object input stream",
                            ignore);
                    }
                }
            }
        }

        return null;
    }

    public Serializable executeRemotely(Request request) {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            Socket s = new Socket(server, port);

            // send request
            InputStream is = s.getInputStream();
            CounterOutputStream cos = new CounterOutputStream(s.getOutputStream());

            out = new ObjectOutputStream(cos);
            LOG.info("Request for " + server + "/" + port + ": " + request +
                " " +
                ObjectStreamClass.lookup(request.getClass())
                                 .getSerialVersionUID());
            out.writeObject(request);

            // receive response
            in = new ObjectInputStream(is);

            Serializable response = (Serializable) in.readObject();

            if (response instanceof NakedObjectException) {
                LOG.debug("Expected exception on server during request " +
                    request, (NakedObjectException) response);
            } else if (response instanceof Throwable) {
                LOG.error("Java Exception on server during request " + request,
                    (Throwable) response);
                throw new NakedObjectRuntimeException((Throwable) response);
            }

            LOG.info("Response = " + response + " " +
                ((response == null) ? ""
                                    : ("[version=" +
                ObjectStreamClass.lookup(response.getClass())
                                 .getSerialVersionUID() + "]")));

            return response;
        } catch (UnknownHostException e) {
            //            throw new ServerRequestException("Could not find the server at " + 
            //                                             server + "/" + port);
            throw new RuntimeException("Could not find the server at " +
                server + "/" + port);
        } catch (ConnectException e) {
            //            throw new ServerRequestException("Failed to connect to server at " + 
            //                                             server + "/" + port);
            throw new RuntimeException("Failed to connect to server at " +
                server + "/" + port);
        } catch (IOException e) {
            LOG.error("Request to server failed: " + request, e); // not sure how to handle this properly

            throw new RuntimeException("Request to server failed: " + request);
        } catch (ClassNotFoundException e) {
            //            throw new ServerRequestException(
            //                    "Server returned an unknown class: " + e.getMessage());
            throw new RuntimeException("Server returned an unknown class: " +
                e.getMessage());
        } finally {
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
    }

    public void shutdown() {
        acceptConnections = false;
        LOG.info("Shutting down UDP listener");

        if (mcs != null) {
            mcs.close();
        }
    }

    public String title() {
        StringBuffer str = new StringBuffer();
        String host = params.getHost();
        str.append("SimpleConnection ");
        str.append((host == null) ? "localhost" : host);
        str.append(":");
        str.append(params.getRequestPort());
        return str.toString();
	}
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        String host = params.getHost();
        str.append("ClientEndPoint [requests=");
        str.append((host == null) ? "localhost" : host);
        str.append(":");
        str.append(params.getRequestPort());
        str.append("/");
        str.append(params.getFilePort());
        str.append(",updates=");
        str.append(params.getUpdateAddress());
        str.append(":");
        str.append(params.getUpdatePort());
        str.append("]");

        return str.toString();
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