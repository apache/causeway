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

import org.apache.isis.runtimes.dflt.remoting.common.data.common.IdentityData;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.encoding.DataInputExtended;

public class AuthorizationRequestUsability extends AuthorizationRequestAbstract {
    private static final long serialVersionUID = 1L;
    
    public AuthorizationRequestUsability(
    		final AuthenticationSession session, IdentityData targetData, final String dataStr) {
        super(session, targetData, dataStr);
        initialized();
    }
    
    public AuthorizationRequestUsability(final DataInputExtended input) throws IOException {
    	super(input);
    	initialized();
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // execute, response
    /////////////////////////////////////////////////////////

    public void execute(final ServerFacade serverFacade) {
        AuthorizationResponse response = serverFacade.authorizeUsability(this);
		setResponse(response);
    }
    
    
}
