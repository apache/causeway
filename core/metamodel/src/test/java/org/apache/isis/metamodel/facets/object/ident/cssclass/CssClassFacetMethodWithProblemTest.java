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

package org.apache.isis.metamodel.facets.object.ident.cssclass;

import java.lang.reflect.Method;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.cssclass.method.CssClassFacetMethod;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class CssClassFacetMethodWithProblemTest {

    private final Mockery mockery = new JUnit4Mockery();

    private CssClassFacetMethod facet;
    private FacetHolder mockFacetHolder;

    private ObjectAdapter mockOwningAdapter;

    private DomainObjectWithProblemInCssClassMethod pojo;

    public static class DomainObjectWithProblemInCssClassMethod {
        public String cssClass() {
            throw new NullPointerException();
        }
    }

    @Before
    public void setUp() throws Exception {

        pojo = new DomainObjectWithProblemInCssClassMethod();
        mockFacetHolder = mockery.mock(FacetHolder.class);
        mockOwningAdapter = mockery.mock(ObjectAdapter.class);
        final Method iconNameMethod = DomainObjectWithProblemInCssClassMethod.class.getMethod("cssClass");
        facet = new CssClassFacetMethod(iconNameMethod, mockFacetHolder);

        mockery.checking(new Expectations() {
            {
                allowing(mockOwningAdapter).getPojo();
                will(returnValue(pojo));
            }
        });
    }

    @After
    public void tearDown() {
        facet = null;
    }

    @Test
    public void testCssClassThrowsException() {
        final String iconName = facet.cssClass(mockOwningAdapter);
        assertThat(iconName, is(nullValue()));
    }

}
