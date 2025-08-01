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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

class ActionAnnotationFacetFactoryTest_Semantics
extends ActionAnnotationFacetFactoryTest {

    private void processSemantics(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        var actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processSemantics(processMethodContext, actionIfAny);
    }

    @Test
    void whenSafe() {

        class Customer {
            @Action(semantics = SemanticsOf.SAFE)
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processSemantics(facetFactory, processMethodContext);
            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            assertNotNull(facet);
            assertThat(facet.value(), is(SemanticsOf.SAFE));
        });
    }

    @Test
    void whenNotSpecified() {

        class Customer {
            @Action()
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processSemantics(facetFactory, processMethodContext);
            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            assertNotNull(facet);
            assertThat(facet.value(), is(SemanticsOf.NON_IDEMPOTENT));
        });
    }

    @Test
    void whenNoAnnotation() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processSemantics(facetFactory, processMethodContext);
            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            assertNotNull(facet);
            assertThat(facet.value(), is(SemanticsOf.NON_IDEMPOTENT));
        });
    }

}