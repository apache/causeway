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


package org.apache.isis.core.progmodel.facets.hide;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.feature.FeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class HiddenAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private HiddenAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new HiddenAnnotationFacetFactory();
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
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testHiddenAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Hidden
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testHiddenAnnotationPickedUpOnCollection() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Hidden
            public Collection getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testHiddenAnnotationPickedUpOnAction() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Hidden
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testHiddenWhenAlwaysAnnotationPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Hidden(When.ALWAYS)
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.ALWAYS, hiddenFacetAbstract.value());
    }

    public void testHiddenWhenNeverAnnotationPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Hidden(When.NEVER)
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.NEVER, hiddenFacetAbstract.value());
    }

    public void testHiddenWhenOncePersistedAnnotationPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Hidden(When.ONCE_PERSISTED)
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.ONCE_PERSISTED, hiddenFacetAbstract.value());
    }

    public void testDisabledWhenUntilPersistedAnnotationPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @Hidden(When.UNTIL_PERSISTED)
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.UNTIL_PERSISTED, hiddenFacetAbstract.value());
    }

}

