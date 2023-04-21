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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract.EventTypeOrigin;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForActionDomainEvent;

import static org.apache.causeway.core.metamodel.commons.matchers.CausewayMatchers.classEqualTo;

import lombok.val;

class ActionAnnotationFacetFactoryTest_Invocation
extends ActionAnnotationFacetFactoryTest {

    private void processInvocation(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processDomainEvent(processMethodContext, actionIfAny);
    }

    @Test
    void withPostsActionInvokedEvent() {

        class Customer {
            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}
            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {}
        }

        // given
        getConfiguration()
            .getApplib().getAnnotation().getAction().getDomainEvent().setPostForDefault(true);

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processInvocation(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then
            final ActionDomainEventFacet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof ActionDomainEventFacet);
            final ActionDomainEventFacet domainEventFacetImpl = domainEventFacet;
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(invocationFacet);
            assertTrue(invocationFacet instanceof ActionInvocationFacetForActionDomainEvent);
            final ActionInvocationFacetForActionDomainEvent invocationFacetImpl = (ActionInvocationFacetForActionDomainEvent) invocationFacet;
            assertEquals(EventTypeOrigin.ANNOTATED_MEMBER, invocationFacetImpl.getEventTypeOrigin());
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        });
    }

    @Test
    void withActionInteractionEvent() {

        class Customer {
            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}
            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processInvocation(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof ActionDomainEventFacet);
            final ActionDomainEventFacet domainEventFacetImpl = (ActionDomainEventFacet) domainEventFacet;
            assertTrue(domainEventFacetImpl.getEventTypeOrigin().isAnnotatedMember());
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(invocationFacet);

            assertTrue(invocationFacet instanceof ActionInvocationFacetForActionDomainEvent);
            final ActionInvocationFacetForActionDomainEvent invocationFacetImpl = (ActionInvocationFacetForActionDomainEvent) invocationFacet;
            assertEquals(EventTypeOrigin.ANNOTATED_MEMBER, invocationFacetImpl.getEventTypeOrigin());
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        });
    }

    @Test
    void withActionDomainEvent() {

        class Customer {
            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {}
            @Action(domainEvent= SomeActionInvokedDomainEvent.class)
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processInvocation(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof ActionDomainEventFacet);
            final ActionDomainEventFacet domainEventFacetImpl = (ActionDomainEventFacet) domainEventFacet;
            assertTrue(domainEventFacetImpl.getEventTypeOrigin().isAnnotatedMember());
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(invocationFacet);
            assertTrue(invocationFacet instanceof ActionInvocationFacetForActionDomainEvent);
            final ActionInvocationFacetForActionDomainEvent invocationFacetImpl = (ActionInvocationFacetForActionDomainEvent) invocationFacet;
            assertEquals(EventTypeOrigin.ANNOTATED_MEMBER, invocationFacetImpl.getEventTypeOrigin());
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        });
    }

    @Test
    void withDefaultEvent() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {}
        }

        // given
        getConfiguration()
            .getApplib().getAnnotation().getAction().getDomainEvent().setPostForDefault(true);

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processInvocation(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof ActionDomainEventFacet);
            final ActionDomainEventFacet domainEventFacetImpl = (ActionDomainEventFacet) domainEventFacet;
            assertTrue(domainEventFacetImpl.getEventTypeOrigin().isDefault());
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(invocationFacet);
            assertTrue(invocationFacet instanceof ActionInvocationFacetForActionDomainEvent);
            final ActionInvocationFacetForActionDomainEvent invocationFacetImpl = (ActionInvocationFacetForActionDomainEvent) invocationFacet;
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));
        });
    }
}
