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
import org.apache.isis.remoting.data.common.ReferenceData;
import org.apache.isis.remoting.facade.ServerFacade;

public class ExecuteClientActionRequest extends RequestAbstract {
    private static final long serialVersionUID = 1L;
    private final int[] types;
    private final ReferenceData[] data;

    public ExecuteClientActionRequest(final AuthenticationSession session, final ReferenceData[] data, final int[] types) {
        super(session);
        this.data = data;
        this.types = types;
        initialized();
    }

    public ExecuteClientActionRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.data = input.readEncodables(ReferenceData.class);
        this.types = input.readInts();
        initialized();
    }

    @Override
    public void encode(DataOutputExtended output)
    		throws IOException {
    	super.encode(output);
    	output.writeEncodables(data);
    	output.writeInts(types);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // request data
    /////////////////////////////////////////////////////////
	
	public ReferenceData[] getData() {
		return data;
	}
	public int[] getTypes() {
		return types;
	}
	
    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////
	
	/**
	 * {@link #setResponse(Object) Sets a response} of a {@link ExecuteClientActionResponse}.
	 */
    public void execute(final ServerFacade serverFacade) {
        ExecuteClientActionResponse response = serverFacade.executeClientAction(this);
		setResponse(response);
    }

    /**
     * Downcasts.
     */
    @Override
    public ExecuteClientActionResponse getResponse() {
        return (ExecuteClientActionResponse) super.getResponse();
    }

    
    /////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("data", data.length);
        return str.toString();
    }
}
