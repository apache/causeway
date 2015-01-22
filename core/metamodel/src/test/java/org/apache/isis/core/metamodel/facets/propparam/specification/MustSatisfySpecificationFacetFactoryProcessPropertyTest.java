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

import java.lang.reflect.Method;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.properties.validating.mustsatisfyspec.MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.validating.mustsatisfyspec.MustSatisfySpecificationOnPropertyFacetFactory;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.apache.isis.core.commons.matchers.IsisMatchers.anInstanceOf;

public class MustSatisfySpecificationFacetFactoryProcessPropertyTest {

    @Rule
    public JUnitRuleMockery2 mockery = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private MethodRemover mockMethodRemover;
    private FacetedMethod mockFacetHolder;

    private Class<DomainObjectWithoutMustSatisfyAnnotations> domainObjectClassWithoutAnnotation;
    private Class<DomainObjectWithMustSatisfyAnnotations> domainObjectClassWithAnnotation;
    private Method firstNameMethodWithout;
    private Method firstNameMethodWith;

    @Before
    public void setUp() throws Exception {
        mockMethodRemover = mockery.mock(MethodRemover.class);
        mockFacetHolder = mockery.mock(FacetedMethod.class);
        domainObjectClassWithoutAnnotation = DomainObjectWithoutMustSatisfyAnnotations.class;
        domainObjectClassWithAnnotation = DomainObjectWithMustSatisfyAnnotations.class;
        firstNameMethodWithout = domainObjectClassWithoutAnnotation.getMethod("getFirstName");
        firstNameMethodWith = domainObjectClassWithAnnotation.getMethod("getFirstName");
    }

    @After
    public void tearDown() throws Exception {
        mockMethodRemover = null;
        mockFacetHolder = null;
    }

    @Test
    public void addsAMustSatisfySpecificationFacetIfAnnotated() {
        final MustSatisfySpecificationOnPropertyFacetFactory facetFactory = new MustSatisfySpecificationOnPropertyFacetFactory();

        mockery.checking(new Expectations() {
            {
                one(mockFacetHolder).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty.class)));
            }
        });
        facetFactory.process(new ProcessMethodContext(domainObjectClassWithAnnotation.getClass(), null, null, firstNameMethodWith, mockMethodRemover, mockFacetHolder));
    }

    @Test
    public void doesNotAddsAMustSatisfySpecificationFacetIfNotAnnotated() {
        final MustSatisfySpecificationOnPropertyFacetFactory facetFactory = new MustSatisfySpecificationOnPropertyFacetFactory();

        mockery.checking(new Expectations() {
            {
                never(mockFacetHolder).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty.class)));
            }
        });
        facetFactory.process(new ProcessMethodContext(domainObjectClassWithAnnotation.getClass(), null, null, firstNameMethodWithout, mockMethodRemover, mockFacetHolder));
    }

}
