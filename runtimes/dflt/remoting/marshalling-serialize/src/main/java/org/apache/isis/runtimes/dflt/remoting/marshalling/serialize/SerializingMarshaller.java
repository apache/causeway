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

package org.apache.isis.runtimes.dflt.remoting.marshalling.serialize;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.SocketException;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.Request;
import org.apache.isis.runtimes.dflt.remoting.common.marshalling.MarshallerAbstract;
import org.apache.isis.runtimes.dflt.remoting.common.protocol.IllegalRequestException;
import org.apache.isis.runtimes.dflt.remoting.transport.ConnectionException;
import org.apache.isis.runtimes.dflt.remoting.transport.Transport;
import org.apache.log4j.Logger;

public class SerializingMarshaller extends MarshallerAbstract {

    private static final Logger LOG = Logger.getLogger(SerializingMarshaller.class);

    private ObjectInputStream input;
    private ObjectOutputStream output;

    public SerializingMarshaller(final IsisConfiguration configuration, final Transport transport) {
        super(configuration, transport);
    }

    // //////////////////////////////////////////////////////
    // connect (client-side impl)
    // //////////////////////////////////////////////////////

    @Override
    public void connect() throws IOException {
        super.connect();

        this.input = new ObjectInputStream(getTransport().getInputStream());
        this.output = new ObjectOutputStream(getTransport().getOutputStream());
    }

    // //////////////////////////////////////////////////////
    // ServerMarshaller impl
    // //////////////////////////////////////////////////////

    @Override
    public Object request(final Request request) throws IOException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("sending request" + request);
            }
            final Serializable serializableRequest = asSerializable(request);
            try {
                output.writeObject(serializableRequest);
            } catch (final SocketException e) {
                reconnect();
                output.writeObject(serializableRequest);
            }
            output.flush();
            final Object object = input.readObject();
            if (LOG.isDebugEnabled()) {
                LOG.debug("response received: " + object);
            }
            return object;
            /*
             * } catch (StreamCorruptedException e) { try { int available =
             * input.available(); LOG.debug("error in reading; skipping bytes: "
             * + available); input.skip(available); } catch (IOException e1) {
             * e1.printStackTrace(); } throw new
             * ConnectionException(e.getMessage(), e);
             */
        } catch (final ClassNotFoundException e) {
            throw new ConnectionException("Failed request", e);
        }
    }

    private Serializable asSerializable(final Request request) {
        return (Serializable) request;
    }

    // //////////////////////////////////////////////////////
    // ServerMarshaller impl
    // //////////////////////////////////////////////////////

    @Override
    public Request readRequest() throws IOException {
        try {
            final Request request = (Request) input.readObject();
            return request;
        } catch (final ClassNotFoundException e) {
            throw new IllegalRequestException("unknown class received; closing connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendError(final IsisException exception) throws IOException {
        send(exception);
    }

    @Override
    public void sendResponse(final Object response) throws IOException {
        send(response);
    }

    private void send(final Object object) throws IOException {
        output.writeObject(object);
        output.flush();
    }

}
