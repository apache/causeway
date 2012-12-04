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
package org.apache.isis.core.progmodel.facets.object.ident.title.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.LocalizationDefault;
import org.apache.isis.core.metamodel.adapter.LocalizationProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation.TitleComponent;

@RunWith(JMock.class)
public class TitleAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private final Mockery context = new JUnit4Mockery();
    private ObjectAdapter objectAdapter;

    private TitleAnnotationFacetFactory facetFactory;

    private SpecificationLoader mockSpecificationLookup;
    private AdapterManager mockAdapterMap;
    private LocalizationProvider mockLocalizationProvider;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        mockSpecificationLookup = context.mock(SpecificationLoader.class);
        mockAdapterMap = context.mock(AdapterManager.class);
        mockLocalizationProvider = context.mock(LocalizationProvider.class);

        objectAdapter = context.mock(ObjectAdapter.class);

        facetFactory = new TitleAnnotationFacetFactory();
        facetFactory.setAdapterManager(mockAdapterMap);
        facetFactory.setSpecificationLookup(mockSpecificationLookup);
        facetFactory.setLocalizationProvider(mockLocalizationProvider);

        context.checking(new Expectations() {
            {
                allowing(mockAdapterMap);
                allowing(mockSpecificationLookup);
                allowing(mockLocalizationProvider).getLocalization();
                will(returnValue(new LocalizationDefault()));
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
        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetViaTitleAnnotation);
        final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation = (TitleFacetViaTitleAnnotation) facet;

        final List<Method> titleMethods = Arrays.asList(Customer.class.getMethod("someTitle"));
        for (int i = 0; i < titleMethods.size(); i++) {
            assertEquals(titleMethods.get(i), titleFacetViaTitleAnnotation.getComponents().get(i).getMethod());
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

    @Test
    public void testTitleAnnotatedMethodsPickedUpOnClass() throws Exception {

        facetFactory.process(new ProcessClassContext(Customer2.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetViaTitleAnnotation);
        final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation = (TitleFacetViaTitleAnnotation) facet;

        final List<Method> titleMethods = Arrays.asList(Customer2.class.getMethod("titleElement1"), Customer2.class.getMethod("titleElement3"), Customer2.class.getMethod("titleElement2"));

        final List<TitleComponent> components = titleFacetViaTitleAnnotation.getComponents();
        for (int i = 0; i < titleMethods.size(); i++) {
            assertEquals(titleMethods.get(i), components.get(i).getMethod());
        }

        final Customer2 customer = new Customer2();

        context.checking(new Expectations() {
            {
                allowing(objectAdapter).getObject();
                will(returnValue(customer));
            }
        });
        final String title = titleFacetViaTitleAnnotation.title(objectAdapter, mockLocalizationProvider.getLocalization());
        assertThat(title, is("titleElement1. titleElement3,titleElement2"));
    }

    public static class Customer3 {
    }

    @Test
    public void testNoExplicitTitleAnnotations() {

        facetFactory.process(new ProcessClassContext(Customer3.class, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(TitleFacet.class));
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

    @Test
    public void titleAnnotatedMethodsSomeOfWhichReturnNulls() throws Exception {

        facetFactory.process(new ProcessClassContext(Customer4.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation = (TitleFacetViaTitleAnnotation) facet;

        final Customer4 customer = new Customer4();

        context.checking(new Expectations() {
            {
                allowing(objectAdapter).getObject();
                will(returnValue(customer));
            }
        });
        final String title = titleFacetViaTitleAnnotation.title(objectAdapter, mockLocalizationProvider.getLocalization());
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

    @Test
    public void hiddenFacetAnnotationInferredFromTitleAnnotation() throws Exception {

        final Method propertyMethod = findMethod(Customer5.class, "titleProperty");
        
        facetFactory.process(new ProcessMethodContext(Customer5.class, propertyMethod, methodRemover, facetedMethod));

        final Customer5 customer = new Customer5();

        context.checking(new Expectations() {
            {
                allowing(objectAdapter).getObject();
                will(returnValue(customer));
            }
        });
        final HiddenFacet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertThat(facet.where(), is(Where.ALL_TABLES));
    }


    @Test
    public void hiddenFacetAnnotationNotInferredIfTitleAnnotationNotPresent() throws Exception {

        final Method propertyMethod = findMethod(Customer5.class, "otherProperty");
        
        facetFactory.process(new ProcessMethodContext(Customer5.class, propertyMethod, methodRemover, facetedMethod));

        final Customer5 customer = new Customer5();

        context.checking(new Expectations() {
            {
                allowing(objectAdapter).getObject();
                will(returnValue(customer));
            }
        });
        final HiddenFacet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNull(facet);
    }
    
}
