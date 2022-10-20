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

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetFallbackToNonIdempotent;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

import lombok.val;

class ActionSemanticsFacetFallbackToNonIdempotentFactoryTest
extends AbstractFacetFactoryTest {

    private ActionAnnotationFacetFactory facetFactory;

    private void processSemantics(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processSemantics(processMethodContext, actionIfAny);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ActionAnnotationFacetFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testNoAnnotationPickedUp() {
        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processSemantics(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionSemanticsFacetFallbackToNonIdempotent);

        assertNoMethodsRemoved();
    }

}
