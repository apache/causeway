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
package org.apache.causeway.core.metamodel.facets.ordering.memberorder;

import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract2;
import org.apache.causeway.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromActionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromCollectionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromPropertyLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;

import lombok.val;

class LayoutOrderTest
extends FacetFactoryTestAbstract2 {

    @BeforeEach
    protected void setUp() {

//FIXME
//        context.checking(new Expectations() {{
//            allowing(mockTranslationService).translate(with(any(TranslationContext.class)), with(any(String.class)));
//            will(new Action() {
//                @Override
//                public Object invoke(final Invocation invocation) throws Throwable {
//                    return invocation.getParameter(1);
//                }
//
//                @Override
//                public void describeTo(final Description description) {
//                    description.appendText("Returns parameter #1");
//                }
//            });
//        }});
    }

    public void testMemberOrderAnnotationPickedUpOnProperty() {
        class Customer {
            @PropertyLayout(sequence = "1")
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethodExactOrFail(Customer.class, "getFirstName");

        val facetFactory = new PropertyLayoutFacetFactory(getMetaModelContext());
        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        val facet = facetedMethod.getFacet(LayoutOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LayoutOrderFacetFromPropertyLayoutAnnotation);
        assertEquals("1", facet.getSequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnCollection() {
        class Order {
        }
        class Customer {
            @CollectionLayout(sequence = "2")
            public Collection<Order> getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void addToOrders(final Order o) {
            }
        }
        final Method method = findMethodExactOrFail(Customer.class, "getOrders");

        val facetFactory = new CollectionLayoutFacetFactory(getMetaModelContext());
        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        val facet = facetedMethod.getFacet(LayoutOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LayoutOrderFacetFromCollectionLayoutAnnotation);
        assertEquals("2", facet.getSequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnAction() {
        class Customer {
            @ActionLayout(sequence = "3")
            public void someAction() {
            }
        }
        final Method method = findMethodExactOrFail(Customer.class, "someAction");

        val facetFactory = new ActionLayoutFacetFactory(getMetaModelContext());
        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        val facet = facetedMethod.getFacet(LayoutOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LayoutOrderFacetFromActionLayoutAnnotation);
        assertEquals("3", facet.getSequence());

        assertNoMethodsRemoved();
    }

}
