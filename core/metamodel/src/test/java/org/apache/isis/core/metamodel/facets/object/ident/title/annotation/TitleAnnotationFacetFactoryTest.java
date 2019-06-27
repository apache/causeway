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
package org.apache.isis.core.metamodel.facets.object.ident.title.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation.TitleComponent;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.AuthenticationSessionProvider;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TitleAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    private TitleAnnotationFacetFactory facetFactory;

    @Mock
    private ObjectAdapter mockObjectAdapter;
    @Mock
    private AuthenticationSession mockAuthenticationSession;

    @Before
    public void setUp() throws Exception {
        
        // PRODUCTION

        context.allowing(mockSpecificationLoader);

        facetFactory = new TitleAnnotationFacetFactory();

        context.checking(new Expectations() {
            {
                allowing(mockServiceRegistry).lookupService(AuthenticationSessionProvider.class);
                will(returnValue(Optional.of(mockAuthenticationSessionProvider)));

                allowing(mockAuthenticationSessionProvider).getAuthenticationSession();
                will(returnValue(mockAuthenticationSession));

//                allowing(mockServicesInjector).getSpecificationLoader();
//                will(returnValue(mockSpecificationLoader));
//
//                allowing(mockServicesInjector).getPersistenceSessionServiceInternal();
//                will(returnValue(mockPersistenceSessionServiceInternal));
            }
        });

    }

    @After
    @Override
    public void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public static class Customer {

        @Title
        public String someTitle() {
            return "Some Title";
        }
    }

    @Test
    public void testTitleAnnotatedMethodPickedUpOnClassRemoved() throws Exception {
        facetFactory.process(new ProcessClassContext(Customer.class, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertTrue(facet instanceof TitleFacetViaTitleAnnotation);
        final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation = (TitleFacetViaTitleAnnotation) facet;

        final List<Method> titleMethods = Arrays.asList(Customer.class.getMethod("someTitle"));
        for (int i = 0; i < titleMethods.size(); i++) {
            final Annotations.MethodEvaluator<Title> titleEvaluator = (Annotations.MethodEvaluator<Title>) titleFacetViaTitleAnnotation.getComponents().get(i)
                    .getTitleEvaluator();

            Assert.assertEquals(titleMethods.get(i),
                    titleEvaluator.getMethod());
        }
    }

    public static class Customer2 {

        @Title(sequence = "1", append = ".")
        public String titleElement1() {
            return "titleElement1";
        }

        @Title(sequence = "2", prepend = ",")
        public String titleElement2() {
            return "titleElement2";
        }

        @Title(sequence = "1.5")
        public String titleElement3() {
            return "titleElement3";
        }

    }

    @Ignore // to re-instate
    @Test
    public void testTitleAnnotatedMethodsPickedUpOnClass() throws Exception {

        facetFactory.process(new ProcessClassContext(Customer2.class, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertTrue(facet instanceof TitleFacetViaTitleAnnotation);
        final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation = (TitleFacetViaTitleAnnotation) facet;

        final List<Method> titleMethods = Arrays.asList(Customer2.class.getMethod("titleElement1"), Customer2.class.getMethod("titleElement3"), Customer2.class.getMethod("titleElement2"));

        final List<TitleComponent> components = titleFacetViaTitleAnnotation.getComponents();
        for (int i = 0; i < titleMethods.size(); i++) {
            final Annotations.MethodEvaluator<Title> titleEvaluator = (Annotations.MethodEvaluator<Title>) titleFacetViaTitleAnnotation.getComponents().get(i)
                    .getTitleEvaluator();

            Assert.assertEquals(titleMethods.get(i),
                    titleEvaluator.getMethod());
        }

        final Customer2 customer = new Customer2();

        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getPojo();
                will(returnValue(customer));
            }
        });
        final String title = titleFacetViaTitleAnnotation.title(mockObjectAdapter);
        assertThat(title, is("titleElement1. titleElement3,titleElement2"));
    }

    public static class Customer3 {
    }

    @Test
    public void testNoExplicitTitleAnnotations() {

        facetFactory.process(new ProcessClassContext(Customer3.class, mockMethodRemover, facetedMethod));

        Assert.assertNull(facetedMethod.getFacet(TitleFacet.class));
    }

    public static class Customer4 {

        @Title(sequence = "1")
        public String titleElement1() {
            return "titleElement1";
        }

        @Title(sequence = "2")
        public String titleElement2() {
            return null;
        }

        @Title(sequence = "3")
        public String titleElement3() {
            return "titleElement3";
        }

        @Title(sequence = "4", prepend = "ignored-since-null", append = "ignored-since-null")
        public Object titleElement4() {
            return null;
        }

        @Title(sequence = "4.4", prepend = "ignored-since-empty-string", append = "ignored-since-empty-string")
        public Object titleElement4a() {
            return "";
        }

        @Title(sequence = "5")
        public String titleElement5() {
            return "titleElement5";
        }

        @Title(sequence = "6")
        public Integer titleElement6() {
            return 3;
        }

        @Title(sequence = "7")
        public String titleElement7() {
            return "  this needs to be trimmed      ";
        }

    }

    @Ignore // to re-instate
    @Test
    public void titleAnnotatedMethodsSomeOfWhichReturnNulls() throws Exception {

        facetFactory.process(new ProcessClassContext(Customer4.class, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation = (TitleFacetViaTitleAnnotation) facet;

        final Customer4 customer = new Customer4();

        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getPojo();
                will(returnValue(customer));
            }
        });
        final String title = titleFacetViaTitleAnnotation.title(mockObjectAdapter);
        assertThat(title, is("titleElement1 titleElement3 titleElement5 3 this needs to be trimmed"));
    }

    
    public static class Customer5 {

        @Title(sequence = "1")
        public String titleProperty() {
            return "titleElement1";
        }

        public String otherProperty() {
            return null;
        }
    }

    
}
