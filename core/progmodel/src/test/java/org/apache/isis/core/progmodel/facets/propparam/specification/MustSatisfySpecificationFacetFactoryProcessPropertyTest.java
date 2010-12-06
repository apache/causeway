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

import static org.apache.isis.core.commons.matchers.IsisMatchers.anInstanceOf;

import java.lang.reflect.Method;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.progmodel.facets.propparam.specification.MustSatisfySpecificationFacet;
import org.apache.isis.core.progmodel.facets.propparam.specification.MustSatisfySpecificationFacetFactory;

@RunWith(JMock.class)
public class MustSatisfySpecificationFacetFactoryProcessPropertyTest {

    private Mockery mockery = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    
    private MustSatisfySpecificationFacetFactory facetFactory;
    private MethodRemover mockMethodRemover;
    private FacetHolder mockFacetHolder;

    private Class<DomainObjectWithoutMustSatisfyAnnotations> domainObjectClassWithoutAnnotation;
    private Class<DomainObjectWithMustSatisfyAnnotations> domainObjectClassWithAnnotation;
    private Method firstNameMethodWithout;
    private Method firstNameMethodWith;

    @Before
    public void setUp() throws Exception {
        facetFactory = new MustSatisfySpecificationFacetFactory();
        mockMethodRemover = mockery.mock(MethodRemover.class);
        mockFacetHolder = mockery.mock(FacetHolder.class);
        domainObjectClassWithoutAnnotation = DomainObjectWithoutMustSatisfyAnnotations.class;
        domainObjectClassWithAnnotation = DomainObjectWithMustSatisfyAnnotations.class;
        firstNameMethodWithout = domainObjectClassWithoutAnnotation.getMethod("getFirstName");
        firstNameMethodWith = domainObjectClassWithAnnotation.getMethod("getFirstName");
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
        mockMethodRemover = null;
        mockFacetHolder = null;
    }

    @Test
    public void addsAMustSatisfySpecificationFacetIfAnnotated() {
        mockery.checking(new Expectations() {{
            one(mockFacetHolder).addFacet(with(anInstanceOf(MustSatisfySpecificationFacet.class)));
        }});
        facetFactory.process(domainObjectClassWithAnnotation.getClass(), firstNameMethodWith, mockMethodRemover, mockFacetHolder);
    }

    @Test
    public void doesNotAddsAMustSatisfySpecificationFacetIfNotAnnotated() {
        mockery.checking(new Expectations() {{
            never(mockFacetHolder).addFacet(with(anInstanceOf(MustSatisfySpecificationFacet.class)));
        }});
        facetFactory.process(domainObjectClassWithAnnotation.getClass(), firstNameMethodWithout, mockMethodRemover, mockFacetHolder);
    }

}
