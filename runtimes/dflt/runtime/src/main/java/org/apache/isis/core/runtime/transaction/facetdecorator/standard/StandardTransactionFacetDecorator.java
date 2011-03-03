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


package org.apache.isis.core.runtime.transaction.facetdecorator.standard;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionClearFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.runtime.transaction.facetdecorator.TransactionFacetDecoratorAbstract;
import org.apache.isis.core.runtime.transaction.facets.ActionInvocationFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.CollectionAddToFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.CollectionClearFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.CollectionRemoveFromFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.PropertyClearFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.PropertySetterFacetWrapTransaction;


public class StandardTransactionFacetDecorator extends TransactionFacetDecoratorAbstract {

    public StandardTransactionFacetDecorator(IsisConfiguration configuration) {
        super(configuration);
    }

    public Facet decorate(final Facet facet, FacetHolder requiredHolder) {
        final Class<? extends Facet> facetType = facet.facetType();
        if (facetType == ActionInvocationFacet.class) {
            ActionInvocationFacet decoratedFacet = (ActionInvocationFacet) facet;
            Facet decoratingFacet = new ActionInvocationFacetWrapTransaction(decoratedFacet);
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == CollectionAddToFacet.class) {
            CollectionAddToFacet decoratedFacet = (CollectionAddToFacet) facet;
            Facet decoratingFacet = new CollectionAddToFacetWrapTransaction(decoratedFacet);
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == CollectionClearFacet.class) {
            CollectionClearFacet decoratedFacet = (CollectionClearFacet) facet;
            Facet decoratingFacet = new CollectionClearFacetWrapTransaction(decoratedFacet);
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == CollectionRemoveFromFacet.class) {
            CollectionRemoveFromFacet decoratedFacet = (CollectionRemoveFromFacet) facet;
            Facet decoratingFacet = new CollectionRemoveFromFacetWrapTransaction(decoratedFacet);
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == PropertyClearFacet.class) {
            PropertyClearFacet decoratedFacet = (PropertyClearFacet) facet;
            Facet decoratingFacet = new PropertyClearFacetWrapTransaction(decoratedFacet);
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        if (facetType == PropertySetterFacet.class) {
            PropertySetterFacet decoratedFacet = (PropertySetterFacet) facet;
            Facet decoratingFacet = new PropertySetterFacetWrapTransaction(decoratedFacet);
			return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }

        return facet;
    }

}

