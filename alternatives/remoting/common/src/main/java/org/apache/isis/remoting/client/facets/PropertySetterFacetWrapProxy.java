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


package org.apache.isis.remoting.client.facets;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.facets.DecoratingFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertySetterFacetAbstract;
import org.apache.isis.remoting.data.common.EncodableObjectData;
import org.apache.isis.remoting.data.common.IdentityData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.exchange.SetAssociationRequest;
import org.apache.isis.remoting.exchange.SetAssociationResponse;
import org.apache.isis.remoting.exchange.SetValueRequest;
import org.apache.isis.remoting.exchange.SetValueResponse;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.protocol.ObjectEncoderDecoder;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.ConcurrencyException;


/**
 * A reflection peer for changing one-to-many fields remotely, instead of on the local machine. Any requests
 * to add or remove elements from the field will be passed over the network to the server for completion. Only
 * requests on persistent objects are passed to the server; on a transient object the request will always be
 * dealt with locally.
 *
 * <p>
 * If any of the objects involved have been changed on the server by another process then a
 * ConcurrencyException will be passed back to the client and re-thrown.
 * </p>
 */
public final class PropertySetterFacetWrapProxy extends PropertySetterFacetAbstract implements
        DecoratingFacet<PropertySetterFacet> {

    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoder;
    private final PropertySetterFacet underlyingFacet;
    private final String name;

    public PropertySetterFacetWrapProxy(
            final PropertySetterFacet underlyingFacet,
            final ServerFacade distribution,
            final ObjectEncoderDecoder encoder,
            final String name) {
        super(underlyingFacet.getFacetHolder());
        this.underlyingFacet = underlyingFacet;
        this.serverFacade = distribution;
        this.encoder = encoder;
        this.name = name;
    }

    public PropertySetterFacet getDecoratedFacet() {
        return underlyingFacet;
    }

    public void setProperty(final ObjectAdapter targetAdapter, final ObjectAdapter associateAdapter) {
        if (targetAdapter.isPersistent()) {
            final IdentityData targetReference = encoder.encodeIdentityData(targetAdapter);
            try {
                ObjectSpecification associatedSpec = associateAdapter.getSpecification();
                if (associatedSpec.isCollection()) {
                    // silently ignore; shouldn't happen
                } else {
                    ObjectData[] updates;
                    if (!associatedSpec.isValueOrIsAggregated()) {
                        final IdentityData associateReference = encoder.encodeIdentityData(associateAdapter);
                        SetAssociationRequest request = new SetAssociationRequest(getAuthenticationSession(), name, targetReference, associateReference);
                        SetAssociationResponse response =
                            serverFacade.setAssociation(request);
                        updates = response.getUpdates();
                    } else {
                        final EncodableObjectData val = encoder.encodeAsValue(associateAdapter);
                        SetValueRequest request = new SetValueRequest(getAuthenticationSession(), name, targetReference, val);
                        SetValueResponse response = serverFacade.setValue(request);
                        updates = response.getUpdates();
                    }
                    encoder.decode(updates);
                }
            } catch (final ConcurrencyException e) {
                throw ProxyUtil.concurrencyException(e);
            }
        } else {
            underlyingFacet.setProperty(targetAdapter, associateAdapter);
        }
    }

	private static AuthenticationSession getAuthenticationSession() {
		return IsisContext.getAuthenticationSession();
	}

}
