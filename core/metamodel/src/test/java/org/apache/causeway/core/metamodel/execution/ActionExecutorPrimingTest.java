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
package org.apache.causeway.core.metamodel.execution;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForMixedInPropertyOrCollection;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.priming.PrimingService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ActionExecutorPrimingTest {

    @Test
    void primesOwnerWithArgumentsBeforeInvokingAction() throws Exception {
        final List<String> calls = new ArrayList<>();
        final RegularTarget targetPojo = new RegularTarget(calls);
        final RecordingPrimingService primingService = new RecordingPrimingService(calls);
        final ActionExecutor executor = executor(
                targetPojo,
                targetPojo,
                RegularTarget.class.getDeclaredMethod("approve", String.class),
                "test.Invoice",
                "approve",
                Can.of(managed("argument", null)),
                nonPostableFacet(ActionInvocationFacetAbstract.class),
                primingService,
                Stream.empty());

        final Object result = executor.execute(mock(ActionInvocation.class));

        assertEquals("approved", result);
        assertEquals(Arrays2.of("prime:test.Invoice#approve:argument", "action:argument"), calls);
    }

    @Test
    void contributedActionPrimesMixeeRatherThanMixin() throws Exception {
        final List<String> calls = new ArrayList<>();
        final Object mixee = new Object();
        final MixinTarget mixin = new MixinTarget(calls);
        final RecordingPrimingService primingService = new RecordingPrimingService(calls);
        final ObjectAction mixinImplementationAction = action(
                "test.Invoice_send", "act");
        when(mixinImplementationAction.isDeclaredOnMixin()).thenReturn(true);
        final ActionExecutor executor = executor(
                mixee,
                mixin,
                MixinTarget.class.getDeclaredMethod("invoke"),
                "test.Invoice",
                mixinImplementationAction,
                Can.empty(),
                nonPostableFacet(ActionInvocationFacetAbstract.class),
                primingService,
                Stream.empty());

        executor.execute(mock(ActionInvocation.class));

        assertSame(mixee, primingService.target);
        assertEquals(Arrays2.of("prime:test.Invoice#send", "mixin-action"), calls);
    }

    @Test
    void syntheticAssociationAccessDoesNotPrime() throws Exception {
        final List<String> calls = new ArrayList<>();
        final RegularTarget targetPojo = new RegularTarget(calls);
        final RecordingPrimingService primingService = new RecordingPrimingService(calls);
        final ActionInvocationFacetForMixedInPropertyOrCollection facet =
                nonPostableFacet(ActionInvocationFacetForMixedInPropertyOrCollection.class);
        final ObjectAssociation association = mock(
                ObjectAssociation.class,
                withSettings().extraInterfaces(MixedInMember.class));
        final ObjectAction owningAction = action("test.Invoice_mixin", "property");
        when(((MixedInMember) association).hasMixinAction(owningAction)).thenReturn(true);
        final ActionExecutor executor = executor(
                targetPojo,
                targetPojo,
                RegularTarget.class.getDeclaredMethod("read"),
                "test.Invoice",
                owningAction,
                Can.empty(),
                facet,
                primingService,
                Stream.of(association));

        executor.execute(mock(ActionInvocation.class));

        assertEquals(Collections.singletonList("association"), calls);
    }

    @Test
    void primerFailurePreventsActionInvocationAndPropagates() throws Exception {
        final List<String> calls = new ArrayList<>();
        final RegularTarget targetPojo = new RegularTarget(calls);
        final PrimingService failing = new RecordingPrimingService(calls) {
            @Override
            public void primeAction(
                    final ObjectSpecification targetSpecification,
                    final String actionLogicalName,
                    final Object target,
                    final List<Object> arguments) {
                throw new IllegalStateException("prime failed");
            }
        };
        final ActionExecutor executor = executor(
                targetPojo,
                targetPojo,
                RegularTarget.class.getDeclaredMethod("approve", String.class),
                "test.Invoice",
                "approve",
                Can.of(managed("argument", null)),
                nonPostableFacet(ActionInvocationFacetAbstract.class),
                failing,
                Stream.empty());

        final IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> executor.execute(mock(ActionInvocation.class)));

        assertEquals("prime failed", failure.getMessage());
        assertEquals(Collections.emptyList(), calls);
    }

    private static ActionExecutor executor(
            final Object ownerPojo,
            final Object targetPojo,
            final Method method,
            final String ownerLogicalType,
            final String actionLogicalName,
            final Can<ManagedObject> arguments,
            final ActionInvocationFacetAbstract facet,
            final PrimingService primingService,
            final Stream<ObjectAssociation> associations) {
        return executor(
                ownerPojo,
                targetPojo,
                method,
                ownerLogicalType,
                action(ownerLogicalType, actionLogicalName),
                arguments,
                facet,
                primingService,
                associations);
    }

    private static ActionExecutor executor(
            final Object ownerPojo,
            final Object targetPojo,
            final Method method,
            final String ownerLogicalType,
            final ObjectAction owningAction,
            final Can<ManagedObject> arguments,
            final ActionInvocationFacetAbstract facet,
            final PrimingService primingService,
            final Stream<ObjectAssociation> associations) {

        final ObjectSpecification ownerSpecification = mock(ObjectSpecification.class);
        when(ownerSpecification.logicalTypeName()).thenReturn(ownerLogicalType);
        when(ownerSpecification.streamAssociations(any())).thenReturn(associations);
        final ManagedObject owner = managed(ownerPojo, ownerSpecification);
        final ManagedObject target = managed(targetPojo, null);
        final InteractionHead head;
        if(owningAction.isDeclaredOnMixin()) {
            final ActionInteractionHead actionHead = mock(ActionInteractionHead.class);
            final ObjectAction domainFacingAction = action(ownerLogicalType, "send");
            when(actionHead.getMetaModel()).thenReturn(domainFacingAction);
            head = actionHead;
        } else {
            head = mock(InteractionHead.class);
        }
        when(head.getOwner()).thenReturn(owner);
        when(head.getTarget()).thenReturn(target);

        final ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.lookupService(PrimingService.class))
                .thenReturn(Optional.ofNullable(primingService));
        final ServiceInjector serviceInjector = mock(ServiceInjector.class);
        when(serviceInjector.injectServicesInto(any())).thenAnswer(invocation -> invocation.getArgument(0));
        final MetaModelContext metaModelContext = mock(MetaModelContext.class);
        when(metaModelContext.getServiceRegistry()).thenReturn(serviceRegistry);
        when(metaModelContext.getServiceInjector()).thenReturn(serviceInjector);

        return new ActionExecutor(
                metaModelContext,
                mock(FacetHolder.class),
                InteractionInitiatedBy.USER,
                owningAction,
                _MethodFacades.testing.regular(method),
                head,
                arguments,
                facet);
    }

    private static <T extends ActionInvocationFacetAbstract> T nonPostableFacet(
            final Class<T> facetType) throws Exception {
        final T facet = mock(facetType);
        @SuppressWarnings("unchecked")
        final DomainEventHolder<ActionDomainEvent<?>> holder = mock(DomainEventHolder.class);
        when(holder.isPostable()).thenReturn(false);
        final java.lang.reflect.Field field = DomainEventFacetAbstract.class
                .getDeclaredField("domainEventHolder");
        field.setAccessible(true);
        field.set(facet, holder);
        return facet;
    }

    private static ManagedObject managed(
            final Object pojo,
            final ObjectSpecification specification) {
        final ManagedObject managedObject = mock(ManagedObject.class);
        when(managedObject.getPojo()).thenReturn(pojo);
        when(managedObject.objSpec()).thenReturn(specification);
        when(managedObject.getSpecialization()).thenReturn(ManagedObject.Specialization.ENTITY);
        when(managedObject.getBookmark()).thenReturn(Optional.empty());
        return managedObject;
    }

    private static ObjectAction action(
            final String logicalTypeName,
            final String actionLogicalName) {
        final ObjectAction action = mock(ObjectAction.class);
        when(action.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(
                LogicalType.eager(Object.class, logicalTypeName),
                actionLogicalName));
        return action;
    }

    private static class RecordingPrimingService implements PrimingService {
        private final List<String> calls;
        private Object target;

        private RecordingPrimingService(final List<String> calls) {
            this.calls = calls;
        }

        @Override
        public void primeAction(
                final ObjectSpecification targetSpecification,
                final String actionLogicalName,
                final Object target,
                final List<Object> arguments) {
            this.target = target;
            final String argument = arguments.isEmpty() ? "" : ":" + arguments.get(0);
            calls.add("prime:" + targetSpecification.logicalTypeName()
                    + "#" + actionLogicalName + argument);
        }

        @Override
        public void primeView(
                final ObjectSpecification targetSpecification,
                final Object target) {
        }
    }

    private static final class RegularTarget {
        private final List<String> calls;

        private RegularTarget(final List<String> calls) {
            this.calls = calls;
        }

        public String approve(final String argument) {
            calls.add("action:" + argument);
            return "approved";
        }

        public String read() {
            calls.add("association");
            return "read";
        }
    }

    private static final class MixinTarget {
        private final List<String> calls;

        private MixinTarget(final List<String> calls) {
            this.calls = calls;
        }

        public String invoke() {
            calls.add("mixin-action");
            return "sent";
        }
    }

    private static final class Arrays2 {
        private static <T> List<T> of(final T first, final T second) {
            return java.util.Arrays.asList(first, second);
        }
    }
}
