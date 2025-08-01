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
package org.apache.causeway.core.metamodel.objects;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.metamodel.MetaModelTestAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetWithStaticTextAbstract;
import org.apache.causeway.core.metamodel.id.TypeIdentifierTestFactory;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.impl._JUnitSupport;
import org.apache.causeway.core.mmtestsupport.TranslationService_forTesting;

class ObjectActionLayoutXmlDefaultTest
extends MetaModelTestAbstract {

    private ObjectAction action;

    private FacetedMethod mockFacetedMethod;

    @Override
    protected void afterSetUp() {

        mockFacetedMethod = mock(FacetedMethod.class);

        when(mockFacetedMethod.getFeatureIdentifier())
        .thenReturn(Identifier.actionIdentifier(TypeIdentifierTestFactory.newCustomer(), "reduceheadcount"));

        when(mockFacetedMethod.getTranslationService())
        .thenReturn(new TranslationService_forTesting());

        action = _JUnitSupport.actionForMethod(mockFacetedMethod);
    }

    @Test
    void nameDefaultsToActionsMethodName() {
        final String name = "Reduceheadcount";

        doReturn(new MemberNamedFacetWithStaticTextAbstract(name, mockFacetedMethod) {})
        .when(mockFacetedMethod).getFacet(MemberNamedFacet.class);

        assertThat(action.getStaticFriendlyName().get(), is(equalTo(name)));
    }

    @Test
    void id() {
        assertEquals("reduceheadcount", action.getId());
    }

}
