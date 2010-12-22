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


package org.apache.isis.core.progmodel.facets.ordering.fieldorder;

import java.util.List;

import org.apache.isis.applib.annotation.FieldOrder;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.ordering.FieldOrderFacet;
import org.apache.isis.core.metamodel.feature.FeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class FieldOrderAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private FieldOrderAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new FieldOrderAnnotationFacetFactory();
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
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testFieldOrderAnnotationPickedUpOnClass() {
        @FieldOrder("foo,bar")
        class Customer {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(FieldOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof FieldOrderFacetAnnotation);
        final FieldOrderFacetAnnotation fieldOrderFacetAnnotation = (FieldOrderFacetAnnotation) facet;
        assertEquals("foo,bar", fieldOrderFacetAnnotation.value());

        assertNoMethodsRemoved();
    }

}

