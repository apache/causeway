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


package org.apache.isis.runtimes.dflt.remoting.common.client.facetdecorator;

import org.apache.isis.runtimes.dflt.remoting.common.client.facets.ActionInvocationFacetWrapProxy;
import org.apache.isis.runtimes.dflt.remoting.common.client.facets.CollectionAddToFacetWrapProxy;
import org.apache.isis.runtimes.dflt.remoting.common.client.facets.CollectionRemoveFromFacetWrapProxy;
import org.apache.isis.runtimes.dflt.remoting.common.client.facets.PropertyClearFacetWrapProxy;
import org.apache.isis.runtimes.dflt.remoting.common.client.facets.PropertySetterFacetWrapProxy;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;
import org.apache.isis.runtimes.dflt.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorAbstract;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public class ProxyFacetDecorator  extends FacetDecoratorAbstract {

	@SuppressWarnings("unused")
	private final IsisConfiguration configuration;
    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoderDecoder;

    public ProxyFacetDecorator(
            final IsisConfiguration configuration,
            final ServerFacade serverFacade,
            final ObjectEncoderDecoder encoderDecoder) {
    	this.configuration = configuration;
        this.serverFacade = serverFacade;
        this.encoderDecoder = encoderDecoder;
    }

    public Facet decorate(final Facet facet, FacetHolder requiredHolder) {
    	if (!(requiredHolder instanceof IdentifiedHolder)) {
            return null;
        }
        IdentifiedHolder identifiedHolder = (IdentifiedHolder) requiredHolder;

        final Class<? extends Facet> facetType = facet.facetType();

        if (facetType == PropertySetterFacet.class) {
            final PropertySetterFacet propertySetterFacet = (PropertySetterFacet) facet;
            PropertySetterFacetWrapProxy decoratingFacet = new PropertySetterFacetWrapProxy(
            		propertySetterFacet, serverFacade, encoderDecoder,
            		identifiedHolder.getIdentifier().getMemberName());
            requiredHolder.addFacet(decoratingFacet);
			return decoratingFacet;
        }

        if (facetType == PropertyClearFacet.class) {
            final PropertyClearFacet propertyClearFacet = (PropertyClearFacet) facet;
            PropertyClearFacetWrapProxy decoratingFacet = new PropertyClearFacetWrapProxy(
            		propertyClearFacet, serverFacade, encoderDecoder,
            		identifiedHolder.getIdentifier().getMemberName());
            requiredHolder.addFacet(decoratingFacet);
			return decoratingFacet;
        }

        if (facetType == CollectionAddToFacet.class) {
            final CollectionAddToFacet collectionAddToFacet = (CollectionAddToFacet) facet;
            CollectionAddToFacetWrapProxy decoratingFacet = new CollectionAddToFacetWrapProxy(
            		collectionAddToFacet, serverFacade, encoderDecoder,
            		identifiedHolder.getIdentifier().getMemberName());
            requiredHolder.addFacet(decoratingFacet);
			return decoratingFacet;
        }

        if (facetType == CollectionRemoveFromFacet.class) {
            final CollectionRemoveFromFacet collectionRemoveFromFacet = (CollectionRemoveFromFacet) facet;
            CollectionRemoveFromFacetWrapProxy decoratingFacet = new CollectionRemoveFromFacetWrapProxy(
            		collectionRemoveFromFacet, serverFacade, encoderDecoder,
            		identifiedHolder.getIdentifier().getMemberName());
            requiredHolder.addFacet(decoratingFacet);
			return decoratingFacet;
        }

        if (facetType == ActionInvocationFacet.class) {
            ActionInvocationFacet invocationFacet = (ActionInvocationFacet) facet;
			ObjectAction objectAction = (ObjectAction) requiredHolder;
			ActionInvocationFacetWrapProxy decoratingFacet = new ActionInvocationFacetWrapProxy(
            		invocationFacet, serverFacade, encoderDecoder, objectAction);
			requiredHolder.addFacet(decoratingFacet);
			return decoratingFacet;
        }

        return facet;
    }

    @SuppressWarnings("unchecked")
	public Class<? extends Facet>[] getFacetTypes() {
        return new Class[] { PropertySetterFacet.class, PropertyClearFacet.class, CollectionAddToFacet.class, CollectionRemoveFromFacet.class, ActionInvocationFacet.class };
    }

}

