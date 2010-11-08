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
import java.io.Serializable;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.encoding.Encodable;

public class ResponseEnvelope implements Encodable, Serializable {
	
    private static final long serialVersionUID = 1L;
    
    private final int id;
    private final Object object;

    public ResponseEnvelope(final Request request) {
        this(request.getId(), request.getResponse());
    }

    private ResponseEnvelope(final int requestId, final Object responseObject) {
        this.id = requestId;
        this.object = responseObject;
        initialized();
    }

    public ResponseEnvelope(final DataInputExtended input) throws IOException {
    	this.id = input.readInt();
    	this.object = input.readEncodable(Object.class);
    	initialized();
    }
    
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeInt(id);
        output.writeEncodable(object);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////

    public int getId() {
        return id;
    }

    public Object getObject() {
    	return object;
    }
    
    /////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("id", id);
        str.append("object", object);
        return str.toString();
    }
}
