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

package org.apache.isis.metamodel.facets.collections.collection;

import java.lang.reflect.Method;
import java.util.Collection;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.metamodel.spec.ObjectSpecification;

public class CollectionAnnotationFacetFactoryTest_typeOf extends AbstractFacetFactoryTest {

    private CollectionAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        facetFactory = new CollectionAnnotationFacetFactory();
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

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(Customer.class);
            will(returnValue(mockCustomerSpec));
        }});

        facetFactory.process(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
        final TypeOfFacetInferredFromGenerics typeOfFacetInferredFromGenerics = (TypeOfFacetInferredFromGenerics) facet;
        assertEquals(Order.class, typeOfFacetInferredFromGenerics.value());

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

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(Customer.class);
            will(returnValue(mockCustomerSpec));
        }});

        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
        final TypeOfFacetInferredFromGenerics typeOfFacetInferredFromGenerics = (TypeOfFacetInferredFromGenerics) facet;
        assertEquals(Order.class, typeOfFacetInferredFromGenerics.value());

    }

    @Mock
    ObjectSpecification mockCustomerSpec;

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

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(Customer.class);
            will(returnValue(mockCustomerSpec));
        }});

        facetFactory.process(new ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromArray);
        final TypeOfFacetInferredFromArray typeOfFacetInferredFromArray = (TypeOfFacetInferredFromArray) facet;
        assertEquals(Order.class, typeOfFacetInferredFromArray.value());

    }


}
