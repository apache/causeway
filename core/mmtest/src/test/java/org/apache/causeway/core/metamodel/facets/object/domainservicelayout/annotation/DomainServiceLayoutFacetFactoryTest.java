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
package org.apache.causeway.core.metamodel.facets.object.domainservicelayout.annotation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.causeway.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacetFactory;

class DomainServiceLayoutFacetFactoryTest
extends FacetFactoryTestAbstract {

    private DomainServiceLayoutFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new DomainServiceLayoutFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    @Test
    void annotationPickedUpOnClass() {
        @DomainService
        @DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.SECONDARY)
        class Customers {
        }

        objectScenario(Customers.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
            DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
            //assertThat(domainServiceLayoutFacet.getMenuOrder(), is("123"));
            assertThat(domainServiceLayoutFacet.getMenuBar(), is(DomainServiceLayout.MenuBar.SECONDARY));

            assertNoMethodsRemoved();
        });
    }

    @Test
    void domainServiceAnnotationPickedUpOnClass() {
        @DomainService
        class Customers {
        }

        objectScenario(Customers.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
            DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
            assertNotNull(domainServiceLayoutFacet);

            assertNoMethodsRemoved();
        });
    }

    @Test
    void domainServiceAndDomainServiceLayoutAnnotationWhenCompatiblePickedUpOnClass() {
        @DomainService//(menuOrder = "123")
        @DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.SECONDARY)
        class Customers {
        }

        objectScenario(Customers.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
            DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
            //assertThat(domainServiceLayoutFacet.getMenuOrder(), is("123"));
            assertThat(domainServiceLayoutFacet.getMenuBar(), is(DomainServiceLayout.MenuBar.SECONDARY));

            assertNoMethodsRemoved();
        });
    }

    @Test
    void domainServiceAndDomainServiceLayoutAnnotation_takes_the_minimum() {
        @DomainService//(menuOrder = "1")
        @DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.SECONDARY)
        class Customers {
        }

        objectScenario(Customers.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
            DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
            //assertThat(domainServiceLayoutFacet.getMenuOrder(), is("1"));
            assertThat(domainServiceLayoutFacet.getMenuBar(), is(DomainServiceLayout.MenuBar.SECONDARY));

            assertNoMethodsRemoved();
        });

    }

}
