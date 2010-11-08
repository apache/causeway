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


package org.apache.isis.metamodel.facets.propparam.validate.mask;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Mask;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.Facet;


public class MaskAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private MaskAnnotationFacetFactory facetFactory;
    private final ObjectSpecification customerNoSpec = new TestProxySpecification(String.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        reflector.setLoadSpecificationStringReturn(customerNoSpec);
        facetFactory = new MaskAnnotationFacetFactory();
        facetFactory.setSpecificationLoader(reflector);
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

    public void testMaskAnnotationPickedUpOnClass() {
        @Mask("###")
        class Customer {}
        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MaskFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MaskFacetAnnotation);
        final MaskFacetAnnotation maskFacet = (MaskFacetAnnotation) facet;
        assertEquals("###", maskFacet.value());
    }

    public void testMaskAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @Mask("###")
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MaskFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MaskFacetAnnotation);
        final MaskFacetAnnotation maskFacet = (MaskFacetAnnotation) facet;
        assertEquals("###", maskFacet.value());
    }

    public void testMaskAnnotationPickedUpOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Mask("###") final String foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(method, 0, facetHolder);

        final Facet facet = facetHolder.getFacet(MaskFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MaskFacetAnnotation);
        final MaskFacetAnnotation maskFacet = (MaskFacetAnnotation) facet;
        assertEquals("###", maskFacet.value());
    }

    public void testMaskAnnotationNotIgnoredForNonStringsProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @Mask("###")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        assertNotNull(facetHolder.getFacet(MaskFacet.class));
    }

    public void testMaskAnnotationNotIgnoredForPrimitiveOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Mask("###") final int foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(method, 0, facetHolder);

        assertNotNull(facetHolder.getFacet(MaskFacet.class));
    }

}

