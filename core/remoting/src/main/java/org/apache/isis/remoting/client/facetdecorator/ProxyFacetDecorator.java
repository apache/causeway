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


package org.apache.isis.remoting.client.facetdecorator;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facetdecorator.FacetDecoratorAbstract;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.identifier.Identified;
import org.apache.isis.remoting.client.facets.ActionInvocationFacetWrapProxy;
import org.apache.isis.remoting.client.facets.CollectionAddToFacetWrapProxy;
import org.apache.isis.remoting.client.facets.CollectionRemoveFromFacetWrapProxy;
import org.apache.isis.remoting.client.facets.PropertyClearFacetWrapProxy;
import org.apache.isis.remoting.client.facets.PropertySetterFacetWrapProxy;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.protocol.encoding.internal.ObjectEncoderDecoder;

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
    	if (!(requiredHolder instanceof Identified)) {
            return null;
        }
        Identified identified = (Identified) requiredHolder;

        final Class<? extends Facet> facetType = facet.facetType();

        if (facetType == PropertySetterFacet.class) {
            final PropertySetterFacet propertySetterFacet = (PropertySetterFacet) facet;
            PropertySetterFacetWrapProxy decoratingFacet = new PropertySetterFacetWrapProxy(
            		propertySetterFacet, serverFacade, encoderDecoder,
            		identified.getIdentifier().getMemberName());
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == PropertyClearFacet.class) {
            final PropertyClearFacet propertyClearFacet = (PropertyClearFacet) facet;
            PropertyClearFacetWrapProxy decoratingFacet = new PropertyClearFacetWrapProxy(
            		propertyClearFacet, serverFacade, encoderDecoder,
            		identified.getIdentifier().getMemberName());
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == CollectionAddToFacet.class) {
            final CollectionAddToFacet collectionAddToFacet = (CollectionAddToFacet) facet;
            CollectionAddToFacetWrapProxy decoratingFacet = new CollectionAddToFacetWrapProxy(
            		collectionAddToFacet, serverFacade, encoderDecoder,
            		identified.getIdentifier().getMemberName());
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == CollectionRemoveFromFacet.class) {
            final CollectionRemoveFromFacet collectionRemoveFromFacet = (CollectionRemoveFromFacet) facet;
            CollectionRemoveFromFacetWrapProxy decoratingFacet = new CollectionRemoveFromFacetWrapProxy(
            		collectionRemoveFromFacet, serverFacade, encoderDecoder,
            		identified.getIdentifier().getMemberName());
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == ActionInvocationFacet.class) {
            ActionInvocationFacet invocationFacet = (ActionInvocationFacet) facet;
			ObjectAction objectAction = (ObjectAction) requiredHolder;
			ActionInvocationFacetWrapProxy decoratingFacet = new ActionInvocationFacetWrapProxy(
            		invocationFacet, serverFacade, encoderDecoder, objectAction);
			return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        return facet;
    }

    @SuppressWarnings("unchecked")
	public Class<? extends Facet>[] getFacetTypes() {
        return new Class[] { PropertySetterFacet.class, PropertyClearFacet.class, CollectionAddToFacet.class, CollectionRemoveFromFacet.class, ActionInvocationFacet.class };
    }

}

