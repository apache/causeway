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
package org.apache.causeway.core.metamodel.facets.collections;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.Mocking;
import org.apache.causeway.core.metamodel.facets.collections.javautilcollection.JavaCollectionFacet;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;

class JavaCollectionFacetTest {

    private MetaModelContext metaModelContext;
    private Mocking mocking = new Mocking();

    @BeforeEach
    void setUp() throws Exception {
        metaModelContext = MetaModelContext_forTesting.buildDefault();
    }

    @Test
    void firstElementForEmptyCollectionIsEmptyOptional() {
        var mockFacetHolder = mock(FacetHolder.class);
        when(mockFacetHolder.getMetaModelContext()).thenReturn(metaModelContext);

        var mockCollection = mocking.asPacked(Can.empty());

        var facet = new JavaCollectionFacet(mockFacetHolder);
        assertThat(facet.firstElement(mockCollection), is(Optional.empty()));
    }

}
