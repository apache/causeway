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
package org.apache.causeway.core.metamodel.facets.object.ident.cssclass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.Mocking;
import org.apache.causeway.core.metamodel.facets.object.cssclass.method.CssClassFacetViaCssClassMethod;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

class CssClassFacetMethodWithProblemTest {

    private Mocking mocking = new Mocking();
    private CssClassFacetViaCssClassMethod facet;
    private ManagedObject mockOwningAdapter;

    private DomainObjectWithProblemInCssClassMethod pojo;

    static class DomainObjectWithProblemInCssClassMethod {
        public String cssClass() {
            throw new NullPointerException("for testing purposes");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {

        pojo = new DomainObjectWithProblemInCssClassMethod();

        final ResolvedMethod iconNameMethod = _GenericResolver.testing
                .resolveMethod(DomainObjectWithProblemInCssClassMethod.class, "cssClass");
        facet = (CssClassFacetViaCssClassMethod) CssClassFacetViaCssClassMethod
                .create(iconNameMethod, Mockito.mock(FacetHolder.class))
                .orElse(null);

        var mockSpec = Mockito.mock(ObjectSpecification.class);
        Mockito.when(mockSpec.isViewModel()).thenReturn(true);

        mockOwningAdapter = mocking.asViewmodel(pojo);

    }

    @AfterEach
    public void tearDown() {
        facet = null;
    }

    @Test
    void cssClassThrowsException() {
        //assertThrows(Exception.class, ()->facet.cssClass(mockOwningAdapter));
        final String iconName = facet.cssClass(mockOwningAdapter);
        assertThat(iconName, is(nullValue()));
    }

}
