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


package org.apache.isis.remoting.client;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.isis.remoting.IsisRemoteException;
import org.apache.isis.remoting.exchange.Request;
import org.apache.isis.remoting.exchange.ResponseEnvelope;
import org.apache.isis.remoting.protocol.ClientMarshaller;
import org.apache.isis.remoting.transport.ConnectionException;
import org.apache.isis.runtime.persistence.ConcurrencyException;



/**
 * Default implementation of {@link ClientConnection} that delegates to
 * {@link ClientMarshaller} supplied in {@link ClientConnectionDefault constructor}.
 */
public class ClientConnectionDefault  implements ClientConnection {

    @SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ClientConnectionDefault.class);

    private final ClientMarshaller marshaller;


    //////////////////////////////////////////////////////////
    // Constructor
    //////////////////////////////////////////////////////////

    public ClientConnectionDefault(
    		final ClientMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    protected ClientMarshaller getMarshaller() {
        return marshaller;
    }

    
    //////////////////////////////////////////////////////////
    // init, shutdown
    //////////////////////////////////////////////////////////

    public void init() {
    	marshaller.init();
    }

    public void shutdown() {
    	marshaller.shutdown();
    }


    //////////////////////////////////////////////////////////
    // reconnect, connect, disconnect
    //////////////////////////////////////////////////////////

    private void connect() {
        try {
			marshaller.connect();
        } catch (final IOException e) {
            throw new ConnectionException("Connection failure", e);
		}
    }

    private void disconnect() {
    	marshaller.disconnect();
    }


    //////////////////////////////////////////////////////////
    // executeRemotely
    //////////////////////////////////////////////////////////

    public ResponseEnvelope executeRemotely(final Request request) {
    	connect();
        try {
            return executeRemotelyElseException(request);
        } catch (final IOException e) {
            throw new ConnectionException("Failed request", e);
        } finally {
        	disconnect();
        }
    }

	private ResponseEnvelope executeRemotelyElseException(final Request request)
			throws IOException {

		Object response = marshaller.request(request);
		
		if (response instanceof ConcurrencyException) {
			throw (ConcurrencyException) response;
		} else if (response instanceof Exception) {
			throw new IsisRemoteException(
					"Exception occurred on server", (Throwable) response);
		} else {
			return (ResponseEnvelope) response;
		}
	}


}
