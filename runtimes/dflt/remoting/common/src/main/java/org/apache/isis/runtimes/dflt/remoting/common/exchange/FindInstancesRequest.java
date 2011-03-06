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


package org.apache.isis.runtimes.dflt.remoting.common.exchange;

import java.io.IOException;

import org.apache.isis.runtimes.dflt.remoting.common.data.query.PersistenceQueryData;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.lang.ToString;

public class FindInstancesRequest extends RequestAbstract {
    private static final long serialVersionUID = 1L;
    private final PersistenceQueryData criteria;

    public FindInstancesRequest(final AuthenticationSession session, final PersistenceQueryData criteria) {
        super(session);
        this.criteria = criteria;
        initialized();
    }

    public FindInstancesRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.criteria = input.readEncodable(PersistenceQueryData.class);
        initialized();
    }

    @Override
    public void encode(DataOutputExtended output)
    		throws IOException {
    	super.encode(output);
        output.writeEncodable(criteria);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // request data
    /////////////////////////////////////////////////////////

	public PersistenceQueryData getCriteria() {
		return criteria;
	}
	
    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////

	/**
	 * {@link #setResponse(Object) Sets a response} of a {@link FindInstancesResponse}.
	 */
    public void execute(final ServerFacade serverFacade) {
        FindInstancesResponse response = serverFacade.findInstances(this);
		setResponse(response);
    }

    /**
     * Downcasts.
     */
    @Override
    public FindInstancesResponse getResponse() {
        return (FindInstancesResponse) super.getResponse();
    }

    
    /////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("criteria", criteria);
        return str.toString();
    }
}
