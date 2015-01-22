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
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionClearFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.runtime.transaction.facetdecorator.TransactionFacetDecoratorAbstract;
import org.apache.isis.core.runtime.transaction.facets.ActionInvocationFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.CollectionAddToFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.CollectionClearFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.CollectionRemoveFromFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.PropertyClearFacetWrapTransaction;
import org.apache.isis.core.runtime.transaction.facets.PropertySetterFacetWrapTransaction;

public class StandardTransactionFacetDecorator extends TransactionFacetDecoratorAbstract {

    public StandardTransactionFacetDecorator(final IsisConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Facet decorate(final Facet facet, final FacetHolder requiredHolder) {
        final Class<? extends Facet> facetType = facet.facetType();
        if (facetType == ActionInvocationFacet.class) {
            final ActionInvocationFacet decoratedFacet = (ActionInvocationFacet) facet;
            final Facet decoratingFacet = new ActionInvocationFacetWrapTransaction(decoratedFacet);
            requiredHolder.addFacet(decoratingFacet);
            return decoratingFacet;
        }

        if (facetType == CollectionAddToFacet.class) {
            final CollectionAddToFacet decoratedFacet = (CollectionAddToFacet) facet;
            final Facet decoratingFacet = new CollectionAddToFacetWrapTransaction(decoratedFacet);
            requiredHolder.addFacet(decoratingFacet);
            return decoratingFacet;
        }

        if (facetType == CollectionClearFacet.class) {
            final CollectionClearFacet decoratedFacet = (CollectionClearFacet) facet;
            final Facet decoratingFacet = new CollectionClearFacetWrapTransaction(decoratedFacet);
            requiredHolder.addFacet(decoratingFacet);
            return decoratingFacet;
        }

        if (facetType == CollectionRemoveFromFacet.class) {
            final CollectionRemoveFromFacet decoratedFacet = (CollectionRemoveFromFacet) facet;
            final Facet decoratingFacet = new CollectionRemoveFromFacetWrapTransaction(decoratedFacet);
            requiredHolder.addFacet(decoratingFacet);
            return decoratingFacet;
        }

        if (facetType == PropertyClearFacet.class) {
            final PropertyClearFacet decoratedFacet = (PropertyClearFacet) facet;
            final Facet decoratingFacet = new PropertyClearFacetWrapTransaction(decoratedFacet);
            requiredHolder.addFacet(decoratingFacet);
            return decoratingFacet;
        }

        if (facetType == PropertySetterFacet.class) {
            final PropertySetterFacet decoratedFacet = (PropertySetterFacet) facet;
            final Facet decoratingFacet = new PropertySetterFacetWrapTransaction(decoratedFacet);
            requiredHolder.addFacet(decoratingFacet);
            return decoratingFacet;
        }

        return facet;
    }

}
