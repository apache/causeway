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
package org.apache.causeway.core.metamodel.facets.object.ident.layout;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutPrefixFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

class LayoutFacetMethodTest {

    private LayoutPrefixFacet facet;
    private ManagedObject mockOwningAdapter;

    private DomainObjectWithProblemInLayoutMethod pojo;

    public static class DomainObjectWithProblemInLayoutMethod {
        public String layout() {
            throw new NullPointerException("for testing purposes");
        }
    }

    @BeforeEach
    void setUp() throws Exception {

        pojo = new DomainObjectWithProblemInLayoutMethod();

        var iconNameMethod = _GenericResolver.testing
                .resolveMethod(DomainObjectWithProblemInLayoutMethod.class, "layout");
        facet = LayoutPrefixFacet.forLayoutMethod(
                        iconNameMethod, Mockito.mock(FacetHolder.class))
                    .orElse(null);

        mockOwningAdapter = Mockito.mock(ManagedObject.class);
        Mockito.when(mockOwningAdapter.getPojo()).thenReturn(pojo);
    }

    @AfterEach
    void tearDown() throws Exception {
        facet = null;
    }

    @Test
    void when_layout_throws_exception() {
        //assertThrows(NullPointerException.class, ()->facet.layout(mockOwningAdapter));
        final String layout = facet.layout(mockOwningAdapter);
        assertThat(layout, is(nullValue()));
    }

}
