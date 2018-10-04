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

public class MustSatisfySpecificationValidatingInteractionTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet facetForSpecificationAlwaysSatisfied;
    private MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet facetForSpecificationNeverSatisfied;

    private IdentifiedHolder identifiedHolder;

    @Mock
    private ServicesInjector mockServicesInjector;

    @Mock
    private TranslationService mockTranslationService;

    @Mock
    private PropertyModifyContext mockContext;

    private ObjectAdapter mockProposedObjectAdapter;
    private Object mockProposedObject;

    private SpecificationAlwaysSatisfied specificationAlwaysSatisfied;
    private SpecificationNeverSatisfied specificationNeverSatisfied;

    public static class Customer {}

    @Before
    public void setUp() throws Exception {
        identifiedHolder = new AbstractFacetFactoryTest.IdentifiedHolderImpl(Identifier.propertyOrCollectionIdentifier(Customer.class, "lastName"));
        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(TranslationService.class);
            will(returnValue(Optional.of(mockTranslationService)));
        }});

        specificationAlwaysSatisfied = new SpecificationAlwaysSatisfied();
        specificationNeverSatisfied = new SpecificationNeverSatisfied();

        facetForSpecificationAlwaysSatisfied = 
        		new MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet(
        				Collections.singletonList(specificationAlwaysSatisfied), 
        				identifiedHolder, 
        				mockServicesInjector);
        
        facetForSpecificationNeverSatisfied = 
        		new MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet(
        				Collections.singletonList(specificationNeverSatisfied), 
        				identifiedHolder, 
        				mockServicesInjector);

        mockProposedObjectAdapter = context.mock(ObjectAdapter.class, "proposed");
        mockProposedObject = context.mock(Object.class, "proposedObject");

        context.checking(new Expectations() {
            {
            	oneOf(mockContext).getProposed();
                will(returnValue(mockProposedObjectAdapter));

                oneOf(mockProposedObjectAdapter).getPojo();
                will(returnValue(mockProposedObject));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        identifiedHolder = null;
        facetForSpecificationAlwaysSatisfied = null;
        facetForSpecificationNeverSatisfied = null;
        mockContext = null;
    }

    @Test
    public void validatesWhenSpecificationDoesNotVeto() {
        final String reason = facetForSpecificationAlwaysSatisfied.invalidates(mockContext);
        assertThat(reason, is(nullValue()));
    }

    @Test
    public void invalidatesWhenSpecificationVetoes() {
        final String reason = facetForSpecificationNeverSatisfied.invalidates(mockContext);
        assertThat(reason, is(not(nullValue())));
        assertThat(reason, is("not satisfied"));
    }

}
