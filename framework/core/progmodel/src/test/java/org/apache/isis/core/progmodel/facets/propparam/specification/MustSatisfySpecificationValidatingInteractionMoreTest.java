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

package org.apache.isis.core.progmodel.facets.propparam.specification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.progmodel.facets.object.validperspec.MustSatisfySpecificationOnTypeFacet;

@RunWith(JMock.class)
public class MustSatisfySpecificationValidatingInteractionMoreTest {

    private final Mockery mockery = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private MustSatisfySpecificationOnTypeFacet facetForSpecificationFirstLetterUpperCase;
    private FacetHolder mockHolder;

    private PropertyModifyContext mockContext;

    private ObjectAdapter mockProposedObjectAdapter;

    private SpecificationRequiresFirstLetterToBeUpperCase requiresFirstLetterToBeUpperCase;

    @Before
    public void setUp() throws Exception {
        mockHolder = mockery.mock(IdentifiedHolder.class);
        requiresFirstLetterToBeUpperCase = new SpecificationRequiresFirstLetterToBeUpperCase();

        facetForSpecificationFirstLetterUpperCase = new MustSatisfySpecificationOnTypeFacet(Utils.listOf(requiresFirstLetterToBeUpperCase), mockHolder);

        mockContext = mockery.mock(PropertyModifyContext.class);
        mockProposedObjectAdapter = mockery.mock(ObjectAdapter.class, "proposed");
    }

    @After
    public void tearDown() throws Exception {
        mockHolder = null;
        requiresFirstLetterToBeUpperCase = null;
        mockContext = null;
    }

    /**
     * As in:
     * 
     * <pre>
     * [at]ValidatedBy(SpecificationRequiresFirstLetterToBeUpperCase.class)
     * public void getLastName() { ... }
     * </pre>
     * 
     * @see SpecificationRequiresFirstLetterToBeUpperCase
     */
    @Test
    public void validatesUsingSpecificationIfProposedOkay() {
        mockery.checking(new Expectations() {
            {
                one(mockContext).getProposed();
                will(returnValue(mockProposedObjectAdapter));

                one(mockProposedObjectAdapter).getObject();
                will(returnValue("This starts with an upper case letter and so is okay"));
            }
        });

        final String reason = facetForSpecificationFirstLetterUpperCase.invalidates(mockContext);
        assertThat(reason, is(nullValue()));
    }

    @Test
    public void invalidatesUsingSpecificationIfProposedNotOkay() {
        mockery.checking(new Expectations() {
            {
                one(mockContext).getProposed();
                will(returnValue(mockProposedObjectAdapter));

                one(mockProposedObjectAdapter).getObject();
                will(returnValue("this starts with an lower case letter and so is not okay"));
            }
        });

        final String reason = facetForSpecificationFirstLetterUpperCase.invalidates(mockContext);
        assertThat(reason, is(not(nullValue())));
        assertThat(reason, is("Must start with upper case"));
    }

}
