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


package org.apache.isis.remoting.exchange;

import java.io.IOException;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;
import org.apache.isis.remoting.facade.ServerFacade;

public class OpenSessionRequest extends RequestAbstract {
	
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String password;

    public OpenSessionRequest(final String username, final String password) {
        super((AuthenticationSession) null);
        this.username = username;
        this.password = password;
        initialized();
    }

    public OpenSessionRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.username = input.readUTF();
        this.password = input.readUTF();
        initialized();
    }

    @Override
	public void encode(final DataOutputExtended output) throws IOException {
    	super.encode(output);
    	output.writeUTF(username);
    	output.writeUTF(password);
    }

	private void initialized() {
		// nothing to do
	}

	
    /////////////////////////////////////////////////////////
    // request data
    /////////////////////////////////////////////////////////

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////


	/**
	 * {@link #setResponse(Object) Sets a response} of an {@link AuthenticationSession}.
	 */
    public void execute(final ServerFacade serverFacade) {
        OpenSessionResponse response = serverFacade.openSession(this);
		setResponse(response);
    }

    /**
     * Downcasts.
     */
    @Override
    public OpenSessionResponse getResponse() {
        return (OpenSessionResponse) super.getResponse();
    }

    
    /////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("sequence", getId());
        return str.toString();
    }


}

