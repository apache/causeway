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


package org.apache.isis.remoting.marshalling;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.remoting.transport.Transport;


public abstract class MarshallerAbstract implements ClientMarshaller, ServerMarshaller {

	private static final Logger LOG = Logger.getLogger(MarshallerAbstract.class);

	private final IsisConfiguration configuration;
	private final Transport transport;
	
	private boolean keepAlive;

    
    public MarshallerAbstract(
    		final IsisConfiguration configuration, final Transport transport) {
		this.configuration = configuration;
		this.transport = transport;
	}

    //////////////////////////////////////////////////////////
    // init, shutdown
    //////////////////////////////////////////////////////////

    public void init() {
    	transport.init();
        keepAlive = getConfiguration().getBoolean(MarshallingConstants.KEEPALIVE_KEY, MarshallingConstants.KEEPALIVE_DEFAULT);
        if (LOG.isInfoEnabled()) {
        	LOG.info("keepAlive=" + keepAlive);
        }
    }

	public void shutdown() {
    	transport.disconnect();
    	transport.shutdown();
	}


    //////////////////////////////////////////////////////////
    // Settings
    //////////////////////////////////////////////////////////
	
	public boolean isKeepAlive() {
		return keepAlive;
	}

	
    //////////////////////////////////////////////////////////
    // connect
    //////////////////////////////////////////////////////////
	
	public void connect() throws IOException {
		transport.connect();
	}
	
	public void disconnect() {
		transport.disconnect();
	}
	
	/**
	 * Not API.  Whether reconnects are performed depends on the
	 * marshaller/protocol.
	 */
    protected void reconnect() throws IOException {
        disconnect();
        connect();
    }


    //////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    //////////////////////////////////////////////////////////

	public IsisConfiguration getConfiguration() {
		return configuration;
	}
    
	public Transport getTransport() {
		return transport;
	}
	
}
