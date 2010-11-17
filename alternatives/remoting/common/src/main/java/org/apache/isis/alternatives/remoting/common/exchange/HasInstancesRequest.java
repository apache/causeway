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

import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;

public class HasInstancesRequest extends RequestAbstract {
    private static final long serialVersionUID = 1L;
    private final String specificationName;

    public HasInstancesRequest(final AuthenticationSession session, final String specificationName) {
        super(session);
        this.specificationName = specificationName;
        initialized();
    }

    public HasInstancesRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.specificationName = input.readUTF();
        initialized();
    }

    @Override
    public void encode(DataOutputExtended output)
    		throws IOException {
    	super.encode(output);
        output.writeUTF(specificationName);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // request data
    /////////////////////////////////////////////////////////

	public String getSpecificationName() {
		return specificationName;
	}
	
    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////

	/**
	 * {@link #setResponse(Object) Sets a response} of a {@link HasInstancesResponse}.
	 */
    public void execute(final ServerFacade serverFacade) {
        final HasInstancesResponse response = serverFacade.hasInstances(this);
		setResponse(response);
    }

    /**
     * Downcasts.
     */
    @Override
    public HasInstancesResponse getResponse() {
        return (HasInstancesResponse) super.getResponse();
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", specificationName);
        return str.toString();
    }
}
