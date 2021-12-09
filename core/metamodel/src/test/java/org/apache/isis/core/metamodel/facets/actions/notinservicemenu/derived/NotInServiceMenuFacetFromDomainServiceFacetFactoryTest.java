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
package org.apache.isis.core.metamodel.facets.actions.notinservicemenu.derived;

import java.util.Optional;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacetAbstract;

@SuppressWarnings("unused")
public class NotInServiceMenuFacetFromDomainServiceFacetFactoryTest
extends AbstractFacetFactoryJUnit4TestCase {

    private NotInServiceMenuFacetFromDomainServiceFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new NotInServiceMenuFacetFromDomainServiceFacetFactory(metaModelContext);
    }

    @Test
    public void whenRest() throws Exception {

        // given
        @DomainService(nature = NatureOfService.REST)
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).lookupNonFallbackFacet(DomainServiceFacet.class);
            will(returnValue(Optional.of(new DomainServiceFacetAbstract(mockObjSpec, NatureOfService.REST) {
            })));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod
                .createForAction(metaModelContext, CustomerService.class, "name");

        // when
        facetFactory.process(ProcessMethodContext
                .forTesting(CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.lookupNonFallbackFacet(NotInServiceMenuFacet.class).orElse(null);
        assertNotNull(facet);
        assertThat(facet instanceof NotInServiceMenuFacetFromDomainServiceFacet, is(true));
        final NotInServiceMenuFacetFromDomainServiceFacet facetDerivedFromDomainServiceFacet = (NotInServiceMenuFacetFromDomainServiceFacet) facet;
        assertEquals(NatureOfService.REST, facetDerivedFromDomainServiceFacet.getNatureOfService());
    }

    @Test
    public void whenView() throws Exception {

        // given
        @DomainService()
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).lookupNonFallbackFacet(DomainServiceFacet.class);
            will(returnValue(Optional.of(new DomainServiceFacetAbstract(mockObjSpec, NatureOfService.VIEW) {
            })));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod
                .createForAction(metaModelContext, CustomerService.class, "name");

        // when
        facetFactory.process(ProcessMethodContext
                .forTesting(CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.lookupNonFallbackFacet(NotInServiceMenuFacet.class).orElse(null);
        assertThat(facet, is(nullValue()));
    }

    @Test
    public void whenMenu() throws Exception {

        // given
        @DomainService()
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).lookupNonFallbackFacet(DomainServiceFacet.class);
            will(returnValue(Optional.of(new DomainServiceFacetAbstract(mockObjSpec, NatureOfService.VIEW) {
            })));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod
                .createForAction(metaModelContext, CustomerService.class, "name");

        // when
        facetFactory.process(ProcessMethodContext
                .forTesting(CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.lookupNonFallbackFacet(NotInServiceMenuFacet.class).orElse(null);
        assertThat(facet, is(nullValue()));
    }

    @Test
    public void whenNone() throws Exception {

        // given
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).lookupNonFallbackFacet(DomainServiceFacet.class);
            will(returnValue(Optional.empty()));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod
                .createForAction(metaModelContext, CustomerService.class, "name");

        // when
        facetFactory.process(ProcessMethodContext
                .forTesting(CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.lookupNonFallbackFacet(NotInServiceMenuFacet.class).orElse(null);
        assertThat(facet, is(nullValue()));
    }

}