package org.nakedobjects.distribution.broadcast;

import org.nakedobjects.distribution.ObjectUpdateMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.log4j.Logger;


class UpdateBroadcaster {
    private final static Logger LOG = Logger.getLogger(UpdateBroadcaster.class);
    private MulticastSocket mcs;
    private InetAddress multicastAddress;
    private int port;
    private int packageSize;

	public UpdateBroadcaster(String address, int port, byte ttl, int packageSize) throws IOException {
		this.port = port;
		this.packageSize = packageSize;
        multicastAddress = InetAddress.getByName(address);
        mcs = new MulticastSocket(port);
        // Use of deprecated method to accomodate .NET port
        mcs.setTTL(ttl);
	}

    void broadcast(ObjectUpdateMessage updateMessage) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(updateMessage);

            byte[] msg = baos.toByteArray();

            oos.close();

            DatagramPacket packet = new DatagramPacket(msg, msg.length,
                    multicastAddress, port);

            if (msg.length > packageSize) {
                LOG.warn("UDP package is greater than expected package size");
            }

            LOG.debug("Sending update message: " + updateMessage + " " +
            		packet.getLength() + " bytes");
            mcs.send(packet);
        } catch (IOException e) {
            LOG.error(this, e);
        }
    }


    public String toString() {
        return "UPD Update Notifier dispatching on " +
        multicastAddress.getHostName() + "/" + port;
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