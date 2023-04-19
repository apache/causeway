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
package org.apache.causeway.core.metamodel.facets.collections;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessor;
import org.apache.causeway.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessorFactory;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;

import lombok.val;

class CollectionFieldMethodsFacetFactoryTest
extends FacetFactoryTestAbstract {

    @Test
    void propertyAccessorFacetIsInstalledForJavaUtilCollectionAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings({ "rawtypes", "unused" })
        class Customer {
            public Collection getOrders() { return null; }
        }

        final Method collectionAccessorMethod = findMethodExactOrFail(Customer.class, "getOrders");

        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
            val propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(collectionAccessorMethod, propertyAccessorFacetViaAccessor);
            assertMethodWasRemoved(collectionAccessorMethod);
        });
    }

    @Test
    void propertyAccessorFacetIsInstalledForJavaUtilListAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings({ "rawtypes", "unused" })
        class Customer {
            public List getOrders() { return null; }
        }

        final Method collectionAccessorMethod = findMethodExactOrFail(Customer.class, "getOrders");

        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
            val propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(collectionAccessorMethod, propertyAccessorFacetViaAccessor);
            assertMethodWasRemoved(collectionAccessorMethod);
        });
    }

    @Test
    void propertyAccessorFacetIsInstalledForJavaUtilSetAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings({ "rawtypes", "unused" })
        class Customer {
            public Set getOrders() { return null; }
        }

        final Method collectionAccessorMethod = findMethodExactOrFail(Customer.class, "getOrders");

        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
            val propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(collectionAccessorMethod, propertyAccessorFacetViaAccessor);
            assertMethodWasRemoved(collectionAccessorMethod);
        });
    }

    @Test
    void propertyAccessorFacetIsInstalledForObjectArrayAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public Object[] getOrders() { return null; }
        }

        final Method collectionAccessorMethod = findMethodExactOrFail(Customer.class, "getOrders");

        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
            val propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(collectionAccessorMethod, propertyAccessorFacetViaAccessor);
            assertMethodWasRemoved(collectionAccessorMethod);
        });
    }

    public void testPropertyAccessorFacetIsInstalledForOrderArrayAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory(getMetaModelContext());
        class Order {
        }
        @SuppressWarnings("unused")
        class Customer {
            public Order[] getOrders() { return null; }
        }

        final Method collectionAccessorMethod = findMethodExactOrFail(Customer.class, "getOrders");

        collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
            val propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(collectionAccessorMethod, propertyAccessorFacetViaAccessor);
            assertMethodWasRemoved(collectionAccessorMethod);
        });
    }

    @Test
    void methodFoundInSuperclass() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory(getMetaModelContext());
        class Order {
        }
        @SuppressWarnings("unused")
        class Customer {
            public Collection<Order> getOrders() { return null; }
        }
        class CustomerEx extends Customer {
        }
        final Method collectionAccessorMethod = findMethodExactOrFail(CustomerEx.class, "getOrders");
        collectionScenario(CustomerEx.class, "orders", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
            val collectionAccessorFacetViaMethod = (CollectionAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(collectionAccessorMethod, collectionAccessorFacetViaMethod);
        });
    }
}
