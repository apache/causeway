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


package org.apache.isis.extensions.xstream.shared;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.SocketException;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.remoting.exchange.Request;
import org.apache.isis.remoting.marshalling.MarshallerAbstract;
import org.apache.isis.remoting.protocol.IllegalRequestException;
import org.apache.isis.remoting.transport.ConnectionException;
import org.apache.isis.remoting.transport.Transport;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;


public class XStreamMarshaller extends MarshallerAbstract {
    
	private static final Logger LOG = Logger.getLogger(XStreamMarshaller.class);
    private final XStream xstream = new XStream();
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public XStreamMarshaller(
    		final IsisConfiguration configuration,
    		final Transport transport) {
    	super(configuration, transport);
    }
    
    
    ////////////////////////////////////////////////////////
    // common Marshaller impl
    ////////////////////////////////////////////////////////

	public void connect() throws IOException {
		super.connect();
    	
    	this.output = new ObjectOutputStream(getTransport().getOutputStream());
        this.input = new ObjectInputStream(getTransport().getInputStream());
    }

    
    ////////////////////////////////////////////////////////
    // ServerMarshaller impl
    ////////////////////////////////////////////////////////

    public Object request(final Request request) throws IOException {
        final String requestData = xstream.toXML(request);
        if (LOG.isInfoEnabled()) {
            LOG.info("sending " + requestData.length() + " bytes of data");
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("sending request \n" + requestData);
            }
            try {
                output.writeObject(requestData);
            } catch (final SocketException e) {
                reconnect();
                output.writeObject(requestData);
            }
            output.flush();
            final String responseData = (String) input.readObject();
            if (LOG.isDebugEnabled()) {
                LOG.debug("response received: \n" + responseData);
            }
            return xstream.fromXML(responseData);

        } catch (final StreamCorruptedException e) {
            try {
                final int available = input.available();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("error in reading; skipping bytes: " + available);
                }
                input.skip(available);
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
            throw new ConnectionException(e.getMessage(), e);
        } catch (final ClassNotFoundException e) {
            throw new ConnectionException("Failed request", e);
        }
    }

    
    ////////////////////////////////////////////////////////
    // ServerMarshaller impl
    ////////////////////////////////////////////////////////
    
    
    public Request readRequest() throws IOException {
        try {
            final String requestData = (String) input.readObject();
            if (LOG.isDebugEnabled()) {
                LOG.debug("request received \n" + requestData);
            }
            final Request request = (Request) xstream.fromXML(requestData);
            return request;
        } catch (final ClassNotFoundException e) {
            throw new IllegalRequestException("unknown class received; closing connection: " + e.getMessage(), e);
        }
    }

    public void sendError(final IsisException exception) throws IOException {
        final String responseData = xstream.toXML(exception);
        sendData(responseData);
    }

    public void sendResponse(final Object response) throws IOException {
        final String responseData = xstream.toXML(response);
        sendData(responseData);
    }

    private void sendData(final String responseData) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("send response \n" + responseData);
        }
        output.writeObject(responseData);
        output.flush();
    }


}
