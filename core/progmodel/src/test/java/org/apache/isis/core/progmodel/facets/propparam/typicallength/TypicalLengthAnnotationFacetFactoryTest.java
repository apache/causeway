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


package org.apache.isis.core.progmodel.facets.propparam.typicallength;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.propparam.typicallength.TypicalLengthFacet;
import org.apache.isis.core.metamodel.feature.FeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class TypicalLengthAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private TypicalLengthAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new TypicalLengthAnnotationFacetFactory();
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

    public void testTypicalLengthAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @TypicalLength(30)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypicalLengthFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypicalLengthFacetAnnotation);
        final TypicalLengthFacetAnnotation typicalLengthFacetAnnotation = (TypicalLengthFacetAnnotation) facet;
        assertEquals(30, typicalLengthFacetAnnotation.value());
    }

    public void testTypicalLengthAnnotationPickedUpOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@TypicalLength(20) final int foo) {}
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(method, 0, facetHolder);

        final Facet facet = facetHolder.getFacet(TypicalLengthFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypicalLengthFacetAnnotation);
        final TypicalLengthFacetAnnotation typicalLengthFacetAnnotation = (TypicalLengthFacetAnnotation) facet;
        assertEquals(20, typicalLengthFacetAnnotation.value());
    }

}

