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
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;

public abstract class AuthorizationRequestAbstract extends RequestAbstract {
    
    private final String dataStr;
    private final IdentityData targetData;

    public AuthorizationRequestAbstract(final AuthenticationSession session, IdentityData targetData, final String data) {
        super(session);
        this.targetData = targetData;
        this.dataStr = data;
        initialized();
    }
    
    public AuthorizationRequestAbstract(final DataInputExtended input) throws IOException {
    	super(input);
    	this.targetData = input.readEncodable(IdentityData.class);
    	this.dataStr = input.readUTF();
    	initialized();
    }

    @Override
    public void encode(DataOutputExtended output)
    		throws IOException {
    	super.encode(output);
    	output.writeEncodable(targetData);
    	output.writeUTF(dataStr);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // request
    /////////////////////////////////////////////////////////

	public IdentityData getTarget() {
		return targetData;
	}
	
	public String getIdentifier() {
		return dataStr;
	}

    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////

    
    /**
     * Downcasts.
     */
    @Override
    public AuthorizationResponse getResponse() {
        return (AuthorizationResponse) super.getResponse();
    }
    
    
}
