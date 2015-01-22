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
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.param.validating.mustsatisfyspec.MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.validating.mustsatisfyspec.MustSatisfySpecificationOnParameterFacetFactory;
import org.apache.isis.core.unittestsupport.jmocking.JavassistImposteriser;

import static org.apache.isis.core.commons.matchers.IsisMatchers.anInstanceOf;

@RunWith(JMock.class)
public class MustSatisfySpecificationFacetFactoryProcessParameterTest {

    private final Mockery mockery = new JUnit4Mockery() {
        {
            setImposteriser(JavassistImposteriser.INSTANCE);
        }
    };

    private FacetedMethodParameter mockFacetedMethodParameter;

    private Class<DomainObjectWithoutMustSatisfyAnnotations> domainObjectClassWithoutAnnotation;
    private Class<DomainObjectWithMustSatisfyAnnotations> domainObjectClassWithAnnotation;
    private Method changeLastNameMethodWithout;
    private Method changeLastNameMethodWith;

    @Before
    public void setUp() throws Exception {
        mockFacetedMethodParameter = mockery.mock(FacetedMethodParameter.class);
        domainObjectClassWithoutAnnotation = DomainObjectWithoutMustSatisfyAnnotations.class;
        domainObjectClassWithAnnotation = DomainObjectWithMustSatisfyAnnotations.class;
        changeLastNameMethodWithout = domainObjectClassWithoutAnnotation.getMethod("changeLastName", String.class);
        changeLastNameMethodWith = domainObjectClassWithAnnotation.getMethod("changeLastName", String.class);
    }

    @Test
    public void addsAMustSatisfySpecificationFacetIfAnnotated() {
        final MustSatisfySpecificationOnParameterFacetFactory facetFactory = new MustSatisfySpecificationOnParameterFacetFactory();

        mockery.checking(new Expectations() {
            {
                one(mockFacetedMethodParameter).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter.class)));
            }
        });
        facetFactory.processParams(new ProcessParameterContext(changeLastNameMethodWith, 0, mockFacetedMethodParameter));
    }

    @Test
    public void doesNotAddsAMustSatisfySpecificationFacetIfNotAnnotated() {
        final MustSatisfySpecificationOnParameterFacetFactory facetFactory = new MustSatisfySpecificationOnParameterFacetFactory();

        mockery.checking(new Expectations() {
            {
                never(mockFacetedMethodParameter).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter.class)));
            }
        });
        facetFactory.processParams(new ProcessParameterContext(changeLastNameMethodWithout, 0, mockFacetedMethodParameter));
    }

}
