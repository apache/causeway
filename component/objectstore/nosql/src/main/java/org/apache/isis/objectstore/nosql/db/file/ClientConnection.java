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

package org.apache.isis.objectstore.nosql.db.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.objectstore.nosql.NoSqlStoreException;
import org.apache.isis.objectstore.nosql.db.file.server.Util;

public class ClientConnection {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConnection.class);

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private String[] headers;
    private int header;

    public ClientConnection(final InputStream input, final OutputStream output) {
        outputStream = Util.trace(output, true);
        writer = new PrintWriter(new OutputStreamWriter(outputStream, Util.ENCODING));
        inputStream = Util.trace(input, true);
        reader = new BufferedReader(new InputStreamReader(inputStream, Util.ENCODING));
    }

    public void close() {
        try {
            reader.close();
        } catch (final IOException e) {
            LOG.error("Failed to close connection", e);
        }
        writer.close();
    }

    void logComplete() {
        LOG.debug("request complete: " + outputStream);
        LOG.debug("response complete: " + inputStream);
    }

    void logFailure() {
        LOG.info("request failed: " + outputStream);
        LOG.info("response failed: " + inputStream);
    }

    public void request(final char command, final String request) {
        LOG.debug("request: " + command + request);
        write(command + request);
    }

    public void validateRequest() {
        writer.print('\n');
        writer.flush();
        getReponseHeader();
        final String status = readNext();
        if (status.equals("error")) {
            final String message = getResponseData();
            throw new RemotingException(message);
        } else if (status.equals("not-found")) {
            final String message = getResponseData();
            throw new ObjectNotFoundException(message);
        } else if (status.equals("concurrency")) {
            final String data = getResponseData();
            // TODO create better exceptions (requires way to restore
            // object/version)
            if (data.startsWith("{")) {
                throw new ConcurrencyException(data, null);

            } else {
                throw new ConcurrencyException(data, null);

            }
        } else if (!status.equals("ok")) {
            throw new RemotingException("Invalid status in response: " + status);
        }
    }

    public void requestData(final String data) {
        write(data);
    }

    public void endRequestSection() {
        writer.print('\n');
    }

    private void write(final String req) {
        writer.print(req);
        writer.print('\n');
    }

    public void getReponseHeader() {
        try {
            final String response = reader.readLine();
            LOG.debug("response: " + response);
            headers = response.split(" ");
        } catch (final IOException e) {
            throw new NoSqlStoreException(e);
        }
    }

    public String getResponse() {
        return readNext();
    }

    public boolean getResponseAsBoolean() {
        final String response = readNext();
        return response.equals("true") ? true : false;
    }

    public long getResponseAsLong() {
        final String response = readNext();
        return Long.valueOf(response).longValue();
    }

    /**
     * Read all the data until the next blank line
     */
    public String getResponseData() {
        try {
            final StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null && line.length() > 0) {
                buffer.append(line);
                buffer.append('\n');
            }
            return buffer.toString();
        } catch (final Exception e) {
            logFailure();
            LOG.error(e.getMessage(), e);
            throw new RemotingException(e);
        }
    }

    private String readNext() {
        if (header >= headers.length) {
            throw new RemotingException("attempting to reader header property (index) " + header + " when there are only " + headers.length);
        }
        return headers[header++];
    }

}
