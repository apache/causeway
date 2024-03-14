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
package org.apache.causeway.core.metamodel.facets.object.domainservice.annotation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.logicaltype.AliasedFacet;

class DomainServiceFacetAnnotationFactoryTest
extends FacetFactoryTestAbstract {

    private DomainServiceFacetAnnotationFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new DomainServiceFacetAnnotationFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    @Test
    void aggregatedAnnotationPickedUpOnClass() {

        @DomainService(aliased = "Test")
        class Customers {
        }

        objectScenario(Customers.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            var facet = facetHolder.getFacet(AliasedFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof AliasedFacetForDomainServiceAnnotation);

            assertNoMethodsRemoved();
        });

    }

}
