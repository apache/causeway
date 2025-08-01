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
package org.apache.causeway.core.metamodel.facets.object.domainobject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.choices.ChoicesFacetFromBoundedAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;

class ChoicesFacetFromBoundedAnnotationFactoryTest
extends FacetFactoryTestAbstract {

    private DomainObjectAnnotationFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new DomainObjectAnnotationFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    @Test
    void boundedAnnotationPickedUpOnClass() {
        @DomainObject(bounding = Bounding.BOUNDED)
        class Customer {
        }
        objectScenario(Customer.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.processBounded(processClassContext.synthesizeOnType(DomainObject.class), processClassContext);
            //then
            final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ChoicesFacetFromBoundedAbstract);

            assertNoMethodsRemoved();
        });

    }
}
