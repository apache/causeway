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


package org.apache.isis.core.progmodel.facets.propparam.validate.mask;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.Mask;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.propparam.validate.mask.annotation.MaskAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.mask.annotation.MaskFacetAnnotation;


public class MaskAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private MaskAnnotationFacetFactory facetFactory;
    private final ObjectSpecification customerNoSpec = new TestProxySpecification(String.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        reflector.setLoadSpecificationStringReturn(customerNoSpec);
        facetFactory = new MaskAnnotationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
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

    public void testMaskAnnotationPickedUpOnClass() {
        @Mask("###")
        class Customer {}
        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MaskFacet.class);
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

        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MaskFacet.class);
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

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(MaskFacet.class);
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

        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        assertNotNull(facetedMethod.getFacet(MaskFacet.class));
    }

    public void testMaskAnnotationNotIgnoredForPrimitiveOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Mask("###") final int foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        assertNotNull(facetedMethodParameter.getFacet(MaskFacet.class));
    }

}

