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


package org.apache.isis.metamodel.facets.naming.named;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.Facet;


public class NamedAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private NamedAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new NamedAnnotationFacetFactory();
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
        assertTrue(contains(featureTypes, ObjectFeatureType.COLLECTION));
        assertTrue(contains(featureTypes, ObjectFeatureType.ACTION));
        assertTrue(contains(featureTypes, ObjectFeatureType.ACTION_PARAMETER));
    }

    public void testNamedAnnotationPickedUpOnClass() {
        @Named("some name")
        class Customer {}
        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Named("some name")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnCollection() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Named("some name")
            public Collection getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnAction() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Named("some name")
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnActionParameter() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            public void someAction(@Named("some name") final int x) {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(actionMethod, 0, facetHolder);

        final Facet facet = facetHolder.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());
    }

}

