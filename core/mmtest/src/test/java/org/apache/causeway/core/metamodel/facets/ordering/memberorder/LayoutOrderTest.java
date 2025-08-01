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

import java.util.Collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromActionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromCollectionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromPropertyLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;

class LayoutOrderTest
extends FacetFactoryTestAbstract {

    @Test
    void memberOrderAnnotationPickedUpOnProperty() {
        var facetFactory = new PropertyLayoutFacetFactory(getMetaModelContext());
        class Customer {
            @PropertyLayout(sequence = "1")
            public String getFirstName() { return null; }
        }
        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            var facet = facetedMethod.getFacet(LayoutOrderFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof LayoutOrderFacetFromPropertyLayoutAnnotation);
            assertEquals("1", facet.getSequence());

            assertNoMethodsRemoved();
        });
    }

    @Test
    void memberOrderAnnotationPickedUpOnCollection() {
        var facetFactory = new CollectionLayoutFacetFactory(getMetaModelContext());
        class Order {
        }
        @SuppressWarnings("unused")
        class Customer {
            @CollectionLayout(sequence = "2")
            public Collection<Order> getOrders() { return null;}
            public void addToOrders(final Order o) {}
        }
        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            var facet = facetedMethod.getFacet(LayoutOrderFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof LayoutOrderFacetFromCollectionLayoutAnnotation);
            assertEquals("2", facet.getSequence());

            assertNoMethodsRemoved();
        });
    }

    @Test
    void memberOrderAnnotationPickedUpOnAction() {
        var facetFactory = new ActionLayoutFacetFactory(getMetaModelContext());
        class Customer {
            @ActionLayout(sequence = "3")
            public void someAction() {}
        }
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            facetFactory.process(processMethodContext);
            //then
            var facet = facetedMethod.getFacet(LayoutOrderFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof LayoutOrderFacetFromActionLayoutAnnotation);
            assertEquals("3", facet.getSequence());

            assertNoMethodsRemoved();
        });
    }
}
