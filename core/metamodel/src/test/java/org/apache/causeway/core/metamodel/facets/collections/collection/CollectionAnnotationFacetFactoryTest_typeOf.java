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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

import org.hamcrest.Matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacetFromFeature;

class CollectionAnnotationFacetFactoryTest_typeOf
extends AbstractFacetFactoryTest {

    private CollectionAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        facetFactory = new CollectionAnnotationFacetFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }


    public void testTypeOfFacetInferredForActionWithGenericCollectionReturnType() {
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetFromFeature);
        final TypeOfFacetFromFeature typeOfFacetInferredFromGenerics = (TypeOfFacetFromFeature) facet;
        assertEquals(Order.class, typeOfFacetInferredFromGenerics.value().getElementType());

    }

    public void testTypeOfFacetInferredForCollectionWithGenericCollectionReturnType() {
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }
        }

        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetFromFeature);
        final TypeOfFacetFromFeature typeOfFacetInferredFromGenerics = (TypeOfFacetFromFeature) facet;
        assertEquals(Order.class, typeOfFacetInferredFromGenerics.value().getElementType());

    }

    public void testTypeOfFacetIsInferredForCollectionFromOrderArray() {
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Order[] getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertEquals(Order.class, facet.value().getElementType());
        assertThat(facet.value().getCollectionSemantics(), Matchers.is(Optional.of(CollectionSemantics.ARRAY)));

    }


}
