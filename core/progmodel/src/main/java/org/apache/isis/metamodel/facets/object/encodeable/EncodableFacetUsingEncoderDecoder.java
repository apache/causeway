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


package org.apache.isis.metamodel.facets.object.encodeable;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.FacetAbstract;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;


public class EncodableFacetUsingEncoderDecoder extends FacetAbstract implements EncodableFacet {

    private final EncoderDecoder encoderDecoder;
	private final RuntimeContext runtimeContext;

    public EncodableFacetUsingEncoderDecoder(
    		final EncoderDecoder encoderDecoder, 
    		final FacetHolder holder, 
    		final RuntimeContext runtimeContext) {
        super(EncodableFacet.class, holder, false);
        this.encoderDecoder = encoderDecoder;
        this.runtimeContext = runtimeContext;
    }

    // TODO: is this safe? really?
    public static String ENCODED_NULL = "NULL";

    @Override
    protected String toStringValues() {
    	getRuntimeContext().injectDependenciesInto(encoderDecoder);
        return encoderDecoder.toString();
    }

    public ObjectAdapter fromEncodedString(final String encodedData) {
        Assert.assertNotNull(encodedData);
        if (ENCODED_NULL.equals(encodedData)) {
            return null;
        } else {
        	getRuntimeContext().injectDependenciesInto(encoderDecoder);
            Object decodedObject = encoderDecoder.fromEncodedString(encodedData);
			return getRuntimeContext().adapterFor(decodedObject);
        }

    }

    public String toEncodedString(final ObjectAdapter object) {
    	getRuntimeContext().injectDependenciesInto(encoderDecoder);
        return object == null ? ENCODED_NULL : encoderDecoder.toEncodedString(object.getObject());
    }


    
    ////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    ////////////////////////////////////////////////////////
    

    private RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

}

