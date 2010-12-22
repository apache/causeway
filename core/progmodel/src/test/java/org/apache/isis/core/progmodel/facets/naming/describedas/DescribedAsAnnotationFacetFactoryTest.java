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


package org.apache.isis.core.progmodel.facets.naming.describedas;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.feature.FeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class DescribedAsAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private DescribedAsAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DescribedAsAnnotationFacetFactory();
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
        assertTrue(contains(featureTypes, FeatureType.COLLECTION));
        assertTrue(contains(featureTypes, FeatureType.ACTION));
        assertTrue(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testDescribedAsAnnotationPickedUpOnClass() {
        @DescribedAs("some description")
        class Customer {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @DescribedAs("some description")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnCollection() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @DescribedAs("some description")
            public Collection getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnAction() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @DescribedAs("some description")
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            public void someAction(@DescribedAs("some description") final int x) {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(actionMethod, 0, facetHolder);

        final Facet facet = facetHolder.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());
    }

}

