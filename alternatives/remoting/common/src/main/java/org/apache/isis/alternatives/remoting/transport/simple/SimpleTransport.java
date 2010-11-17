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


package org.apache.isis.alternatives.remoting.transport.simple;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.isis.alternatives.remoting.transport.TransportAbstract;
import org.apache.isis.core.metamodel.config.IsisConfiguration;

/**
 * Simple implementation that simply wraps an already existing
 * {@link InputStream} and {@link OutputStream}.
 * 
 * <p>
 * Originally written to assist with refactoring.
 */
public class SimpleTransport extends TransportAbstract {
	
	private final InputStream inputStream;
	private final OutputStream outputStream;
	
	public SimpleTransport(IsisConfiguration configuration,
			InputStream inputStream, OutputStream outputStream) {
		super(configuration);
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}


	//////////////////////////////////////////////////////
	// init, shutdown
	//////////////////////////////////////////////////////
	
	public void init() {
		// does nothing
	}

	public void shutdown() {
		// does nothing
	}

	//////////////////////////////////////////////////////
	// connect, disconnect
	//////////////////////////////////////////////////////

	public void connect() {
		// does nothing
	}

	public void disconnect() {
		// does nothing
	}

	//////////////////////////////////////////////////////
	// input & output streams
	//////////////////////////////////////////////////////

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

}
