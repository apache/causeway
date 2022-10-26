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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.val;

@ExtendWith(MockitoExtension.class)
class TitleFacetViaTitleAnnotationTest {

    @Mock FacetHolder mockFacetHolder;
    @Mock ManagedObject mockManagedObject;

    protected MetaModelContext metaModelContext;

    protected static class DomainObjectWithProblemInItsAnnotatedTitleMethod {

        @Title
        public String brokenTitle() {
            throw new NullPointerException();
        }

    }

    protected static class NormalDomainObject {

        @Title(sequence = "1.0")
        public String titleElement1() {
            return "Normal";
        }

        @Title(sequence = "2.0")
        public String titleElement2() {
            return "Domain";
        }

        @Title(sequence = "3.0")
        public String titleElement3() {
            return "Object";
        }

    }

    @BeforeEach
    public void setUp() {
        metaModelContext = MetaModelContext_forTesting.builder()
                .build();
    }

    @Test
    public void testTitle() throws Exception {

        final TitleFacetViaTitleAnnotation facet =
                (TitleFacetViaTitleAnnotation) TitleFacetViaTitleAnnotation
                .create(NormalDomainObject.class, mockFacetHolder)
                .orElse(null);

        final NormalDomainObject normalPojo = new NormalDomainObject();

        Mockito.when(mockFacetHolder.getMetaModelContext()).thenReturn(metaModelContext);

        val managedObject =
                metaModelContext.getObjectManager().adapt(normalPojo);

        final String title = facet.title(managedObject);
        assertThat(title, is("Normal Domain Object"));
    }

    @Test
    public void titleThrowsException() {

        final TitleFacetViaTitleAnnotation facet =
                (TitleFacetViaTitleAnnotation) TitleFacetViaTitleAnnotation
                .create(DomainObjectWithProblemInItsAnnotatedTitleMethod.class, mockFacetHolder)
                .orElse(null);

        final DomainObjectWithProblemInItsAnnotatedTitleMethod screwedPojo =
                new DomainObjectWithProblemInItsAnnotatedTitleMethod();

        Mockito.when(mockFacetHolder.getMetaModelContext()).thenReturn(metaModelContext);
        Mockito.when(mockManagedObject.getPojo()).thenReturn(screwedPojo);

        final String title = facet.title(mockManagedObject);
        assertThat(title, is("Failed Title"));
    }

}
