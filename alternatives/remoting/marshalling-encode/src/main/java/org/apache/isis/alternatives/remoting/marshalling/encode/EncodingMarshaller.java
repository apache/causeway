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

package org.apache.isis.alternatives.remoting.marshalling.encode;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.isis.alternatives.remoting.common.exchange.Request;
import org.apache.isis.alternatives.remoting.common.marshalling.MarshallerAbstract;
import org.apache.isis.alternatives.remoting.common.marshalling.MarshallingConstants;
import org.apache.isis.alternatives.remoting.transport.Transport;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputStreamExtended;
import org.apache.isis.core.metamodel.encoding.DebugDataInputExtended;
import org.apache.isis.core.metamodel.encoding.DebugDataOutputExtended;
import org.apache.isis.core.metamodel.encoding.Encodable;
import org.apache.log4j.Logger;

public class EncodingMarshaller extends MarshallerAbstract {

    public EncodingMarshaller(final IsisConfiguration configuration, final Transport transport) {
        super(configuration, transport);
    }

    private static final Logger LOG = Logger.getLogger(EncodingMarshaller.class);

    private static enum As {
        ENCODABLE(0) {
            @Override
            public void writeObject(DataOutputExtended output, Object object) throws IOException {
                writeTo(output);
                output.writeEncodable(object);
            }

            @Override
            public <T> T readObject(DataInputExtended input, Class<T> cls) throws IOException {
                // not quite symmetrical with write; the byte has already been read from stream
                // to determine which As instance to delegate to
                return input.readEncodable(cls);
            }
        },
        SERIALIZABLE(1) {
            @Override
            public void writeObject(DataOutputExtended output, Object object) throws IOException {
                writeTo(output);
                output.writeSerializable(object);
            }

            @Override
            public <T> T readObject(DataInputExtended input, Class<T> cls) throws IOException {
                // not quite symmetrical with write; the byte has already been read from stream
                // to determine which As instance to delegate to
                return input.readSerializable(cls);
            }
        };
        static Map<Integer, As> cache = new HashMap<Integer, As>();
        static {
            for (As as : values()) {
                cache.put(as.idx, as);
            }
        }
        private final int idx;

        private As(int idx) {
            this.idx = idx;
        }

        static As get(int idx) {
            return cache.get(idx);
        }

        public static As readFrom(DataInputExtended input) throws IOException {
            return get(input.readByte());
        }

        public void writeTo(DataOutputExtended output) throws IOException {
            output.writeByte(idx);
        }

        public abstract void writeObject(DataOutputExtended output, Object object) throws IOException;

        public abstract <T> T readObject(DataInputExtended input, Class<T> cls) throws IOException;
    }

    // ////////////////////////////////////////////////////////////////
    // Properties
    // ////////////////////////////////////////////////////////////////

    private boolean debugging;

    private DataInputExtended input;
    private DataOutputExtended output;

    // ////////////////////////////////////////////////////////////////
    // Common to both ClientMarshaller and ServerMarshaller
    // ////////////////////////////////////////////////////////////////

    @Override
    public void init() {
        super.init();
        debugging =
            getConfiguration().getBoolean(MarshallingConstants.DEBUGGING_KEY, MarshallingConstants.DEBUGGING_DEFAULT);
        if (LOG.isInfoEnabled()) {
            LOG.info("debugging=" + debugging);
        }
    }

    // ////////////////////////////////////////////////////////////////
    // Common to both ClientMarshaller and ServerMarshaller
    // ////////////////////////////////////////////////////////////////

    @Override
    public void connect() throws IOException {
        super.connect();

        this.input = new DataInputStreamExtended(getTransport().getInputStream());
        this.output = new DataOutputStreamExtended(getTransport().getOutputStream());

        if (debugging) {
            this.input = new DebugDataInputExtended(input);
            this.output = new DebugDataOutputExtended(output);
        }
    }

    private void writeToOutput(final Object object) throws IOException {
        // expect to be either Encodable or Serializable, prefer the former.
        if (object instanceof Encodable) {
            As.ENCODABLE.writeObject(output, object);
        } else {
            As.SERIALIZABLE.writeObject(output, object);
        }
        output.flush();
    }

    private <T> T readFromInput(Class<T> cls) throws IOException {
        As as = As.readFrom(input);
        return as.readObject(input, cls);
    }

    // ////////////////////////////////////////////////////////////////
    // ClientMarshaller impl
    // ////////////////////////////////////////////////////////////////

    @Override
    public Object request(final Request request) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sending request " + request);
        }
        try {
            writeToOutput(request);
        } catch (final SocketException e) {
            reconnect();
            writeToOutput(request);
        }
        final Object object = readFromInput(Object.class);
        if (LOG.isDebugEnabled()) {
            LOG.debug("response received: " + object);
        }
        if (!isKeepAlive()) {
            disconnect();
        }
        return object;
    }

    // ////////////////////////////////////////////////////////////////
    // ServerMarshaller impl
    // ////////////////////////////////////////////////////////////////

    @Override
    public Request readRequest() throws IOException {
        final Request request = readFromInput(Request.class);
        if (LOG.isDebugEnabled()) {
            LOG.debug("request received: " + request);
        }
        return request;
    }

    @Override
    public void sendResponse(final Object response) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("send response: " + response);
        }
        writeToOutput(response);
    }

    @Override
    public void sendError(final IsisException exception) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("send error: " + exception);
        }
        writeToOutput(exception);
    }

}
