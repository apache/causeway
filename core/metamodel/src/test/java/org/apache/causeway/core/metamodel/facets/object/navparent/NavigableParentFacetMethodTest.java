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
package org.apache.causeway.core.metamodel.facets.object.navparent;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.navparent.method.NavigableParentFacetViaMethod;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.val;

class NavigableParentFacetMethodTest {

    private NavigableParentFacet facet;
    private FacetHolder simpleFacetHolder;
    private ManagedObject mockOwningAdapter;

    private DomainObjectWithProblemInNavigableParentMethod pojo;

    public static class DomainObjectWithProblemInNavigableParentMethod {
        public Object parent() {
            throw new NullPointerException();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {

        val mmc = MetaModelContext_forTesting.buildDefault();
        simpleFacetHolder = FacetHolder.forTesting(mmc);

        pojo = new DomainObjectWithProblemInNavigableParentMethod();

        mockOwningAdapter = Mockito.mock(ManagedObject.class);
        final Method navigableParentMethod = DomainObjectWithProblemInNavigableParentMethod.class.getMethod("parent");
        facet = NavigableParentFacetViaMethod.create(pojo.getClass(), navigableParentMethod, simpleFacetHolder)
                .orElse(null);

        Mockito.when(mockOwningAdapter.getPojo()).thenReturn(pojo);
    }

    @AfterEach
    public void tearDown() throws Exception {
        facet = null;
    }

    @Test
    public void testNavigableParentThrowsException() {
        final Object parent = facet.navigableParent(mockOwningAdapter.getPojo());
        assertThat(parent, is(nullValue()));
    }

}
