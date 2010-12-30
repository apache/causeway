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


package org.apache.isis.core.progmodel.facets.propparam.validate.regex;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class RegExAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private RegExAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new RegExAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, FeatureType.OBJECT));
        assertTrue(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertTrue(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testRegExAnnotationPickedUpOnClass() {
        @RegEx(validation = "^A.*", caseSensitive = false)
        class Customer {}
        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetAnnotation);
        final RegExFacetAnnotation regExFacet = (RegExFacetAnnotation) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @RegEx(validation = "^A.*", caseSensitive = false)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetAnnotation);
        final RegExFacetAnnotation regExFacet = (RegExFacetAnnotation) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationPickedUpOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RegEx(validation = "^A.*", caseSensitive = false) final String foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(method, 0, facetHolder);

        final Facet facet = facetHolder.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetAnnotation);
        final RegExFacetAnnotation regExFacet = (RegExFacetAnnotation) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationIgnoredForNonStringsProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @RegEx(validation = "^A.*", caseSensitive = false)
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        assertNull(facetHolder.getFacet(RegExFacet.class));
    }

    public void testRegExAnnotationIgnoredForPrimitiveOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RegEx(validation = "^A.*", caseSensitive = false) final int foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(method, 0, facetHolder);

        assertNull(facetHolder.getFacet(RegExFacet.class));
    }

}

