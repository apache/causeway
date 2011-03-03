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
import java.io.Serializable;

import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;

public class SetAssociationResponse implements Encodable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final ObjectData[] updates;
	
	public SetAssociationResponse(ObjectData[] updates) {
		this.updates = updates;
		instantiated();
	}

	public SetAssociationResponse(DataInputExtended input) throws IOException {
		this.updates = input.readEncodables(ObjectData.class);
		instantiated();
	}

	public void encode(DataOutputExtended output) throws IOException {
		output.writeEncodables(updates);
	}

	private void instantiated() {
		// nothing to do
	}

	
	///////////////////////////////////////////
	//
	///////////////////////////////////////////

	public ObjectData[] getUpdates() {
		return updates;
	}
	
}
