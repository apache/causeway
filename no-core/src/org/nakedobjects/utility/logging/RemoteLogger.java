package org.nakedobjects.utility.logging;


import org.nakedobjects.utility.AboutNakedObjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


public class RemoteLogger {
    private static final Logger LOG = Logger.getLogger(RemoteLogger.class);
    private static final String URL_SPEC = "http://development.nakedobjects.net/errors/log.php";

    /**
     * Submits an error log to the development server.
     */
    public static void submitLog(String message, String detail, String proxyAddress, int proxyPort) {
        String user = System.getProperty("user.name");
        String system = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") "
                + System.getProperty("os.version");
        String java = System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version");
        String version = AboutNakedObjects.getFrameworkVersion() + " " + AboutNakedObjects.getFrameworkBuild();
        
        try {
            URL url = proxyAddress == null ? new URL(URL_SPEC) : new URL("http", proxyAddress, proxyPort, URL_SPEC);
            LOG.info("connect to " + url);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            HttpQueryWriter out = new HttpQueryWriter(connection.getOutputStream());
            out.addParameter("user", user);
            out.addParameter("system", system);
            out.addParameter("vm", java);
            out.addParameter("error", message);
            out.addParameter("trace", detail);
            out.addParameter("version", version);
            out.close();

            InputStream in = connection.getInputStream();
            int c;
            StringBuffer result = new StringBuffer();
            while ((c = in.read()) != -1) {
                result.append((char) c);
            }
            LOG.info(result);

            in.close();

        } catch (UnknownHostException e) {
            LOG.info("could not find host (unknown host) to submit log to");
        } catch (IOException e) {
            LOG.debug("i/o problem submitting log", e);
        }

    }

    private static class HttpQueryWriter extends OutputStreamWriter {

        private int parameter = 1;

        public HttpQueryWriter(OutputStream outputStream) throws UnsupportedEncodingException {
            super(outputStream, "ASCII");
        }

        public void addParameter(String name, String value) throws IOException {
            if (name == null || value == null) {
                return;
            }

            if (parameter > 1) {
                write("&");
            }
            parameter++;
            write(URLEncoder.encode(name));
            write("=");
            write(URLEncoder.encode(value));
        }

        public void close() throws IOException {
            write("\r\n");
            flush();
            super.close();
        }
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */