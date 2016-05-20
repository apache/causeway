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

package org.apache.isis.core.metamodel.facets.properties.property;

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
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.propparam.specification.DomainObjectWithMustSatisfyAnnotations;
import org.apache.isis.core.metamodel.facets.propparam.specification.DomainObjectWithoutMustSatisfyAnnotations;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.apache.isis.core.commons.matchers.IsisMatchers.anInstanceOf;

public class MustSatisfySpecificationFacetFactoryProcessPropertyTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private MethodRemover mockMethodRemover;
    @Mock
    private FacetedMethod mockFacetedMethod;

    @Mock
    private ServicesInjector mockServicesInjector;
    @Mock
    private IsisConfigurationDefault mockConfiguration;

    @Mock
    private TranslationService mockTranslationService;

    private Class<DomainObjectWithoutMustSatisfyAnnotations> domainObjectClassWithoutAnnotation;
    private Class<DomainObjectWithMustSatisfyAnnotations> domainObjectClassWithAnnotation;
    private Method firstNameMethodWithout;
    private Method firstNameMethodWith;
    private PropertyAnnotationFacetFactory facetFactory;

    public static class Customer {}

    @Before
    public void setUp() throws Exception {

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(TranslationService.class);
            will(returnValue(mockTranslationService));

            allowing(mockServicesInjector).getConfigurationServiceInternal();
            will(returnValue(mockConfiguration));

            allowing(mockServicesInjector).injectServicesInto(with(any(List.class)));

            allowing(mockFacetedMethod).getIdentifier();
            will(returnValue(Identifier.actionIdentifier(Customer.class, "foo")));

        }});

        domainObjectClassWithoutAnnotation = DomainObjectWithoutMustSatisfyAnnotations.class;
        domainObjectClassWithAnnotation = DomainObjectWithMustSatisfyAnnotations.class;
        firstNameMethodWithout = domainObjectClassWithoutAnnotation.getMethod("getFirstName");
        firstNameMethodWith = domainObjectClassWithAnnotation.getMethod("getFirstName");

        facetFactory = new PropertyAnnotationFacetFactory();
        facetFactory.setServicesInjector(mockServicesInjector);
    }

    @Test
    public void addsAMustSatisfySpecificationFacetIfAnnotated() {

        context.checking(new Expectations() {
            {
                oneOf(mockFacetedMethod).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty.class)));
            }
        });
        facetFactory.processMustSatisfy(new ProcessMethodContext(domainObjectClassWithAnnotation.getClass(), null, null, firstNameMethodWith, mockMethodRemover, mockFacetedMethod));
    }

    @Test
    public void doesNotAddsAMustSatisfySpecificationFacetIfNotAnnotated() {

        context.checking(new Expectations() {
            {
                never(mockFacetedMethod).addFacet(with(anInstanceOf(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty.class)));
            }
        });
        facetFactory.processMustSatisfy(new ProcessMethodContext(domainObjectClassWithAnnotation.getClass(), null, null, firstNameMethodWithout, mockMethodRemover, mockFacetedMethod));
    }

}
