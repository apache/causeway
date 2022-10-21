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
package org.apache.causeway.core.metamodel.facets.actions.action;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;

import static org.apache.causeway.core.metamodel.commons.matchers.CausewayMatchers.classEqualTo;

import lombok.val;

class ActionAnnotationFacetFactoryTest_Invocation
extends ActionAnnotationFacetFactoryTest {

    private void processInvocation(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processInvocation(processMethodContext, actionIfAny);
    }

    @Test
    void withPostsActionInvokedEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}

            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        super.metaModelContext.getConfiguration()
        .getApplib().getAnnotation().getAction().getDomainEvent().setPostForDefault(true);

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // expect
        expectRemoveMethod(actionMethod);

        // then
        final ActionDomainEventFacet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        assertNotNull(domainEventFacet);
        assertTrue(domainEventFacet instanceof ActionDomainEventFacetAbstract);
        final ActionDomainEventFacetAbstract domainEventFacetImpl = (ActionDomainEventFacetAbstract) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(invocationFacet);
        assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    void withActionInteractionEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}

            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // expect
        expectRemoveMethod(actionMethod);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        assertNotNull(domainEventFacet);
        assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
        final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl =
                (ActionDomainEventFacetForActionAnnotation) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(invocationFacet);

        assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl =
                (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    void withActionDomainEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}

            @Action(domainEvent= SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // expect
        expectRemoveMethod(actionMethod);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        assertNotNull(domainEventFacet);
        assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
        final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl = (ActionDomainEventFacetForActionAnnotation) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(invocationFacet);
        assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    void withDefaultEvent() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        super.metaModelContext.getConfiguration()
                .getApplib().getAnnotation().getAction().getDomainEvent().setPostForDefault(true);

        // when
        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processInvocation(facetFactory, processMethodContext);

        // expect
        expectRemoveMethod(actionMethod);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        assertNotNull(domainEventFacet);
        assertTrue(domainEventFacet instanceof ActionDomainEventFacetDefault);
        final ActionDomainEventFacetDefault domainEventFacetImpl = (ActionDomainEventFacetDefault) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(invocationFacet);
        assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromDefault);
        final ActionInvocationFacetForDomainEventFromDefault invocationFacetImpl = (ActionInvocationFacetForDomainEventFromDefault) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));
    }
}
