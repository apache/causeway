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


package org.apache.isis.metamodel.facets.propparam.multiline;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.Facet;


public class MultiLineAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private MultiLineAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new MultiLineAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, ObjectFeatureType.OBJECT));
        assertTrue(contains(featureTypes, ObjectFeatureType.PROPERTY));
        assertFalse(contains(featureTypes, ObjectFeatureType.COLLECTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION));
        assertTrue(contains(featureTypes, ObjectFeatureType.ACTION_PARAMETER));
    }

    public void testMultiLineAnnotationPickedUpOnClass() {
        @MultiLine(numberOfLines = 3, preventWrapping = false)
        class Customer {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MultiLineFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MultiLineFacetAnnotation);
        final MultiLineFacetAnnotation multiLineFacetAnnotation = (MultiLineFacetAnnotation) facet;
        assertEquals(3, multiLineFacetAnnotation.numberOfLines());
        assertEquals(false, multiLineFacetAnnotation.preventWrapping());
    }

    public void testMultiLineAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @MultiLine(numberOfLines = 12, preventWrapping = true)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MultiLineFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MultiLineFacetAnnotation);
        final MultiLineFacetAnnotation multiLineFacetAnnotation = (MultiLineFacetAnnotation) facet;
        assertEquals(12, multiLineFacetAnnotation.numberOfLines());
        assertEquals(true, multiLineFacetAnnotation.preventWrapping());
    }

    public void testMultiLineAnnotationPickedUpOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@MultiLine(numberOfLines = 8, preventWrapping = false) final String foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(method, 0, facetHolder);

        final Facet facet = facetHolder.getFacet(MultiLineFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MultiLineFacetAnnotation);
        final MultiLineFacetAnnotation multiLineFacetAnnotation = (MultiLineFacetAnnotation) facet;
        assertEquals(8, multiLineFacetAnnotation.numberOfLines());
        assertEquals(false, multiLineFacetAnnotation.preventWrapping());
    }

    public void testMultiLineAnnotationDefaults() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        @MultiLine
        class Customer {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MultiLineFacet.class);
        final MultiLineFacetAnnotation multiLineFacetAnnotation = (MultiLineFacetAnnotation) facet;
        assertEquals(6, multiLineFacetAnnotation.numberOfLines());
        assertEquals(true, multiLineFacetAnnotation.preventWrapping());
    }

    public void testMultiLineAnnotationIgnoredForNonStringProperties() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @MultiLine(numberOfLines = 8, preventWrapping = false)
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MultiLineFacet.class);
        assertNull(facet);
    }

    public void testMultiLineAnnotationIgnoredForNonStringActionParameters() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@MultiLine(numberOfLines = 8, preventWrapping = false) final int foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(method, 0, facetHolder);

        assertNull(facetHolder.getFacet(MultiLineFacet.class));
    }

}

