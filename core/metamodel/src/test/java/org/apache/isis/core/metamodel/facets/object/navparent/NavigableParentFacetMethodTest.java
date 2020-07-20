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

package org.apache.isis.core.metamodel.facets.object.navparent;

import java.lang.reflect.Method;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.navparent.method.NavigableParentFacetMethod;

public class NavigableParentFacetMethodTest {

    private final Mockery mockery = new JUnit4Mockery();

    private NavigableParentFacetMethod facet;
    private FacetHolder mockFacetHolder;

    private ObjectAdapter mockOwningAdapter;

    private DomainObjectWithProblemInNavigableParentMethod pojo;

    public static class DomainObjectWithProblemInNavigableParentMethod {
        public String parent() {
            throw new NullPointerException();
        }
    }

    @Before
    public void setUp() throws Exception {

        pojo = new DomainObjectWithProblemInNavigableParentMethod();
        mockFacetHolder = mockery.mock(FacetHolder.class);
        mockOwningAdapter = mockery.mock(ObjectAdapter.class);
        final Method navigableParentMethod = DomainObjectWithProblemInNavigableParentMethod.class.getMethod("parent");
        facet = new NavigableParentFacetMethod(navigableParentMethod, mockFacetHolder);

        mockery.checking(new Expectations() {
            {
                allowing(mockOwningAdapter).getPojo();
                will(returnValue(pojo));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        facet = null;
    }

    @Test
    public void testNavigableParentThrowsException() {
        final Object parent = facet.navigableParent(mockOwningAdapter.getPojo());
        assertThat(parent, is(nullValue()));
    }

}
