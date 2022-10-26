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
package org.apache.causeway.core.metamodel.facets.object.ident.title;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryJupiterTestCase;
import org.apache.causeway.core.metamodel.facets.object.title.methods.TitleFacetViaTitleMethod;
import org.apache.causeway.core.metamodel.object.ManagedObject;

class TitleFacetViaMethodTest
extends AbstractFacetFactoryJupiterTestCase {

    private TitleFacetViaTitleMethod facet;

    private ManagedObject stubAdapter;
    private DomainObjectWithProblemInItsTitleMethod pojo;
    private MetaModelContext metaModelContext;

    public static class DomainObjectWithProblemInItsTitleMethod {
        public String title() {
            throw new NullPointerException("for testing purposes");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .build();

        mockFacetHolder = Mockito.mock(FacetHolder.class);
        Mockito.when(mockFacetHolder.getMetaModelContext()).thenReturn(metaModelContext);

        pojo = new DomainObjectWithProblemInItsTitleMethod();
        //mockFacetHolder = mockery.mock(FacetHolder.class);
        //mockOwningAdapter = mockery.mock(ManagedObject.class);
        final Method iconNameMethod = DomainObjectWithProblemInItsTitleMethod.class.getMethod("title");
        facet = (TitleFacetViaTitleMethod) TitleFacetViaTitleMethod
                .create(iconNameMethod, mockFacetHolder)
                .orElse(null);

        stubAdapter = metaModelContext.getObjectManager().adapt(pojo);
    }

    @Test
    public void testTitleThrowsException() {
        final String title = facet.title(stubAdapter);
        assertThat(title, is("Failed Title"));
    }

}
