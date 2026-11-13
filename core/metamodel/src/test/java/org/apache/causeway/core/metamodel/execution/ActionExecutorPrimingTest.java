/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.core.metamodel.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForMixedInPropertyOrCollection;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.priming.PrimingService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import io.micrometer.observation.ObservationRegistry;

class ActionExecutorPrimingTest {
    @Test void nonEventActionPrimesBeforeMethodWithEffectiveArguments() throws Exception {
        var f = new Fixture();
        var result = f.executor(InteractionInitiatedBy.USER, false, false).executeWithExecutingEvents(0, f.invocation());
        assertEquals("approved", result);
        assertEquals(List.of("prime:approve:argument", "action:argument"), f.calls);
    }

    @Test void executingEventAdjustmentsReachPrimerBeforeMethodAndExecutedEvent() throws Exception {
        var f = new Fixture();
        var event = mock(ActionDomainEvent.Default.class);
        when(event.getArguments()).thenReturn(List.of("adjusted"));
        when(event.getReturnValue()).thenReturn("approved");
        var helper = mock(DomainEventHelper.class);
        when(f.context.domainEventHelper()).thenReturn(helper);
        doAnswer(call -> { f.calls.add("executing"); return event; }).when(helper).postEventForAction(
                eq(AbstractDomainEvent.Phase.EXECUTING), org.mockito.ArgumentMatchers.<Class<? extends ActionDomainEvent<?>>>any(),
                same(f.action), same(f.action), same(f.head), any(), isNull());
        doAnswer(call -> { f.calls.add("executed"); return null; }).when(helper).postEventForAction(
                eq(AbstractDomainEvent.Phase.EXECUTED), same(event), same(f.action), same(f.action), same(f.head), any(), eq("approved"));
        var param = mock(ObjectActionParameter.class);
        when(param.isSingular()).thenReturn(true);
        when(param.getParameterIndex()).thenReturn(0);
        var stringSpec = specification(String.class);
        when(param.getElementType()).thenReturn(stringSpec);
        when(f.action.getParameters()).thenReturn(Can.of(param));
        f.executor(InteractionInitiatedBy.USER, true, false).executeWithExecutingEvents(0, f.invocation());
        assertEquals(List.of("executing", "prime:approve:adjusted", "action:adjusted", "executed"), f.calls);
    }

    @Test void mixinPrimerReceivesDomainOwnerAndContributedNameWithoutChangingInvocation() throws Exception {
        var f = new Fixture();
        var mixin = new Mixin(f.calls);
        var mixinAdapter = managed(mixin);
        when(f.head.target()).thenReturn(mixinAdapter);
        var invoked = Identifier.actionIdentifier(LogicalType.eager(Mixin.class, "implementation.Invoice_send"), "act", String.class);
        when(f.action.getFeatureIdentifier()).thenReturn(invoked);
        var domain = Identifier.actionIdentifier(LogicalType.eager(Target.class, "demo.Invoice"), "send", String.class);
        var invocation = new ActionInvocation(mock(Interaction.class), invoked, domain, mixin, List.of("argument"), ActionInvocation.RuleChecking.CHECKED);
        var executor = f.executor(InteractionInitiatedBy.USER, false, false);
        executor = new ActionExecutor(f.context, f.holder, InteractionInitiatedBy.USER, f.action,
                _MethodFacades.testing.regular(Mixin.class.getMethod("act", String.class)), f.head, executor.arguments(),
                executor.actionInvocationFacetAbstract(), executor.observationProvider());
        executor.executeWithExecutingEvents(0, invocation);
        assertEquals(List.of("prime:send:argument", "mixin:argument"), f.calls);
        assertSame(invoked, invocation.getLogicalMemberIdentifier());
    }

    @Test void nestedOrdinaryExecutionsEachPrimeIndependently() throws Exception {
        var f = new Fixture();
        var nested = new Fixture();
        var nestedExecutor = nested.executor(InteractionInitiatedBy.FRAMEWORK, false, false);
        f.target.nested = () -> nestedExecutor.executeWithExecutingEvents(1, nested.invocation());
        f.executor(InteractionInitiatedBy.USER, false, false).executeWithExecutingEvents(0, f.invocation());
        assertEquals(List.of("prime:approve:argument", "action:argument"), f.calls);
        assertEquals(List.of("prime:approve:argument", "action:argument"), nested.calls);
    }

    @Test void passThroughAndSyntheticAssociationDoNotPrime() throws Exception {
        for (boolean synthetic : List.of(false, true)) {
            var f = new Fixture();
            f.executor(synthetic ? InteractionInitiatedBy.USER : InteractionInitiatedBy.PASS_THROUGH, false, synthetic)
                    .executeWithExecutingEvents(0, f.invocation());
            assertEquals(List.of("action:argument"), f.calls);
            verifyNoInteractions(f.priming);
        }
    }

    @Test void absentPrimingServiceDoesNotChangeExecution() throws Exception {
        var f = new Fixture();
        when(f.holder.lookupService(PrimingService.class)).thenReturn(Optional.empty());
        f.executor(InteractionInitiatedBy.USER, false, false).executeWithExecutingEvents(0, f.invocation());
        assertEquals(List.of("action:argument"), f.calls);
    }

    @Test void primerFailureEscapesIdenticallyAndPreventsMethod() throws Exception {
        var f = new Fixture();
        var failure = new IllegalStateException("primer failed");
        doThrow(failure).when(f.priming).primeAction(any(), any(), any(), any());
        var executor = f.executor(InteractionInitiatedBy.USER, false, false);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> executor.executeWithExecutingEvents(0, f.invocation())));
        assertTrue(f.calls.isEmpty());
    }

    private static ObjectSpecification specification(final Class<?> type) {
        var spec = mock(ObjectSpecification.class, RETURNS_DEEP_STUBS);
        doReturn(type).when(spec).correspondingClass();
        when(spec.getSpecificationLoader().specForType(type)).thenReturn(Optional.of(spec));
        return spec;
    }
    private static ManagedObject managed(final Object value) { return ManagedObject.other(specification(value.getClass()), value); }

    public static final class Target {
        final List<String> calls;
        Runnable nested = () -> { };
        Target(final List<String> calls) { this.calls = calls; }
        public String approve(final String value) { calls.add("action:" + value); nested.run(); return "approved"; }
    }
    public static final class Mixin {
        final List<String> calls;
        Mixin(final List<String> calls) { this.calls = calls; }
        public String act(final String value) { calls.add("mixin:" + value); return "sent"; }
    }
    private static final class Fixture {
        final List<String> calls = new ArrayList<>();
        final Target target = new Target(calls);
        final PrimingService priming = mock(PrimingService.class);
        final FacetHolder holder = mock(FacetHolder.class);
        final ExecutionContext context = mock(ExecutionContext.class);
        final ObjectAction action = mock(ObjectAction.class);
        final InteractionHead head = mock(InteractionHead.class);
        final Identifier id = Identifier.actionIdentifier(LogicalType.eager(Target.class, "demo.Invoice"), "approve", String.class);
        Fixture() {
            var owner = managed(target);
            when(head.owner()).thenReturn(owner);
            when(head.target()).thenReturn(owner);
            when(action.getFeatureIdentifier()).thenReturn(id);
            when(holder.lookupService(PrimingService.class)).thenReturn(Optional.of(priming));
            var injector = mock(ServiceInjector.class);
            when(injector.injectServicesInto(any())).thenAnswer(call -> call.getArgument(0));
            when(holder.getServiceInjector()).thenReturn(injector);
            doAnswer(call -> {
                assertSame(target, call.getArgument(2));
                calls.add("prime:" + call.getArgument(1) + ":" + ((List<?>)call.getArgument(3)).get(0));
                return null;
            }).when(priming).primeAction(any(), any(), any(), any());
        }
        ActionInvocation invocation() {
            return new ActionInvocation(mock(Interaction.class), id, target, List.of("argument"));
        }
        ActionExecutor executor(final InteractionInitiatedBy initiatedBy, final boolean postable, final boolean synthetic) throws Exception {
            ActionInvocationFacetAbstract facet = synthetic ? mock(ActionInvocationFacetForMixedInPropertyOrCollection.class)
                    : mock(ActionInvocationFacetAbstract.class);
            when(facet.isPostable()).thenReturn(postable);
            return new ActionExecutor(context, holder, initiatedBy, action,
                    _MethodFacades.testing.regular(Target.class.getMethod("approve", String.class)), head,
                    Can.of(managed("argument")), facet,
                    new CausewayObservationIntegration(ObservationRegistry.NOOP).provider(ActionExecutor.class));
        }
    }
}
