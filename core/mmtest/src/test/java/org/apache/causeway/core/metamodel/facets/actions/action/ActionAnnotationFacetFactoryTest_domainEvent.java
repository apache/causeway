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
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract.EventTypeOrigin;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForAction;
import org.apache.causeway.core.metamodel.postprocessors.members.SynthesizeDomainEventsForMixinPostProcessor;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.impl._JUnitSupport;

import static org.apache.causeway.core.metamodel.commons.matchers.CausewayMatchers.classEqualTo;

import lombok.RequiredArgsConstructor;

class ActionAnnotationFacetFactoryTest_domainEvent
extends ActionAnnotationFacetFactoryTest {

    private void processDomainEvent(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        var actionIfAny = facetFactory.actionIfAny(processMethodContext);
        facetFactory.processDomainEvent(processMethodContext, actionIfAny);
    }

    private void assertHasActionDomainEventFacet(
            final FacetedMethod facetedMethod,
            final EventTypeOrigin eventTypeOrigin,
            final Class<? extends ActionDomainEvent<?>> eventType) {

        var domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        assertNotNull(domainEventFacet);
        assertTrue(domainEventFacet instanceof ActionDomainEventFacet);
        assertThat(domainEventFacet.getEventType(), classEqualTo(eventType));

        var invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(invocationFacet);
        assertTrue(invocationFacet instanceof ActionInvocationFacetForAction);
        var invocationFacetImpl = (ActionInvocationFacetForAction) invocationFacet;
        assertEquals(eventTypeOrigin, invocationFacetImpl.getEventTypeOrigin());
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(eventType));
    }

    /**
     * Asserts the event type observed <i>on the mixed-in member itself</i> (its local, object-type-specific
     * overlay), as opposed to the shared mixin faceted method. Used to verify per-mixee isolation.
     */
    private void assertMixedInActionHasEvent(
            final ObjectAction mixedInAct,
            final EventTypeOrigin eventTypeOrigin,
            final Class<? extends ActionDomainEvent<?>> eventType) {

        var domainEventFacet = mixedInAct.getFacet(ActionDomainEventFacet.class);
        assertNotNull(domainEventFacet);
        assertEquals(eventTypeOrigin, domainEventFacet.getEventTypeOrigin());
        assertThat(domainEventFacet.getEventType(), classEqualTo(eventType));

        var invocationFacet = mixedInAct.getFacet(ActionInvocationFacet.class);
        assertNotNull(invocationFacet);
        assertTrue(invocationFacet instanceof ActionInvocationFacetForAction);
        var invocationFacetImpl = (ActionInvocationFacetForAction) invocationFacet;
        assertEquals(eventTypeOrigin, invocationFacetImpl.getEventTypeOrigin());
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(eventType));
    }

    @Test
    void withActionDomainEvent_fallingBackToDefault() {

        @SuppressWarnings("unused")
        class Customer {
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processDomainEvent(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.DEFAULT, ActionDomainEvent.Default.class);
        });

    }

    @Test
    void withActionDomainEvent_annotatedOnMethod() {
        class Customer {
            class SomeActionDomainEvent extends ActionDomainEvent<Customer> {}
            @Action(domainEvent = SomeActionDomainEvent.class)
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processDomainEvent(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.ANNOTATED_MEMBER, Customer.SomeActionDomainEvent.class);
        });
    }

    @Test
    void withActionDomainEvent_annotatedOnType() {

        @DomainObject(actionDomainEvent = Customer.SomeActionDomainEvent.class)
        class Customer {
            class SomeActionDomainEvent extends ActionDomainEvent<Customer> {}
            @Action
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processDomainEvent(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.ANNOTATED_OBJECT, Customer.SomeActionDomainEvent.class);
        });
    }

    @Test
    void withActionDomainEvent_annotatedOnTypeAndMethod() {

        @DomainObject(actionDomainEvent = Customer.SomeActionDomainEvent1.class)
        class Customer {
            class SomeActionDomainEvent1 extends ActionDomainEvent<Customer> {}
            class SomeActionDomainEvent2 extends ActionDomainEvent<Customer> {}
            @Action(domainEvent = SomeActionDomainEvent2.class)
            public void someAction() {}
        }

        // given
        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processDomainEvent(facetFactory, processMethodContext);

            // expect
            assertMethodWasRemoved(findMethodExactOrFail(Customer.class, "someAction"));

            // then - the action annotation should win
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.ANNOTATED_MEMBER, Customer.SomeActionDomainEvent2.class);
        });
    }

    @Test
    void withActionDomainEvent_mixedIn_annotatedOnMethod() {
        var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

        // given
        class Customer {
            class SomeActionDomainEvent extends ActionDomainEvent<Customer> {}
        }
        @DomainObject(nature=Nature.MIXIN, mixinMethod = "act")
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        class Customer_someAction {
            final Customer mixee;
            @Action(domainEvent = Customer.SomeActionDomainEvent.class)
            public void act() { }
        }

        actionScenarioMixedIn(Customer.class, Customer_someAction.class,
                (processMethodContext, mixeeSpec, facetedMethod, mixedInAct)->{

            // when
            processDomainEvent(facetFactory, processMethodContext);
            postProcessor.postProcessAction(mixeeSpec, mixedInAct);

            // then
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.ANNOTATED_MEMBER, Customer.SomeActionDomainEvent.class);

        });
    }

    @Test
    void withActionDomainEvent_mixedIn_annotatedOnMixedInType() {
        var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

        // given
        class Customer {
            class SomeActionDomainEvent extends ActionDomainEvent<Customer> {}
        }
        @Action(domainEvent = Customer.SomeActionDomainEvent.class)
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        class Customer_someAction {
            final Customer mixee;
            @MemberSupport
            public void act() { }
        }

        actionScenarioMixedIn(Customer.class, Customer_someAction.class,
                (processMethodContext, mixeeSpec, facetedMethod, mixedInAct)->{

            // when
            processDomainEvent(facetFactory, processMethodContext);
            postProcessor.postProcessAction(mixeeSpec, mixedInAct);

            // then
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.ANNOTATED_MEMBER, Customer.SomeActionDomainEvent.class);

        });
    }

    @Test
    void withActionDomainEvent_mixedIn_annotatedOnMixeeType() {
        var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

        // given
        @DomainObject(actionDomainEvent = Customer.SomeActionDomainEvent.class)
        class Customer {
            class SomeActionDomainEvent extends ActionDomainEvent<Customer> {}
        }
        @Action
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        class Customer_someAction {
            final Customer mixee;
            @MemberSupport
            public void act() { }
        }

        actionScenarioMixedIn(Customer.class, Customer_someAction.class,
                (processMethodContext, mixeeSpec, facetedMethod, mixedInAct)->{

            // when
            processDomainEvent(facetFactory, processMethodContext);
            postProcessor.postProcessAction(mixeeSpec, mixedInAct);

            // then - the mixee's object-level default is installed as a per-mixee overlay on the mixed-in
            // member, while the shared mixin faceted method is left at its default (no shared mutation).
            assertMixedInActionHasEvent(mixedInAct,
                    EventTypeOrigin.ANNOTATED_OBJECT, Customer.SomeActionDomainEvent.class);
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.DEFAULT, ActionDomainEvent.Default.class);

        });
    }

    @Test
    void withActionDomainEvent_mixedIn_isolatedAcrossMixees() {
        var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

        // given: two mixees declaring different object-level action domain-event defaults ...
        @DomainObject(actionDomainEvent = Mixee1.Event1.class)
        class Mixee1 {
            class Event1 extends ActionDomainEvent<Object> {}
        }
        @DomainObject(actionDomainEvent = Mixee2.Event2.class)
        class Mixee2 {
            class Event2 extends ActionDomainEvent<Object> {}
        }
        // ... contributed by one shared, un-annotated mixin
        @Action
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        class SharedMixin {
            final Object mixee;
            @MemberSupport
            public void act() { }
        }

        actionScenarioMixedIn(Mixee1.class, SharedMixin.class,
                (processMethodContext, mixee1Spec, facetedMethod, mixedInAct1)->{

            // and: a second mixed-in action for Mixee2 layered over the SAME shared mixin faceted method
            var mixinSpec = getSpecificationLoader().loadSpecification(SharedMixin.class);
            var mixee2Spec = getSpecificationLoader().loadSpecification(Mixee2.class);
            var mixedInAct2 = _JUnitSupport.mixedInActionforMixinMain(mixee2Spec, mixinSpec, "act", facetedMethod);

            // when
            processDomainEvent(facetFactory, processMethodContext);
            postProcessor.postProcessAction(mixee1Spec, mixedInAct1);
            postProcessor.postProcessAction(mixee2Spec, mixedInAct2);

            // then: each mixee's mixed-in action posts ITS OWN object-level event ...
            assertMixedInActionHasEvent(mixedInAct1, EventTypeOrigin.ANNOTATED_OBJECT, Mixee1.Event1.class);
            assertMixedInActionHasEvent(mixedInAct2, EventTypeOrigin.ANNOTATED_OBJECT, Mixee2.Event2.class);

            // ... and the shared mixin faceted method is never mutated (no last-writer-wins pollution)
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.DEFAULT, ActionDomainEvent.Default.class);

        });
    }

    @Test
    void withActionDomainEvent_mixedIn_annotatedOnMixeeAndMixedInType() {
        var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

        // given
        @DomainObject(actionDomainEvent = Customer.SomeActionDomainEvent1.class)
        class Customer {
            class SomeActionDomainEvent1 extends ActionDomainEvent<Customer> {}
            class SomeActionDomainEvent2 extends ActionDomainEvent<Customer> {}
        }
        @Action(domainEvent = Customer.SomeActionDomainEvent2.class)
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        class Customer_someAction {
            final Customer mixee;
            @MemberSupport
            public void act() { }
        }

        actionScenarioMixedIn(Customer.class, Customer_someAction.class,
                (processMethodContext, mixeeSpec, facetedMethod, mixedInAct)->{

            // when
            processDomainEvent(facetFactory, processMethodContext);
            postProcessor.postProcessAction(mixeeSpec, mixedInAct);

            // then
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.ANNOTATED_MEMBER, Customer.SomeActionDomainEvent2.class);

        });
    }

    @Test
    void withActionDomainEvent_mixedIn_annotatedOnMixeeTypeAndMixedInMethod() {
        var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

        // given
        @DomainObject(actionDomainEvent = Customer.SomeActionDomainEvent1.class)
        class Customer {
            class SomeActionDomainEvent1 extends ActionDomainEvent<Customer> {}
            class SomeActionDomainEvent2 extends ActionDomainEvent<Customer> {}
        }
        @DomainObject(nature=Nature.MIXIN, mixinMethod = "act")
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        class Customer_someAction {
            final Customer mixee;
            @Action(domainEvent = Customer.SomeActionDomainEvent2.class)
            public void act() { }
        }

        actionScenarioMixedIn(Customer.class, Customer_someAction.class,
                (processMethodContext, mixeeSpec, facetedMethod, mixedInAct)->{

            // when
            processDomainEvent(facetFactory, processMethodContext);
            postProcessor.postProcessAction(mixeeSpec, mixedInAct);

            // then
            assertHasActionDomainEventFacet(facetedMethod,
                    EventTypeOrigin.ANNOTATED_MEMBER, Customer.SomeActionDomainEvent2.class);

        });
    }

}
