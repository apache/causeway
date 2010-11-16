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


package org.apache.isis.extensions.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.isis.core.runtime.persistence.ConcurrencyException;
import org.apache.isis.extensions.file.server.Util;
import org.apache.isis.extensions.nosql.NoSqlStoreException;


public class ClientConnection {

    private static final Logger LOG = Logger.getLogger(ClientConnection.class);

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String[] headers;
    private int header;

    private InputStream inputStream;
    private OutputStream outputStream;

    public ClientConnection(String host, int port, int timeout) {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(timeout);
            outputStream = socket.getOutputStream();
            outputStream = Util.trace(outputStream, true);
            writer = new PrintWriter(new OutputStreamWriter(outputStream, Util.ENCODING));
            inputStream = socket.getInputStream();
            inputStream = Util.trace(inputStream, true);
            reader = new BufferedReader(new InputStreamReader(inputStream, Util.ENCODING));
        } catch (UnknownHostException e) {
            throw new NoSqlStoreException("Unknow host " + host, e);
        } catch (IOException e) {
            throw new NoSqlStoreException("Failed to connect to " + host + ":" + port, e);
        }

    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            LOG.error("Failed to close connection", e);
        }
        writer.close();
    }

    void logComplete() {
        LOG.info("request complete: " + outputStream);
        LOG.debug("response complete: " + inputStream);
    }

    void logFailure() {
        LOG.debug("request failed: " + outputStream);
        LOG.info("response failed: " + inputStream);
    }

    public void request(char command, String request) {
        LOG.debug("request: " + command + request);
        write(command + request);
    }

    public void validateRequest() {
        writer.println();
        writer.flush();
        getReponseHeader();
        String status = readNext();
        if (status.equals("error")) {
            String message = getResponseData();
            throw new RemotingException(message);
        } else if (status.equals("concurrency")) {
            String data = getResponseData();
            // TODO create better exceptions (requires way to restore object/version)
            if (data.startsWith("{")) {
                throw new ConcurrencyException(data, (Throwable) null);

            } else {
                throw new ConcurrencyException(data, (Throwable) null);

            }
        } else if (!status.equals("ok")) {
            throw new RemotingException("Invalid status in response: " + status);
        }
    }

    public void requestData(String data) {
        write(data);
    }

    public void endRequestSection() {
        writer.println();
    }

    private void write(String req) {
        writer.println(req);
    }

    public void getReponseHeader() {
        try {
            String response = reader.readLine();
            LOG.debug("response: " + response);
            headers = response.split(" ");
        } catch (IOException e) {
            throw new NoSqlStoreException(e);
        }
    }

    public String getResponse() {
        return readNext();
    }

    public boolean getResponseAsBoolean() {
        String response = readNext();
        return response.equals("true") ? true : false;
    }

    public long getResponseAsLong() {
        String response = readNext();
        return Long.valueOf(response).longValue();
    }

    /**
     * Read all the data until the next blank line
     */
    public String getResponseData() {
        try {
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null && line.length() > 0) {
                buffer.append(line);
                buffer.append('\n');
            }
            return buffer.toString();
        } catch (Exception e) {
            logFailure();
            LOG.error(e);
            throw new RemotingException(e);
        }
    }

    private String readNext() {
        if (header >= headers.length) {
            throw new RemotingException("attempting to reader header property (index) " + header + " when there are only "
                    + headers.length);
        }
        return headers[header++];
    }

}

