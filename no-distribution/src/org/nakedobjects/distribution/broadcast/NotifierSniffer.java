
package org.nakedobjects.distribution.broadcast;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.distribution.ObjectUpdateMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Date;


public class NotifierSniffer {
    public NotifierSniffer() {
        try {
            Parameters params = new Parameters();
            String addr = params.getUpdateAddress();
            InetAddress inetAddress = InetAddress.getByName(addr);
            int port = params.getUpdatePort();
            int pacakageSize = params.getUpdatePackageSize();

            System.out.println("Listening on " + inetAddress + "/" + port);

            MulticastSocket mcs = new MulticastSocket(port);
            byte[] buffer = new byte[pacakageSize];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            mcs.joinGroup(inetAddress);

            int i = 1;

            while (true) {
                System.out.print(i++ + "... ");
                packet.setLength(pacakageSize);
                mcs.receive(packet);
                System.out.print(new Date() + "/" +
                    packet.getAddress().getHostName() + "/" +
                    packet.getLength() + " bytes - ");

                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                   ObjectUpdateMessage message = ((ObjectUpdateMessage) ois.readObject());
                    System.out.print(message);
                } catch (Exception e) {
                    System.out.print(" failed to extract message " + e);
                }

                System.out.println();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the application.
     * @param args an array of command-line arguments
     */
    public static void main(java.lang.String[] args) throws ConfigurationException {
        org.apache.log4j.PropertyConfigurator.configure(
            "log4j.testing.properties");

        String config;

        if (args.length == 1) {
            config = args[0];
        } else {
            config = "." + File.separator + "nakedobjects.properties";
        }

        if (new java.io.File(config).exists()) {
            Configuration.getInstance().load(config);
        } else {
            System.out.println("No configuration file found or loaded.");
        }

        new NotifierSniffer();
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