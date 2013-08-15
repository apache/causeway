/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.runtime.logging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.TriggeringEventEvaluator;

public class WebSnapshotAppender extends SnapshotAppender {
    private static class HttpQueryWriter extends OutputStreamWriter {

        private int parameter = 1;

        public HttpQueryWriter(final OutputStream outputStream) throws UnsupportedEncodingException {
            super(outputStream, "ASCII");
        }

        public void addParameter(final String name, final String value) throws IOException {
            if (name == null || value == null) {
                return;
            }

            if (parameter > 1) {
                write("&");
            }
            parameter++;
            write(URLEncoder.encode(name, "UTF-8"));
            write("=");
            write(URLEncoder.encode(value, "UTF-8"));
        }

        @Override
        public void close() throws IOException {
            write("\r\n");
            flush();
            super.close();
        }
    }

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(WebSnapshotAppender.class);
    private String proxyAddress;
    private int proxyPort = -1;

    private String url_spec = "http://development.isis.net/errors/log.php";

    /**
     * The default constructor will instantiate the appender with a
     * {@link TriggeringEventEvaluator} that will trigger on events with level
     * ERROR or higher.
     */
    public WebSnapshotAppender() {
    }

    public WebSnapshotAppender(final org.apache.log4j.spi.TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyAddress(final String proxyAddess) {
        this.proxyAddress = proxyAddess;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setUrl(final String url) {
        url_spec = url;
    }

    @Override
    protected void writeSnapshot(final String message, final String details) {
        try {
            final URL url = proxyAddress == null ? new URL(url_spec) : new URL("http", proxyAddress, proxyPort, url_spec);
            LOG.info("connect to " + url);
            final URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            final HttpQueryWriter out = new HttpQueryWriter(connection.getOutputStream());
            out.addParameter("error", message);
            out.addParameter("trace", details);
            out.close();

            final InputStream in = connection.getInputStream();
            int c;
            final StringBuffer result = new StringBuffer();
            while ((c = in.read()) != -1) {
                result.append((char) c);
            }
            LOG.info(result.toString());

            in.close();

        } catch (final UnknownHostException e) {
            LOG.info("could not find host (unknown host) to submit log to");
        } catch (final IOException e) {
            LOG.debug("i/o problem submitting log", e);
        }
    }

}
