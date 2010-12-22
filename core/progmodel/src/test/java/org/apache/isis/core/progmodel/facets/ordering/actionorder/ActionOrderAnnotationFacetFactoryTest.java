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


package org.apache.isis.core.progmodel.facets.ordering.actionorder;

import java.util.List;

import org.apache.isis.applib.annotation.ActionOrder;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.ordering.actionorder.ActionOrderFacet;
import org.apache.isis.core.metamodel.feature.FeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class ActionOrderAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private ActionOrderAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ActionOrderAnnotationFacetFactory();
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

    public void testActionOrderAnnotationPickedUpOnClass() {
        @ActionOrder("foo,bar")
        class Customer {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(ActionOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionOrderFacetAnnotation);
        final ActionOrderFacetAnnotation actionOrderFacetAnnotation = (ActionOrderFacetAnnotation) facet;
        assertEquals("foo,bar", actionOrderFacetAnnotation.value());

        assertNoMethodsRemoved();
    }

}

