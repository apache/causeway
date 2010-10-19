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


package org.apache.isis.metamodel.facets.ordering.memberorder;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;


public class MemberOrderAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private MemberOrderAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new MemberOrderAnnotationFacetFactory();
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
        assertTrue(contains(featureTypes, ObjectFeatureType.PROPERTY));
        assertTrue(contains(featureTypes, ObjectFeatureType.COLLECTION));
        assertTrue(contains(featureTypes, ObjectFeatureType.ACTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION_PARAMETER));
    }

    public void testMemberOrderAnnotationPickedUpOnProperty() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @MemberOrder(sequence = "1")
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MemberOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberOrderFacetAnnotation);
        final MemberOrderFacetAnnotation memberOrderFacetAnnotation = (MemberOrderFacetAnnotation) facet;
        assertEquals("1", memberOrderFacetAnnotation.sequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnCollection() {
        class Order {}
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @MemberOrder(sequence = "2")
            public Collection<Order> getOrders() {
                return null;
            }

            public void addToOrders(final Order o) {}
        }
        final Method method = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MemberOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberOrderFacetAnnotation);
        final MemberOrderFacetAnnotation memberOrderFacetAnnotation = (MemberOrderFacetAnnotation) facet;
        assertEquals("2", memberOrderFacetAnnotation.sequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnAction() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            @MemberOrder(sequence = "3")
            public void someAction() {}
        }
        final Method method = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, method, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(MemberOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberOrderFacetAnnotation);
        final MemberOrderFacetAnnotation memberOrderFacetAnnotation = (MemberOrderFacetAnnotation) facet;
        assertEquals("3", memberOrderFacetAnnotation.sequence());

        assertNoMethodsRemoved();
    }

}

