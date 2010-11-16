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


package org.apache.isis.alternatives.remoting.common.exchange;

import java.io.IOException;

import org.apache.isis.alternatives.remoting.common.data.common.IdentityData;
import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;

public class ClearValueRequest extends RequestAbstract {
    private static final long serialVersionUID = 1L;
    private final String fieldIdentifier;
    private final IdentityData target;

    public ClearValueRequest(final AuthenticationSession session, final String fieldIdentifier, final IdentityData target) {
        super(session);
        this.fieldIdentifier = fieldIdentifier;
        this.target = target;
        initialized();
    }

    public ClearValueRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.fieldIdentifier = input.readUTF();
        this.target = input.readEncodable(IdentityData.class);
        initialized();
    }

    @Override
    public void encode(DataOutputExtended outputStream)
    		throws IOException {
    	super.encode(outputStream);
        outputStream.writeUTF(fieldIdentifier);
        outputStream.writeEncodable(target);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // request data
    /////////////////////////////////////////////////////////

	public String getFieldIdentifier() {
		return fieldIdentifier;
	}
	
	public IdentityData getTarget() {
		return target;
	}
	
    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////

	/**
	 * {@link #setResponse(Object) Sets a response} of {@link ObjectData}[] (array).
	 */
    public void execute(final ServerFacade serverFacade) {
        ClearValueResponse response = serverFacade.clearValue(this);
		setResponse(response);
    }

    /**
     * Downcasts.
     */
    @Override
    public ClearValueResponse getResponse() {
        return (ClearValueResponse) super.getResponse();
    }
}
