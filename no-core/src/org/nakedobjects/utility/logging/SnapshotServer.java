package org.nakedobjects.utility.logging;

import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


public class SnapshotServer {
    private static final Logger LOG = Logger.getLogger(SnapshotServer.class);
    
    public static void main(String[] args) {
        BasicConfigurator.configure();
        
        int port;
        String directoryPath;
        String fileName;
        String extension;
        
        PropertiesFileLoader loader = new PropertiesFileLoader("nakedobjects.properties", true);
        PropertiesConfiguration c = new PropertiesConfiguration(loader);

        String prefix = "snapshotserver.";
        port = c.getInteger(prefix + "port", 9289);
        directoryPath = c.getString(prefix + "directory", ".");
        fileName = c.getString(prefix + "filename", "log");
        extension = c.getString(prefix + "extension", "txt");
        
        ServerSocket server;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("failed to start server", e);
            return;
        }
        
        while (true) {
            try {
                Socket s = server.accept();
                
                LOG.info("receiving log from " + s.getInetAddress().getHostName());
                
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "8859_1"));
                
                String message = in.readLine();
                SnapshotWriter w = new SnapshotWriter(directoryPath, fileName, extension, message);
                String line;
                while((line = in.readLine()) != null) {
                    w.appendLog(line);
                }
                s.close();
                
                in.close();
            } catch (IOException e) {
                LOG.error("failed to log", e);
            }
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */