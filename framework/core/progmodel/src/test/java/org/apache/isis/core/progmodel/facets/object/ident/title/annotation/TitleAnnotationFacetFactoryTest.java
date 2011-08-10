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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.object.title.annotation.Title;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation;


public class TitleAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private TitleAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new TitleAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testTitleAnnotatedMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {

            @SuppressWarnings("unused")
            @Title
            public String someTitle() {
                return "Some Title";
            }

        }

        titleAnnotatedMethodsPickedUpOnClassAndMethodsRemovedCommonTest(Customer.class);
    }

    public void testTitleAnnotatedMethodsPickedUpOnClassAndMethodsRemoved() {
        class Customer {

            @SuppressWarnings("unused")
            @Title
            public String titleElement1() {
                return "titleElement1";
            }

            @SuppressWarnings("unused")
            @Title
            public String titleElement2() {
                return "titleElement2";
            }

            @SuppressWarnings("unused")
            @Title
            public String titleElement3() {
                return "titleElement3";
            }

        }

        titleAnnotatedMethodsPickedUpOnClassAndMethodsRemovedCommonTest(Customer.class);
    }

    public void testNoExplicitTitleAnnotations() {
        class Customer {}

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(TitleFacet.class));

        assertNoMethodsRemoved();
    }

    protected void titleAnnotatedMethodsPickedUpOnClassAndMethodsRemovedCommonTest(Class<?> type) {
    	assertNotNull("Type MUST not be 'null'", type);

        final List<Method> titleMethods = MethodFinderUtils.findMethodsWithAnnotation(type, MethodScope.OBJECT, Title.class);

        facetFactory.process(new ProcessClassContext(type, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetViaTitleAnnotation);
        final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation = (TitleFacetViaTitleAnnotation) facet;
        assertEquals(titleMethods, titleFacetViaTitleAnnotation.getMethods());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().containsAll(titleMethods));
    }

}
