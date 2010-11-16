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


package org.apache.isis.remoting.protocol.internal;

import org.apache.isis.alternatives.remoting.common.data.Data;
import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.alternatives.remoting.common.data.query.PersistenceQueryData;
import org.apache.isis.alternatives.remoting.common.exchange.KnownObjectsRequest;
import org.apache.isis.alternatives.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.alternatives.remoting.common.protocol.PersistenceQueryEncoder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.query.PersistenceQuery;

public abstract class PersistenceQueryEncoderAbstract implements PersistenceQueryEncoder {

    private ObjectEncoderDecoder objectEncoder;

    public PersistenceQuery decode(
    		final PersistenceQueryData persistenceQueryData) {
        String typeName = persistenceQueryData.getType();
		return doDecode(loadSpecification(typeName), persistenceQueryData);
    }

    protected abstract PersistenceQuery doDecode(
	            ObjectSpecification specification,
	            PersistenceQueryData persistenceQueryData);

    private ObjectSpecification loadSpecification(String typeName) {
    	return getSpecificationLoader().loadSpecification(
    			typeName);
    }

    

    /**
     * Convenience method for any implementations that need to map over
     * {@link ObjectAdapter}s.
     * 
     * @see #decodeObject(ObjectData)
     */
	protected ObjectData encodeObject(final ObjectAdapter adapter) {
		// REVIEW: this implementation is a bit of a hack...
		Data[] datas = getObjectEncoder().encodeActionParameters(
				new ObjectSpecification[] { adapter.getSpecification() }, 
				new ObjectAdapter[] { adapter }, 
				new KnownObjectsRequest());
		return (ObjectData) datas[0];
	}

	/**
     * Convenience method for any implementations that need to map over
     * {@link ObjectAdapter}s.
     * 
     * @see #encodeObject(ObjectAdapter)
	 */
	protected ObjectAdapter decodeObject(final ObjectData objectData) {
		return getObjectEncoder().decode(objectData);
	}


    /////////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    /////////////////////////////////////////////////////////////////////

    protected ObjectEncoderDecoder getObjectEncoder() {
		return objectEncoder;
	}
	public void setObjectEncoder(ObjectEncoderDecoder objectEncoder) {
		this.objectEncoder = objectEncoder;
	}
    
    /////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    /////////////////////////////////////////////////////////////////////
	
	private static SpecificationLoader getSpecificationLoader() {
		return IsisContext.getSpecificationLoader();
	}


}

