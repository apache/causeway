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
    }
    
    public void readCommand() {
        readHeaders();
    }

    private void logFailure() {
        LOG.error("(failed " + inputStream + ")");
        LOG.error("(failed " + outputStream + ")");
    }

    public void logComplete() {
        LOG.debug("(complete " + inputStream + ")");
        LOG.debug("(complete " + outputStream + ")");
    }

    boolean readHeaders() {
        try {
            String line = reader.readLine();
            LOG.debug("header: " + line);
            if (line == null) {
                logFailure();
                throw new RemotingException("stream ended prematurely while reading header, aborting request");
            }
            if (line.length() == 0) {
                return false;
            } else {
                command = line.charAt(0);
                headers = line.substring(1).split(" ");
                this.header = 0;
                return true;
            }
        } catch (IOException e) {
            logFailure();
            throw new NoSqlStoreException(e);
        }
    }

    public boolean readWriteHeaders() {
        boolean readHeaders = readHeaders();
        if (readHeaders && headers.length != 4) {
            logFailure();
            throw new RemotingException("invalid header string, aborting request");
        }
        return readHeaders;
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

    public void endCommand() {
        try {
            String line = reader.readLine();
            if (line == null) {
                logFailure();
                throw new RemotingException("stream ended prematurely while reading end of command, aborting request");
            }
            if (line.length() > 0) {
                logFailure();
                throw new RemotingException("command didn't end with an empty blank line, aborting request");
            }
        } catch (IOException e) {
            logFailure();
            throw new NoSqlStoreException(e);
        }
    }
    
    /**
     * Reads all the data up until the next blank line.
     */
    public String getData() {
        try {
            StringBuffer buffer = new StringBuffer();
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    logFailure();
                    throw new RemotingException("stream ended prematurely while reading data, aborting request");
                }
                if (line.length() == 0) {
                    break;
                }
                buffer.append(line);
                buffer.append('\n');
            }
            return buffer.toString();
        } catch (IOException e) {
            logFailure();
            throw new RemotingException(e);
        }
    }
/*
    public void getTermination() {
        try {
            String line = reader.readLine();
             if (line == null || !line.equals("***")) {
                logFailure();
                throw new RemotingException("stream ended abruptly while reading data, aborting request");
            }
        } catch (IOException e) {
            logFailure();
            throw new RemotingException(e);
        }

    }
*/
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
