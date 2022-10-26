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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.prototype.PrototypeFacet;

import lombok.val;

class ActionAnnotationFacetFactoryTest_RestrictTo
extends ActionAnnotationFacetFactoryTest {

    private void processRestrictTo(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processRestrictTo(processMethodContext, actionIfAny);
    }

    @Test
    void whenRestrictedToPrototyping() {

        class Customer {
            @Action(restrictTo = org.apache.causeway.applib.annotation.RestrictTo.PROTOTYPING)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processRestrictTo(facetFactory, processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        assertNotNull(facet);
    }

    @Test
    void whenRestrictedToNoRestriction() {

        class Customer {
            @Action(restrictTo = org.apache.causeway.applib.annotation.RestrictTo.NO_RESTRICTIONS)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processRestrictTo(facetFactory, processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        assertNull(facet);
    }

    @Test
    void whenNotPresent() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processRestrictTo(facetFactory, processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        assertNull(facet);
    }

}