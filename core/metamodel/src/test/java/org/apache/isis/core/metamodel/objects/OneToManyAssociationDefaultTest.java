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
package org.apache.isis.core.metamodel.objects;

import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.MetaModelTestAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.id.TypeIdentifierTestFactory;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationDefault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;

class OneToManyAssociationDefaultTest
extends MetaModelTestAbstract {

    private static final String COLLECTION_ID = "orders";

    public static class Order {
    }

    private static final Class<?> COLLECTION_TYPE = Order.class;

    private OneToManyAssociation association;

    @Override
    protected void afterSetUp() {

        val mockHasStaticText = mock(HasStaticText.class);
        when(mockHasStaticText.translated()).thenReturn("My name");

        val mockNamedFacet = mock(MemberNamedFacet.class);
        when(mockNamedFacet.getSpecialization()).thenReturn(_Either.left(mockHasStaticText));

        val mockPeer = mock(FacetedMethod.class);
        doReturn(COLLECTION_TYPE).when(mockPeer).getType();
        when(mockPeer.getMetaModelContext()).thenReturn(getMetaModelContext());
        when(mockPeer.getFeatureIdentifier()).thenReturn(
                Identifier
                .propertyOrCollectionIdentifier(
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
