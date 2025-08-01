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
package org.apache.causeway.core.metamodel.facets.object.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;

public abstract class ObjectSupportFacetFactoryTestAbstract
extends FacetFactoryTestAbstract {

    protected ObjectSupportFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new ObjectSupportFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    /**
     * see also CallbackFacetFactoryTestAbstract#assertPicksUp
     */
    protected void assertPicksUp(
            final int expectedSupportMethodCount,
            final FacetFactory facetFactory,
            final Class<?> type,
            final ProgrammingModelConstants.ObjectSupportMethod supportMethodEnum,
            final Class<? extends Facet> facetType) {

        objectScenario(type, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            var supportMethods = supportMethodEnum.getMethodNames()
                    .map(methodName->findMethodExactOrFail(type, methodName))
                    .map(_MethodFacades::regular)
                    .map(MethodFacade::asMethodElseFail);

            assertEquals(expectedSupportMethodCount, supportMethods.size());

            var facet = facetHolder.getFacet(facetType);
            assertNotNull(facet);
            assertTrue(facet instanceof ImperativeFacet);
            var imperativeFacet = (ImperativeFacet)facet;

            supportMethods.forEach(method->{
                assertMethodWasRemoved(method);
                assertTrue(imperativeFacet.getMethods()
                        .map(MethodFacade::asMethodElseFail).contains(method));
            });

        });

    }

}
