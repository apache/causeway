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


package org.apache.isis.runtimes.dflt.remoting.common.client.facets;

import org.apache.isis.runtimes.dflt.remoting.common.data.common.IdentityData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ClearAssociationRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ClearAssociationResponse;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;
import org.apache.isis.runtimes.dflt.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.DecoratingFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacetAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.ConcurrencyException;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;


/**
 * A reflection peer for changing one-to-many fields remotely, instead of on the local machine.
 * 
 * <p>
 * Any requests to add or remove elements from the field will be passed over the network to the server for
 * completion. Only requests on persistent objects are passed to the server; on a transient object the request
 * will always be dealt with locally.
 * 
 * <p>
 * If any of the objects involved have been changed on the server by another process then a
 * {@link ConcurrencyException} will be passed back to the client and re-thrown.
 * </p>
 */
public final class PropertyClearFacetWrapProxy extends PropertyClearFacetAbstract implements DecoratingFacet<PropertyClearFacet> {

    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoder;
    private final PropertyClearFacet underlyingFacet;
    private final String name;

    public PropertyClearFacetWrapProxy(
            final PropertyClearFacet underlyingFacet,
            final ServerFacade serverFacade,
            final ObjectEncoderDecoder encoderDecoder,
            final String name) {
    	super(underlyingFacet.getFacetHolder());
        this.underlyingFacet = underlyingFacet;
        this.serverFacade = serverFacade;
        this.encoder = encoderDecoder;
        this.name = name;
    }

    public PropertyClearFacet getDecoratedFacet() {
        return underlyingFacet;
    }

    public void clearProperty(final ObjectAdapter inObject) {
        if (inObject.isPersistent()) {
            final IdentityData targetReference = encoder.encodeIdentityData(inObject);
            ObjectData[] updates;
            try {
                IdentityData nullData = encoder.encodeIdentityData(null); // not used.
				ClearAssociationRequest request = 
                	new ClearAssociationRequest(getAuthenticationSession(), name, targetReference, nullData);
                ClearAssociationResponse response = serverFacade.clearAssociation(request);
				updates = response.getUpdates();
            } catch (final ConcurrencyException e) {
                throw ProxyUtil.concurrencyException(e);
            }
            encoder.decode(updates);
        } else {
            underlyingFacet.clearProperty(inObject);
        }
    }

	protected static AuthenticationSession getAuthenticationSession() {
		return IsisContext.getAuthenticationSession();
	}

}
