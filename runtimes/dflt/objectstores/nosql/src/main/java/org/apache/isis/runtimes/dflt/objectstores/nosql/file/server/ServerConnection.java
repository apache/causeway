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

package org.apache.isis.runtimes.dflt.objectstores.nosql.file.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlStoreException;
import org.apache.isis.runtimes.dflt.objectstores.nosql.file.RemotingException;
import org.apache.log4j.Logger;

public class ServerConnection {

    private static final Logger LOG = Logger.getLogger(ServerConnection.class);

    private final BufferedReader reader;
    private final PrintWriter writer;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private int header;
    private String[] headers;
    private char command;

    public ServerConnection(InputStream input, OutputStream output) {
        outputStream = Util.trace(output, true);
        inputStream = Util.trace(input, true);
        this.reader = new BufferedReader(new InputStreamReader(inputStream, Util.ENCODING));
        this.writer = new PrintWriter(new OutputStreamWriter(outputStream, Util.ENCODING));
        readHeader();
    }

    private void logFailure() {
        LOG.error("(failed " + inputStream + ")");
        LOG.error("(failed " + outputStream + ")");
    }

    public void logComplete() {
        LOG.debug("(complete " + inputStream + ")");
        LOG.debug("(complete " + outputStream + ")");
    }

    boolean readHeader() {
        try {
            String header = reader.readLine();
            LOG.debug("request: " + header);
            if (header == null || header.length() == 0) {
                return false;
            } else {
                command = header.charAt(0);
                headers = header.substring(1).split(" ");
                this.header = 0;
                return true;
            }
        } catch (IOException e) {
            logFailure();
            throw new NoSqlStoreException(e);
        }
    }

    public String getRequest() {
        return headers[header++];
    }

    public int getRequestAsInt() {
        return Integer.valueOf(getRequest()).intValue();
    }

    public char getCommand() {
        return command;
    }

    /**
     * Reads all the data up until the next blank line.
     */
    public String getData() {
        try {
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null && line.length() != 0) {
                buffer.append(line);
                buffer.append('\n');
            }
            return buffer.toString();
        } catch (IOException e) {
            logFailure();
            throw new RemotingException(e);
        }
    }

    public void notFound(String message) {
        writer.println("not-found");
        writer.println(message);
        writer.flush();
    }

    public void error(String message) {
        writer.println("error");
        writer.println(message);
        writer.flush();
    }

    public void error(String message, Exception exception) {
        error(message);
        exception.printStackTrace(writer);
        writer.println();
        writer.println();
        writer.flush();
    }

    private void write(String result) {
        writer.println(result);
        writer.flush();
    }

    public void ok() {
        response(Util.OK, "");
    }

    public void abort() {
        response(Util.ABORT, "");
    }

    public void response(boolean flag) {
        response(Util.OK, " " + (flag ? "true" : "false"));
    }

    public void response(long value) {
        response(Util.OK, " " + Long.toString(value));
    }

    public void response(String message) {
        response(Util.OK, " " + message);
    }

    private void response(String status, String message) {
        String response = status + message;
        LOG.debug("response: " + response);
        write(response);
    }

    public void responseData(String data) {
        write(data);
    }

    public void close() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            logFailure();
            throw new RemotingException(e);
        }
    }

    public void endBlock() {
        writer.println();
    }

}
