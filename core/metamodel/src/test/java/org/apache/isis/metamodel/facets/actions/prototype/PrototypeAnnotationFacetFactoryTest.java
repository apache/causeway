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


package org.apache.isis.metamodel.facets.actions.prototype;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;


public class PrototypeAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private PrototypeAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new PrototypeAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertFalse(contains(featureTypes, ObjectFeatureType.OBJECT));
        assertFalse(contains(featureTypes, ObjectFeatureType.PROPERTY));
        assertFalse(contains(featureTypes, ObjectFeatureType.COLLECTION));
        assertTrue(contains(featureTypes, ObjectFeatureType.ACTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION_PARAMETER));
    }

    public void testAnnotationPickedUp() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
			@Prototype
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(PrototypeFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PrototypeFacetAbstract);

        assertNoMethodsRemoved();
    }

}

