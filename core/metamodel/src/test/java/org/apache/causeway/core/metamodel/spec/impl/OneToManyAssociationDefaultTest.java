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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.MetaModelTestAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.id.TypeIdentifierTestFactory;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

class OneToManyAssociationDefaultTest
extends MetaModelTestAbstract {

    private static final String COLLECTION_ID = "orders";

    public static class Order {
    }

    private static final Class<?> COLLECTION_TYPE = Order.class;
    private static final ResolvedType TOAC = ResolvedType.plural(
            COLLECTION_TYPE, List.class, CollectionSemantics.LIST);

    private OneToManyAssociation association;

    @Override
    protected void afterSetUp() {

        var mockHasStaticText = mock(HasStaticText.class);
        when(mockHasStaticText.translated()).thenReturn("My name");

        var mockNamedFacet = mock(MemberNamedFacet.class);
        when(mockNamedFacet.getSpecialization()).thenReturn(Either.left(mockHasStaticText));

        var mockPeer = mock(FacetedMethod.class);
        doReturn(TOAC).when(mockPeer).resolvedType();
        when(mockPeer.getMetaModelContext()).thenReturn(getMetaModelContext());
        when(mockPeer.getFeatureIdentifier()).thenReturn(
                Identifier
                .collectionIdentifier(
                        TypeIdentifierTestFactory.newCustomer(),
                        COLLECTION_ID));
        when(mockPeer.getFacet(MemberNamedFacet.class)).thenReturn(mockNamedFacet);

        association = OneToManyAssociationDefault.forMethod(mockPeer);
    }

    @Test
    void id() {
        assertThat(association.getId(), is(equalTo(COLLECTION_ID)));
    }

    @Test
    void name() {
        assertThat(association.getStaticFriendlyName().get(), is(equalTo("My name")));
    }

}
