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


package org.apache.isis.runtimes.dflt.runtime.memento;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;

public class StandaloneData extends Data {

	private static final long serialVersionUID = 1L;
	
    private static enum As {
    	ENCODED_STRING(0),
    	SERIALIZABLE(1);
    	static Map<Integer, As> cache = new HashMap<Integer, As>();
    	static {
    		for(As as: values()) {
    			cache.put(as.idx, as);
    		}
    	}
    	private final int idx;
    	private As(int idx) {
    		this.idx = idx;
    	}
    	static As get(int idx) {
    		return cache.get(idx); 
    	}
		public static As readFrom(DataInputExtended input) throws IOException {
			return get(input.readByte());
		}
		public void writeTo(DataOutputExtended output) throws IOException {
			output.writeByte(idx);
		}
    }

	
	private String objectAsEncodedString;
	private Serializable objectAsSerializable;

	public StandaloneData(ObjectAdapter adapter) {
		super(null, adapter.getResolveState().name(), adapter.getSpecification().getFullIdentifier());
		
		Object object = adapter.getObject();
		if (object instanceof Serializable) {
			this.objectAsSerializable = (Serializable) object;
			initialized();
			return;
		}
		
		EncodableFacet encodeableFacet = adapter.getSpecification().getFacet(EncodableFacet.class);
		if (encodeableFacet != null) {
			this.objectAsEncodedString = encodeableFacet.toEncodedString(adapter);
			initialized();
			return;
		}
		
		throw new IllegalArgumentException("Object wrapped by standalone adapter is not serializable and its specificatoin does not have an EncodeableFacet");
	}
	
	public StandaloneData(DataInputExtended input) throws IOException {
		super(input);
		As as = As.readFrom(input);
		if (as == As.SERIALIZABLE) {
			this.objectAsSerializable = input.readSerializable(Serializable.class);
		} else {
			this.objectAsEncodedString = input.readUTF();
		}
		initialized();
	}

	public void encode(DataOutputExtended output) throws IOException {
		super.encode(output);
		if(objectAsSerializable != null) {
			As.SERIALIZABLE.writeTo(output);
			output.writeSerializable(objectAsSerializable);
		} else {
			As.ENCODED_STRING.writeTo(output);
			output.writeUTF(objectAsEncodedString);
		}
	}

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////
    


	public ObjectAdapter getAdapter() {
		if (objectAsSerializable != null) {
			return IsisContext.getPersistenceSession().getAdapterManager().adapterFor(objectAsSerializable);
		} else {
			ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(getClassName());
			EncodableFacet encodeableFacet = spec.getFacet(EncodableFacet.class);
			return encodeableFacet.fromEncodedString(objectAsEncodedString);
		}
	}

}
