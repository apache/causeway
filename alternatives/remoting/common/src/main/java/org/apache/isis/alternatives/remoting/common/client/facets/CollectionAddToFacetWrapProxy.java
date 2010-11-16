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


package org.apache.isis.alternatives.remoting.common.client.facets;

import org.apache.isis.alternatives.remoting.common.data.common.IdentityData;
import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.alternatives.remoting.common.exchange.SetAssociationRequest;
import org.apache.isis.alternatives.remoting.common.exchange.SetAssociationResponse;
import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.isis.alternatives.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.facets.DecoratingFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionAddToFacetAbstract;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.ConcurrencyException;
import org.apache.log4j.Logger;


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
public final class CollectionAddToFacetWrapProxy extends CollectionAddToFacetAbstract implements
        DecoratingFacet<CollectionAddToFacet> {

    private final static Logger LOG = Logger.getLogger(CollectionAddToFacetWrapProxy.class);
    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoderDecoder;
    private final CollectionAddToFacet underlyingFacet;
    private final String name;

    public CollectionAddToFacetWrapProxy(
            final CollectionAddToFacet underlyingFacet,
            final ServerFacade connection,
            final ObjectEncoderDecoder encoderDecoder,
            final String name) {
        super(underlyingFacet.getFacetHolder());
        this.underlyingFacet = underlyingFacet;
        this.serverFacade = connection;
        this.encoderDecoder = encoderDecoder;
        this.name = name;
    }

    public CollectionAddToFacet getDecoratedFacet() {
        return underlyingFacet;
    }

    public void add(final ObjectAdapter inObject, final ObjectAdapter referencedAdapter) {
        if (inObject.isPersistent()) {
            try {
                final IdentityData targetReference = encoderDecoder.encodeIdentityData(inObject);
                final IdentityData associateReference = encoderDecoder.encodeIdentityData(referencedAdapter);
                SetAssociationRequest request = new SetAssociationRequest(getAuthenticationSession(), name, targetReference, associateReference);
				SetAssociationResponse response = 
                	serverFacade.setAssociation(request);
                ObjectData[] updates = response.getUpdates();
				encoderDecoder.decode(updates);
            } catch (final ConcurrencyException e) {
                throw ProxyUtil.concurrencyException(e);
            } catch (final IsisException e) {
                LOG.error("remote exception: " + e.getMessage(), e);
                throw e;
            }
        } else {
            underlyingFacet.add(inObject, referencedAdapter);
        }
    }

	protected static AuthenticationSession getAuthenticationSession() {
		return IsisContext.getAuthenticationSession();
	}


}
