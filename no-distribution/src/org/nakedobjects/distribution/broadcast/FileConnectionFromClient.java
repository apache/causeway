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

package org.nakedobjects.distribution.broadcast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Category;

import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.utility.Configuration;


/**
 */
public class FileConnectionFromClient implements Runnable {
    final static Category LOG = Category.getInstance(
                                        FileConnectionFromClient.class);
    private static final String DIRECTORY = "file.serving.directory";
    private Socket clientSocket;
    private RequestContext server;

    public FileConnectionFromClient(Socket clientSocket, RequestContext server) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;

        try {
            // find out what is requested
            in = clientSocket.getInputStream();

            byte[] b = new byte[100];
            int len = in.read(b);

            if (len >= 0) {
                String path = new String(b, 0, len);

                LOG.info("File requested " + path);

                // get file
                File f = new File(Configuration.getInstance()
                                                         .getString(DIRECTORY, 
                                                                    "./"), path);

                LOG.debug("Looking for " + f.getAbsolutePath() + f.exists());

                byte[] data = null;

                if (f.exists() && f.isFile()) {
                    data = new byte[(int) f.length()];

                    FileInputStream fis = new FileInputStream(f);

                    fis.read(data);
                    LOG.info("Returning " + data.length + " bytes");


                    // return it
                    out = clientSocket.getOutputStream();
                    out.write(data);
                } else {
                    LOG.info("Returning null - file not found");
                }
            } else {
               	LOG.info("Client disconnected");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                    LOG.error("Error while closing socket output stream", 
                              ignore);
                }
            }
        }
    }
}
