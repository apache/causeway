/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.core.metamodel.facets.collections.collection;

import java.lang.reflect.Method;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetFromGenerics;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.hidden.HiddenFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.typeof.TypeOfFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

@SuppressWarnings("unused")
public class CollectionAnnotationFacetFactoryTest
extends AbstractFacetFactoryJUnit4TestCase {

    CollectionAnnotationFacetFactory facetFactory;
    Method collectionMethod;

    @Mock
    ObjectSpecification mockTypeSpec;
    @Mock
    ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {
            {
                oneOf(mockMethodRemover).removeMethod(actionMethod);
            }
        });
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {
            {
                allowing(mockSpecificationLoader).loadSpecification(cls);
                will(returnValue(mockTypeSpec));

                allowing(mockSpecificationLoader).loadSpecification(returnType);
                will(returnValue(mockReturnTypeSpec));
            }
        });
    }

    private static void processModify(
            final CollectionAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val collectionIfAny = processMethodContext.synthesizeOnMethod(Collection.class);
        facetFactory.processModify(processMethodContext, collectionIfAny);
    }

    private static void processHidden(
            final CollectionAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val collectionIfAny = processMethodContext.synthesizeOnMethod(Collection.class);
        facetFactory.processHidden(processMethodContext, collectionIfAny);
    }


    private static void processTypeOf(
            final CollectionAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val collectionIfAny = processMethodContext.synthesizeOnMethod(Collection.class);
        facetFactory.processTypeOf(processMethodContext, collectionIfAny);
    }


    @Before
    public void setUp() throws Exception {
        facetFactory = new CollectionAnnotationFacetFactory(metaModelContext);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }


    public static class Hidden extends CollectionAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Order {
            }
            class Customer {
                @Collection(hidden = Where.REFERENCES_PARENT)
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = ProcessMethodContext
                    .forTesting(cls, null, collectionMethod, mockMethodRemover, facetedMethod);
            processHidden(facetFactory, processMethodContext);

            // then
            final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
            Assert.assertNotNull(hiddenFacet);
            Assert.assertTrue(hiddenFacet instanceof HiddenFacetForCollectionAnnotation);
            final HiddenFacetForCollectionAnnotation hiddenFacetImpl = (HiddenFacetForCollectionAnnotation) hiddenFacet;
            assertThat(hiddenFacetImpl.where(), is(Where.REFERENCES_PARENT));

            final Facet hiddenFacetForColl = facetedMethod.getFacet(HiddenFacet.class);
            Assert.assertNotNull(hiddenFacetForColl);
            Assert.assertTrue(hiddenFacet == hiddenFacetForColl);
        }

    }

    public static class TypeOf extends CollectionAnnotationFacetFactoryTest {


        @Test
        public void whenCollectionAnnotation() {

            class Order {
            }
            class Customer {
                @Collection(typeOf = Order.class)
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = ProcessMethodContext
                    .forTesting(cls, null, collectionMethod, mockMethodRemover, facetedMethod);
            processTypeOf(facetFactory, processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetForCollectionAnnotation);
            assertThat(facet.value(), IsisMatchers.classEqualTo(Order.class));
        }

        @Test
        public void whenInferFromType() {

            class Order {
            }
            class Customer {
                public Order[] getOrders() {
                    return null;
                }

                public void setOrders(final Order[] orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = ProcessMethodContext
                    .forTesting(cls, null, collectionMethod, mockMethodRemover, facetedMethod);
            processTypeOf(facetFactory, processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetFromArray);
            assertThat(facet.value(), IsisMatchers.classEqualTo(Order.class));
        }

        @Test
        public void whenInferFromGenerics() {

            class Order {
            }
            class Customer {
                public java.util.Collection<Order> getOrders() {
                    return null;
                }

                public void setOrders(final java.util.Collection<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = ProcessMethodContext
                    .forTesting(cls, null, collectionMethod, mockMethodRemover, facetedMethod);
            processTypeOf(facetFactory, processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetFromGenerics);
            assertThat(facet.value(), IsisMatchers.classEqualTo(Order.class));
        }

    }

}
