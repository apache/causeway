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

import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.encoding.DataInputExtended;
import org.apache.isis.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.metamodel.encoding.DataOutputExtended;


public abstract class RequestAbstract implements Request {
	
	
	private static int nextId = 0;
    protected transient Object response;
    private final int id;
    protected final AuthenticationSession session;

    public RequestAbstract(final AuthenticationSession session) {
        this.session = session;
        this.id = nextId++;
        initialized();
    }

    public RequestAbstract(final DataInputExtended input) throws IOException {
        id = input.readInt();
        session = input.readEncodable(AuthenticationSession.class);
        initialized();
    }

    public void encode(final DataOutputExtended output) throws IOException {
        output.writeInt(id);
        output.writeEncodable(session);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////


    public final void setResponse(final Object response) {
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }

    public AuthenticationSession getSession() {
        return session;
    }

    public int getId() {
        return id;
    }
}
