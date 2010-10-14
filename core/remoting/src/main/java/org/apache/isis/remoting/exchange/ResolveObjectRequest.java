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

import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.encoding.DataInputExtended;
import org.apache.isis.metamodel.encoding.DataOutputExtended;
import org.apache.isis.remoting.data.common.IdentityData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.facade.ServerFacade;

public class ResolveObjectRequest extends RequestAbstract {
    private static final long serialVersionUID = 1L;
    private final IdentityData target;

    public ResolveObjectRequest(final AuthenticationSession session, final IdentityData target) {
        super(session);
        this.target = target;
        initialized();
    }

    public ResolveObjectRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.target = input.readEncodable(IdentityData.class);
        initialized();
    }

    @Override
    public void encode(DataOutputExtended output)
    		throws IOException {
    	super.encode(output);
        output.writeEncodable(target);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // request data
    /////////////////////////////////////////////////////////

	public IdentityData getTarget() {
		return target;
	}
	
    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////

	/**
	 * {@link #setResponse(Object) Sets a response} of an {@link ObjectData}.
	 */
    public void execute(final ServerFacade serverFacade) {
        ResolveObjectResponse response = serverFacade.resolveImmediately(this);
		setResponse(response);
    }

    /**
     * Downcasts
     */
    @Override
    public ResolveObjectResponse getResponse() {
        return (ResolveObjectResponse) super.getResponse();
    }


    /////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("target", target);
        return str.toString();
    }
}
