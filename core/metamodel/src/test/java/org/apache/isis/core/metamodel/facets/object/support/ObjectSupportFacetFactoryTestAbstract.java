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
package org.apache.isis.core.metamodel.facets.object.support;

import org.junit.jupiter.api.Assertions;

import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;

import lombok.val;

public abstract class ObjectSupportFacetFactoryTestAbstract
extends AbstractFacetFactoryTest {

    protected ObjectSupportFacetFactory facetFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new ObjectSupportFacetFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    protected void assertPicksUp(
            final int expectedSupportMethodCount,
            final FacetFactory facetFactory,
            final Class<?> type,
            final ProgrammingModelConstants.ObjectSupportMethod supportMethodEnum,
            final Class<? extends Facet> facetType) {

        // when
        facetFactory.process(ProcessClassContext
                .forTesting(type, methodRemover, facetedMethod));

        val supportMethods = supportMethodEnum.getMethodNames()
                .map(methodName->findMethod(type, methodName));

        Assertions.assertEquals(expectedSupportMethodCount, supportMethods.size());

        val facet = facetedMethod.getFacet(facetType);
        assertNotNull(facet);
        assertTrue(facet instanceof ImperativeFacet);
        val imperativeFacet = (ImperativeFacet)facet;

        supportMethods.forEach(method->{
            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
            assertTrue(imperativeFacet.getMethods().contains(method));
        });

    }

}
