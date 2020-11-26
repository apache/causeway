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

package org.apache.isis.core.metamodel.facets.actions.layout;

import org.hamcrest.CoreMatchers;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;

import lombok.val;

public class ActionLayoutAnnotationFacetFactoryJunit4Test extends AbstractFacetFactoryJUnit4TestCase {

    ActionLayoutFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new ActionLayoutFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);
    }

    public static class Contributing extends ActionLayoutAnnotationFacetFactoryJunit4Test {

        @Test
        public void onDomainServiceForViewWithDefault() {

            // given
            @DomainService()
            class CustomerService {

                @ActionLayout()
                public String name() {
                    return "Joe";
                }
            }

            context.checking(new Expectations() {{

                allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
                will(returnValue(mockObjSpec));

                allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
                will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, NatureOfService.VIEW) {
                }));
            }});

            expectNoMethodsRemoved();

            facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

            // when
            facetFactory.process(new FacetFactory.ProcessMethodContext(
                    CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(NotContributedFacet.class);
            assertThat(facet, CoreMatchers.is(nullValue()));

        }

        @Test @Ignore
        public void testOnDomainServiceForViewWithAssociation() {

            // given
            @DomainService()
            class CustomerService {

                @Property
                public String name() {
                    return "Joe";
                }
            }

            context.checking(new Expectations() {{

                allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
                will(returnValue(mockObjSpec));

                allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
                will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, NatureOfService.VIEW) {
                }));
            }});

            expectNoMethodsRemoved();

            facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

            // when
            facetFactory.process(new FacetFactory.ProcessMethodContext(
                    CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

            // then
            val facet = facetedMethod.getFacet(NotContributedFacet.class);
            assertThat(facet, CoreMatchers.is(not(nullValue())));
//            assertThat(facet instanceof NotContributedFacetForActionLayoutAnnotation, CoreMatchers.is(true));
//            final NotContributedFacetForActionLayoutAnnotation facetImpl = (NotContributedFacetForActionLayoutAnnotation) facet;
            assertThat(facet.contributed(), CoreMatchers.equalTo(Contributed.AS_ASSOCIATION));

        }

        @Test @Ignore
        public void onDomainServiceForViewWithAction() {

            // given
            @DomainService()
            class CustomerService {

                @Action
                public String name() {
                    return "Joe";
                }
            }

            context.checking(new Expectations() {{

                allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
                will(returnValue(mockObjSpec));

                allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
                will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, NatureOfService.VIEW) {
                }));
            }});

            expectNoMethodsRemoved();

            facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

            // when
            facetFactory.process(new FacetFactory.ProcessMethodContext(
                    CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

            // then
            val facet = facetedMethod.getFacet(NotContributedFacet.class);
            assertThat(facet, CoreMatchers.is(not(nullValue())));
            //assertThat(facet instanceof NotContributedFacetForActionLayoutAnnotation, CoreMatchers.is(true));
            //final NotContributedFacetForActionLayoutAnnotation facetImpl = (NotContributedFacetForActionLayoutAnnotation) facet;
            assertThat(facet.contributed(), CoreMatchers.equalTo(Contributed.AS_ACTION));
        }

        @Test @Ignore
        public void onDomainServiceForViewWithNeither() {
            // given
            @DomainService()
            class CustomerService {

                @Programmatic
                public String name() {
                    return "Joe";
                }
            }

            context.checking(new Expectations() {{
                allowing(mockSpecificationLoader).loadSpecification(CustomerService.class);
                will(returnValue(mockObjSpec));

                allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
                will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, NatureOfService.VIEW) {
                }));
            }});

            expectNoMethodsRemoved();

            facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

            // when
            facetFactory.process(new FacetFactory.ProcessMethodContext(
                    CustomerService.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

            // then
            val facet = facetedMethod.getFacet(NotContributedFacet.class);
            assertThat(facet, CoreMatchers.is(not(nullValue())));
//            assertThat(facet instanceof NotContributedFacetForActionLayoutAnnotation, CoreMatchers.is(true));
//            final NotContributedFacetForActionLayoutAnnotation facetImpl = (NotContributedFacetForActionLayoutAnnotation) facet;
            assertThat(facet.contributed(), CoreMatchers.equalTo(Contributed.AS_NEITHER));

        }

        @Test
        public void onDomainObjectIsIgnored() {

            // given
            @DomainObject
            class Customer {

                @Action
                public String name() {
                    return "Joe";
                }
            }

            context.checking(new Expectations() {{

                allowing(mockSpecificationLoader).loadSpecification(Customer.class);
                will(returnValue(mockObjSpec));

                allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
                will(returnValue(null));

                allowing(mockObjSpec).getFacet(MixinFacet.class);
                will(returnValue(null));
            }});

            expectNoMethodsRemoved();

            facetedMethod = FacetedMethod.createForAction(Customer.class, "name");

            // when
            facetFactory.process(new FacetFactory.ProcessMethodContext(
                    Customer.class, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(NotContributedFacet.class);
            assertThat(facet, CoreMatchers.is(nullValue()));
        }

    }

}
