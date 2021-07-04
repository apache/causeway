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

package org.apache.isis.core.metamodel.facets.collections;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.javautilcollection.JavaCollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;

class JavaCollectionFacetTest {

    private MetaModelContext metaModelContext;

    @BeforeEach
    void setUp() throws Exception {
        metaModelContext = MetaModelContext_forTesting.buildDefault();
    }

    @Test
    void firstElementForEmptyCollectionIsEmptyOptional() {

        val mockFacetHolder = mock(FacetHolder.class);
        when(mockFacetHolder.getMetaModelContext()).thenReturn(metaModelContext);

        val mockCollection = mock(ManagedObject.class);
        when(mockCollection.getPojo()).thenReturn(new ArrayList<Object>());

        val facet = new JavaCollectionFacet(mockFacetHolder);
        assertThat(facet.firstElement(mockCollection), is(Optional.empty()));
    }

}
