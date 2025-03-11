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
package org.apache.causeway.core.metamodel.facets.object.ident.icon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.Mocking;
import org.apache.causeway.core.metamodel.facets.object.icon.method.IconFacetViaIconNameMethod;
import org.apache.causeway.core.metamodel.object.ManagedObject;

class IconFacetMethodTest {

    private Mocking mocking = new Mocking();
    private IconFacetViaIconNameMethod facet;
    private ManagedObject mockOwningAdapter;

    private DomainObjectWithProblemInIconNameMethod pojo;

    public static class DomainObjectWithProblemInIconNameMethod {
        public String iconName() {
            throw new NullPointerException("for testing purposes");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {

        pojo = new DomainObjectWithProblemInIconNameMethod();
        var iconNameMethod = _GenericResolver.testing
                .resolveMethod(DomainObjectWithProblemInIconNameMethod.class, "iconName");

        facet = (IconFacetViaIconNameMethod) IconFacetViaIconNameMethod
                .create(iconNameMethod, Mockito.mock(FacetHolder.class))
                .orElse(null);

        mockOwningAdapter = mocking.asViewmodel(pojo);
    }

    @AfterEach
    public void tearDown() throws Exception {
        facet = null;
    }

    @Test
    void iconNameThrowsException() {
        //assertThrows(Exception.class, ()->facet.iconName(mockOwningAdapter));
        final String iconName = facet.iconName(mockOwningAdapter);
        assertThat(iconName, is(nullValue()));
    }

}
