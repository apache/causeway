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
package org.apache.causeway.core.metamodel.facets.actions.notinservicemenu.derived;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.actions.notinservicemenu.WebApiOnlyActionFacet;

class WebApiOnlyActionFacetFromDomainServiceFacetFactoryTest
extends FacetFactoryTestAbstract {

    private NotInServiceMenuFacetFromDomainServiceFacetFactory facetFactory;

    @BeforeEach
    public void setUp() throws Exception {
        facetFactory = new NotInServiceMenuFacetFromDomainServiceFacetFactory(getMetaModelContext());
    }

    @Test
    public void whenRest() throws Exception {

        // given
        @DomainService(nature = NatureOfService.WEB_API)
        class CustomerService {
            @SuppressWarnings("unused")
            public String name() { return "Joe"; }
        }

        actionScenario(CustomerService.class, "name", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.lookupNonFallbackFacet(WebApiOnlyActionFacet.class).orElse(null);
            assertNotNull(facet);
            assertThat(facet instanceof WebApiOnlyActionFacetFromDomainServiceFacet, is(true));
            final WebApiOnlyActionFacetFromDomainServiceFacet facetDerivedFromDomainServiceFacet = (WebApiOnlyActionFacetFromDomainServiceFacet) facet;
            assertEquals(NatureOfService.WEB_API, facetDerivedFromDomainServiceFacet.getNatureOfService());
            assertNoMethodsRemoved();
        });

    }

    @Test
    public void whenView() throws Exception {

        // given
        @DomainService()
        class CustomerService {
            @SuppressWarnings("unused")
            public String name() { return "Joe"; }
        }

        actionScenario(CustomerService.class, "name", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.lookupNonFallbackFacet(WebApiOnlyActionFacet.class).orElse(null);
            assertThat(facet, is(nullValue()));
            assertNoMethodsRemoved();
        });
    }

    @Test
    public void whenMenu() throws Exception {

        // given
        @DomainService()
        class CustomerService {
            @SuppressWarnings("unused")
            public String name() { return "Joe"; }
        }

        actionScenario(CustomerService.class, "name", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.lookupNonFallbackFacet(WebApiOnlyActionFacet.class).orElse(null);
            assertThat(facet, is(nullValue()));
            assertNoMethodsRemoved();
        });
    }

    @Test
    public void whenNone() throws Exception {

        // given
        class CustomerService {
            @SuppressWarnings("unused")
            public String name() { return "Joe"; }
        }

        actionScenario(CustomerService.class, "name", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.lookupNonFallbackFacet(WebApiOnlyActionFacet.class).orElse(null);
            assertThat(facet, is(nullValue()));
            assertNoMethodsRemoved();
        });
    }

}