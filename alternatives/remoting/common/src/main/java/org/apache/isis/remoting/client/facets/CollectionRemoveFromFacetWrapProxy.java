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

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.facets.DecoratingFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacetAbstract;
import org.apache.isis.remoting.data.common.IdentityData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.exchange.ClearAssociationRequest;
import org.apache.isis.remoting.exchange.ClearAssociationResponse;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.protocol.ObjectEncoderDecoder;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.ConcurrencyException;
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
public final class CollectionRemoveFromFacetWrapProxy extends CollectionRemoveFromFacetAbstract implements
        DecoratingFacet<CollectionRemoveFromFacet> {

    private final static Logger LOG = Logger.getLogger(CollectionRemoveFromFacetWrapProxy.class);
    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoder;
    private final CollectionRemoveFromFacet underlyingFacet;
    private final String name;

    public CollectionRemoveFromFacetWrapProxy(
            final CollectionRemoveFromFacet underlyingFacet,
            final ServerFacade connection,
            final ObjectEncoderDecoder encoder,
            final String name) {
        super(underlyingFacet.getFacetHolder());
        this.underlyingFacet = underlyingFacet;
        this.serverFacade = connection;
        this.encoder = encoder;
        this.name = name;
    }

    public CollectionRemoveFromFacet getDecoratedFacet() {
        return underlyingFacet;
    }

    /**
     * Remove an associated object (the element) from the specified ObjectAdapter in the association field
     * represented by this object.
     */
    public void remove(final ObjectAdapter inObject, final ObjectAdapter associate) {
        if (inObject.isPersistent()) {
            LOG.debug("clear association remotely " + inObject + "/" + associate);
            try {
                final IdentityData targetReference = encoder.encodeIdentityData(inObject);
                final IdentityData associateReference = encoder.encodeIdentityData(associate);
                ClearAssociationRequest request = new ClearAssociationRequest(getAuthenticationSession(), name, targetReference, associateReference);
				ClearAssociationResponse response = serverFacade.clearAssociation(request);
				ObjectData[] updates = response.getUpdates();
				encoder.decode(updates);
            } catch (final ConcurrencyException e) {
                throw ProxyUtil.concurrencyException(e);
            } catch (final IsisException e) {
                LOG.error("remote exception: " + e.getMessage(), e);
                throw e;
            }
        } else {
            LOG.debug("clear association locally " + inObject + "/" + associate);
            underlyingFacet.remove(inObject, associate);
        }
    }

	protected static AuthenticationSession getAuthenticationSession() {
		return IsisContext.getAuthenticationSession();
	}
}
