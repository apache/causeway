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
package org.apache.isis.core.metamodel.facets.actions.action;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;

import static org.apache.isis.core.metamodel.commons.matchers.IsisMatchers.classEqualTo;

import lombok.val;

public class ActionAnnotationFacetFactoryTest_Invocation extends ActionAnnotationFacetFactoryTest {

    private void processInvocation(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processInvocation(processMethodContext, actionIfAny);
    }

    @Test
    public void withPostsActionInvokedEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}

            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        super.metaModelContext.getConfiguration()
        .getApplib().getAnnotation().getAction().getDomainEvent().setPostForDefault(true);

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // then
        final ActionDomainEventFacet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetAbstract);
        final ActionDomainEventFacetAbstract domainEventFacetImpl = (ActionDomainEventFacetAbstract) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);
        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    public void withActionInteractionEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}

            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
        final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl =
                (ActionDomainEventFacetForActionAnnotation) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);

        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl =
                (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    public void withActionDomainEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}

            @Action(domainEvent= SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
        final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl = (ActionDomainEventFacetForActionAnnotation) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);
        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    public void withDefaultEvent() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        super.metaModelContext.getConfiguration()
                .getApplib().getAnnotation().getAction().getDomainEvent().setPostForDefault(true);

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetDefault);
        final ActionDomainEventFacetDefault domainEventFacetImpl = (ActionDomainEventFacetDefault) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);
        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromDefault);
        final ActionInvocationFacetForDomainEventFromDefault invocationFacetImpl = (ActionInvocationFacetForDomainEventFromDefault) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));
    }
}