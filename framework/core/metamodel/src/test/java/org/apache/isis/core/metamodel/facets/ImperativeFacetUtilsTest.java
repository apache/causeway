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

package org.apache.isis.core.metamodel.facets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import com.google.common.collect.Lists;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.ImperativeFacetUtils.ImperativeFacetFlags;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

@RunWith(JMock.class)
public class ImperativeFacetUtilsTest {

    public static class Customer {

        private String firstName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }

    }

    private final Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private ObjectMember mockObjectMember;
    private Method method;

    @Before
    public void setUp() throws Exception {
        mockObjectMember = context.mock(ObjectMember.class);
        method = Customer.class.getDeclaredMethod("getFirstName");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getImperativeFacetsWhenHasNone() throws Exception {
        context.checking(new Expectations() {
            {
                one(mockObjectMember).getFacets(with(any(Filter.class)));
                will(returnValue(Lists.newArrayList()));
            }
        });
        final ImperativeFacetFlags flags = ImperativeFacetUtils.getImperativeFacetFlags(mockObjectMember, method);
        assertThat(flags, is(not(nullValue())));
        assertThat(flags.impliesResolve(), is(false));
        assertThat(flags.impliesObjectChanged(), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getImperativeFacetsWhenHasOneImperativeFacet() throws Exception {
        final ImperativeFacet imperativeFacet = null;
        context.checking(new Expectations() {
            {
                one(mockObjectMember).getFacets(with(any(Filter.class)));
                will(returnValue(Lists.newArrayList((Facet) imperativeFacet)));
            }
        });
        final ImperativeFacetFlags flags = ImperativeFacetUtils.getImperativeFacetFlags(mockObjectMember, method);
        assertThat(flags, is(not(nullValue())));
        // TODO: need more tests here, these don't go deep enough...
    }

}
