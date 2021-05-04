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
package org.apache.isis.core.metamodel.facets.actions.action;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;

import static org.apache.isis.core.metamodel.commons.matchers.IsisMatchers.classEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

import lombok.val;

public class ActionAnnotationFacetFactoryTest_TypeOf extends ActionAnnotationFacetFactoryTest {

    private void processTypeOf(
            ActionAnnotationFacetFactory facetFactory, ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processTypeOf(processMethodContext, actionIfAny);
    }

    @Test
    public void whenDeprecatedTypeOfAnnotationOnActionNotReturningCollection() {

        class Customer {
            @SuppressWarnings("unused")
            public Customer someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void whenActionAnnotationOnActionReturningCollection() {

        class Order {
        }
        class Customer {
            @SuppressWarnings("rawtypes")
            @Action(typeOf = Order.class)
            public Collection someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertTrue(facet instanceof TypeOfFacetForActionAnnotation);
        assertThat(facet.value(), classEqualTo(Order.class));
    }

    @Test
    public void whenActionAnnotationOnActionNotReturningCollection() {

        class Order {
        }
        class Customer {
            @Action(typeOf = Order.class)
            public Customer someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void whenInferFromType() {

        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Order[] someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertTrue(facet instanceof TypeOfFacetInferredFromArray);
        assertThat(facet.value(), classEqualTo(Order.class));
    }

    @Test
    public void whenInferFromGenerics() {

        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertEquals(TypeOfFacetInferredFromGenerics.class, facet.getClass());
        assertThat(facet.value(), classEqualTo(Order.class));
    }

}