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

package org.apache.isis.core.metamodel.facets.param.parameter;

import java.lang.reflect.Method;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.propparam.specification.DomainObjectWithMustSatisfyAnnotations;
import org.apache.isis.core.metamodel.facets.propparam.specification.DomainObjectWithoutMustSatisfyAnnotations;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.apache.isis.core.commons.matchers.IsisMatchers.anInstanceOf;

public class MustSatisfySpecificationFacetFactoryProcessParameterTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private FacetedMethodParameter mockFacetedMethodParameter;

    @Mock
    private ServicesInjector mockServicesInjector;

    @Mock
    private TranslationService mockTranslationService;

    private IsisConfigurationDefault stubConfiguration;


    private Class<DomainObjectWithoutMustSatisfyAnnotations> domainObjectClassWithoutAnnotation;
    private Class<DomainObjectWithMustSatisfyAnnotations> domainObjectClassWithAnnotation;
    private Method changeLastNameMethodWithout;
    private Method changeLastNameMethodWith;

    private ParameterAnnotationFacetFactory facetFactory;

    public static class Customer {}

    @Before
    public void setUp() throws Exception {

        stubConfiguration = new IsisConfigurationDefault();

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(TranslationService.class);
            will(returnValue(mockTranslationService));

            allowing(mockServicesInjector).getConfigurationServiceInternal();
            will(returnValue(stubConfiguration));

            allowing(mockServicesInjector).injectServicesInto(with(any(List.class)));
        }});


        domainObjectClassWithoutAnnotation = DomainObjectWithoutMustSatisfyAnnotations.class;
        domainObjectClassWithAnnotation = DomainObjectWithMustSatisfyAnnotations.class;
        changeLastNameMethodWithout = domainObjectClassWithoutAnnotation.getMethod("changeLastName", String.class);
        changeLastNameMethodWith = domainObjectClassWithAnnotation.getMethod("changeLastName", String.class);

        context.checking(new Expectations() {
            {
                allowing(mockFacetedMethodParameter).getIdentifier();
                will(returnValue(Identifier.actionIdentifier(Customer.class, "foo")));
            }
        });

        facetFactory = new ParameterAnnotationFacetFactory();
        facetFactory.setServicesInjector(mockServicesInjector);
    }

    @Test
    public void addsAMustSatisfySpecificationFacetIfAnnotated() {

        context.checking(new Expectations() {
            {
                oneOf(mockFacetedMethodParameter).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter.class)));
            }
        });
        facetFactory.processParamsMustSatisfy(new ProcessParameterContext(Customer.class, changeLastNameMethodWith, 0, null, mockFacetedMethodParameter));
    }

    @Test
    public void doesNotAddsAMustSatisfySpecificationFacetIfNotAnnotated() {

        context.checking(new Expectations() {
            {
                never(mockFacetedMethodParameter).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter.class)));
            }
        });
        facetFactory.processParamsMustSatisfy(new ProcessParameterContext(Customer.class, changeLastNameMethodWithout, 0, null, mockFacetedMethodParameter));
    }

}
