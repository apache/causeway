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
package org.apache.causeway.core.metamodel.facets.collections.collection;

import java.util.Collection;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacetFromFeature;

class CollectionAnnotationFacetFactoryTest_typeOf
extends FacetFactoryTestAbstract {

    private CollectionAnnotationFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new CollectionAnnotationFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    @Test
    void typeOfFacetInferredForActionWithGenericCollectionReturnType() {
        class Order {
        }
        @SuppressWarnings("unused")
        class Customer {
            public Collection<Order> someAction() { return null;}
        }
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            facetFactory.process(processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof TypeOfFacetFromFeature);
            final TypeOfFacetFromFeature typeOfFacetInferredFromGenerics = (TypeOfFacetFromFeature) facet;
            assertEquals(Order.class, typeOfFacetInferredFromGenerics.value().elementType());
        });
    }

    @Test
    void typeOfFacetInferredForCollectionWithGenericCollectionReturnType() {
        class Order {
        }
        @SuppressWarnings("unused")
        class Customer {
            public Collection<Order> getOrders() { return null; }
        }
        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof TypeOfFacetFromFeature);
            final TypeOfFacetFromFeature typeOfFacetInferredFromGenerics = (TypeOfFacetFromFeature) facet;
            assertEquals(Order.class, typeOfFacetInferredFromGenerics.value().elementType());
        });
    }

    @Test
    void typeOfFacetIsInferredForCollectionFromOrderArray() {
        class Order {
        }
        @SuppressWarnings("unused")
        class Customer {
            public Order[] getOrders() { return null;}
        }
        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            assertNotNull(facet);
            assertEquals(Order.class, facet.value().elementType());
            assertThat(facet.value().collectionSemantics(), Matchers.is(Optional.of(CollectionSemantics.ARRAY)));
        });
    }

}
