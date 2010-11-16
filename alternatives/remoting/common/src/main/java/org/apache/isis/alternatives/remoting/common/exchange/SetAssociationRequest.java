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
import org.apache.isis.core.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;

public class SetAssociationRequest extends RequestAbstract {
	
    private static final long serialVersionUID = 1L;
    
    private final String fieldIdentifier;
    private final IdentityData target;
    private final IdentityData associate;

    public SetAssociationRequest(
            final AuthenticationSession session,
            final String fieldIdentifier,
            final IdentityData target,
            final IdentityData associate) {
        super(session);
        this.fieldIdentifier = fieldIdentifier;
        this.target = target;
        this.associate = associate;
        initialized();
    }

    public SetAssociationRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.fieldIdentifier = input.readUTF();
        this.target = input.readEncodable(IdentityData.class);
        this.associate = input.readEncodable(IdentityData.class);
        initialized();
    }

    @Override
    public void encode(DataOutputExtended output)
    		throws IOException {
    	super.encode(output);
        output.writeUTF(fieldIdentifier);
        output.writeEncodable(target);
        output.writeEncodable(associate);
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
	
	public IdentityData getAssociate() {
		return associate;
	}
	
    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////

	/**
	 * {@link #setResponse(Object) Sets a response} of {@link SetAssociationResponse}.
	 */
    public void execute(final ServerFacade serverFacade) {
        SetAssociationResponse response = serverFacade.setAssociation(this);
		setResponse(response);
    }

    /**
     * Downcasts.
     */
    @Override
    public SetAssociationResponse getResponse() {
        return (SetAssociationResponse) super.getResponse();
    }
}
