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
package org.apache.causeway.core.metamodel.facets.object.callback;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract2;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.CallbackFacetFactory;

import lombok.val;

abstract class CallbackFacetFactoryTestAbstract
extends FacetFactoryTestAbstract2 {

    protected CallbackFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new CallbackFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    protected void assertPicksUp(
            final int expectedCallbackCount,
            final FacetFactory facetFactory,
            final Class<?> type,
            final ProgrammingModelConstants.CallbackMethod callbackMethod,
            final Class<? extends ImperativeFacet> facetType) {

        // when
        facetFactory.process(ProcessClassContext
                .forTesting(type, methodRemover, facetedMethod));

        val callbackMethods = callbackMethod.getMethodNames()
                .map(methodName->findMethod(type, methodName))
                .map(_MethodFacades::regular);

        Assertions.assertEquals(expectedCallbackCount, callbackMethods.size());

        val facet = facetedMethod.getFacet(facetType);
        assertNotNull(facet);
        assertTrue(facet instanceof ImperativeFacet);
        val imperativeFacet = facet;

        callbackMethods.forEach(method->{
            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
            assertTrue(imperativeFacet.getMethods().contains(method));
        });

    }

}
