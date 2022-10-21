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
package org.apache.causeway.core.metamodel.facets.actions.action;

import java.util.Collection;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacetFromFeature;
import org.apache.causeway.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;

import static org.apache.causeway.core.metamodel.commons.matchers.CausewayMatchers.classEqualTo;

import lombok.val;

class ActionAnnotationFacetFactoryTest_TypeOf
extends ActionAnnotationFacetFactoryTest {

    private void processTypeOf(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processTypeOf(processMethodContext, actionIfAny);
    }

    @Test
    void whenDeprecatedTypeOfAnnotationOnActionNotReturningCollection() {

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
        final ProcessMethodContext processMethodContext = ProcessMethodContext.forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNull(facet);
    }

    @Test
    void whenActionAnnotationOnActionReturningCollection() {

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
        final ProcessMethodContext processMethodContext = ProcessMethodContext.forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetForActionAnnotation);
        assertThat(facet.value().getElementType(), classEqualTo(Order.class));
    }

    @Test
    void whenActionAnnotationOnActionNotReturningCollection() {

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
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNull(facet);
    }

    @Test
    void whenInferFromType() {

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
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacet);
        assertThat(facet.value().getElementType(), classEqualTo(Order.class));
        assertThat(facet.value().getCollectionSemantics(), Matchers.is(Optional.of(CollectionSemantics.ARRAY)));
    }

    @Test
    void whenInferFromGenerics() {

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
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processTypeOf(facetFactory, processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertEquals(TypeOfFacetFromFeature.class, facet.getClass());
        assertThat(facet.value().getElementType(), classEqualTo(Order.class));
    }

}
