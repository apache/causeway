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

package org.apache.isis.core.metamodel.facets.propparam.specification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Optional;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.object.validating.mustsatisfyspec.MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class MustSatisfySpecificationValidatingInteractionMoreTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet facetForSpecificationFirstLetterUpperCase;

    @Mock
    private IdentifiedHolder identifiedHolder;

    @Mock
    private ServicesInjector mockServicesInjector;

    @Mock
    private TranslationService mockTranslationService;

    @Mock
    private PropertyModifyContext mockContext;

    private ObjectAdapter mockProposedObjectAdapter;

    private SpecificationRequiresFirstLetterToBeUpperCase requiresFirstLetterToBeUpperCase;

    public static class Customer {}
    
    @Before
    public void setUp() throws Exception {

        identifiedHolder = new AbstractFacetFactoryTest.IdentifiedHolderImpl(Identifier.propertyOrCollectionIdentifier(Customer.class, "lastName"));

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(TranslationService.class);
            will(returnValue(Optional.of(mockTranslationService)));
        }});

        requiresFirstLetterToBeUpperCase = new SpecificationRequiresFirstLetterToBeUpperCase();

        facetForSpecificationFirstLetterUpperCase = 
        		new MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet(
        				Collections.singletonList(requiresFirstLetterToBeUpperCase), 
        				identifiedHolder, 
        				mockServicesInjector);

        mockProposedObjectAdapter = context.mock(ObjectAdapter.class, "proposed");
    }

    @After
    public void tearDown() throws Exception {
        identifiedHolder = null;
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
        context.checking(new Expectations() {
            {
            	oneOf(mockContext).getProposed();
                will(returnValue(mockProposedObjectAdapter));

                oneOf(mockProposedObjectAdapter).getPojo();
                will(returnValue("This starts with an upper case letter and so is okay"));
            }
        });

        final String reason = facetForSpecificationFirstLetterUpperCase.invalidates(mockContext);
        assertThat(reason, is(nullValue()));
    }

    @Test
    public void invalidatesUsingSpecificationIfProposedNotOkay() {
        context.checking(new Expectations() {
            {
            	oneOf(mockContext).getProposed();
                will(returnValue(mockProposedObjectAdapter));

                oneOf(mockProposedObjectAdapter).getPojo();
                will(returnValue("this starts with an lower case letter and so is not okay"));
            }
        });

        final String reason = facetForSpecificationFirstLetterUpperCase.invalidates(mockContext);
        assertThat(reason, is(not(nullValue())));
        assertThat(reason, is("Must start with upper case"));
    }

}
