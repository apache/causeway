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
package org.apache.causeway.core.runtimeservices.executor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.execution.ActionExecutor;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberExecutorServiceDefaultObservationTest {

    @Test
    void actionObservationHasStableNameTagsAndRootParent() throws Exception {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final ManagedObject expected = mock(ManagedObject.class);
        final MemberExecutorServiceDefault service = newService(registry);
        final ActionExecutor actionExecutor = actionExecutor("succeed", expected);
        final Observation root = Observation.start("test root", registry);

        final ManagedObject actual;
        try (Observation.Scope ignored = root.openScope()) {
            actual = service.invokeAction(actionExecutor);
        } finally {
            root.stop();
        }

        assertSame(expected, actual);
        assertNull(registry.getCurrentObservation());
        Approvals.verify(report(
                "Action observation name, metadata, and root parentage",
                List.of(
                        "The action uses one stable operation name.",
                        "The action identifier is an explicit low-cardinality attribute.",
                        "The initiation mode is an explicit low-cardinality attribute.",
                        "The action observation is a child of the current root observation.",
                        "Action arguments, results, targets, users, and tenancy values are not captured."),
                handler.events));
    }

    @Test
    void actionFailureIsRecordedAndRethrown() throws Exception {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final MemberExecutorServiceDefault service = newService(registry);

        final RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.invokeAction(actionExecutor("fail", null)));
        handler.record("failure returned to caller: "
                + thrown.getClass().getSimpleName() + ": " + thrown.getMessage());
        handler.record("current observation after failure: "
                + (registry.getCurrentObservation() == null ? "<none>" : "unexpected"));

        Approvals.verify(report(
                "Action observation failure",
                List.of(
                        "A propagated action failure reaches the observation handler.",
                        "The failure continues to the caller.",
                        "The action scope closes before the observation stops.",
                        "No current observation remains after failure."),
                handler.events));
    }

    @Test
    void inactiveRegistryLeavesActionBehaviorUnchanged() throws Exception {
        final ManagedObject expected = mock(ManagedObject.class);
        final ObservationRegistry registry = ObservationRegistry.NOOP;
        final MemberExecutorServiceDefault service = newService(registry);

        final ManagedObject actual = service.invokeAction(actionExecutor("succeed", expected));

        assertSame(expected, actual);
        assertNull(registry.getCurrentObservation());
    }

    private static ObservationRegistry registryWith(final RecordingHandler handler) {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    private static MemberExecutorServiceDefault newService(final ObservationRegistry registry) {
        @SuppressWarnings("unchecked")
        final Provider<CommandPublisher> commandPublisherProvider = mock(Provider.class);
        return new MemberExecutorServiceDefault(
                null,
                mock(CausewayConfiguration.class),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                commandPublisherProvider,
                new CausewayObservationIntegration(registry));
    }

    private static ActionExecutor actionExecutor(
            final String methodName,
            final ManagedObject adaptedResult) throws Exception {
        final InvocationTarget targetPojo = new InvocationTarget();
        final ManagedObject target = mock(ManagedObject.class);
        when(target.getPojo()).thenReturn(targetPojo);

        final InteractionHead head = mock(InteractionHead.class);
        when(head.getTarget()).thenReturn(target);

        final ObjectManager objectManager = mock(ObjectManager.class);
        when(objectManager.adapt("ok")).thenReturn(adaptedResult);
        final FacetHolder facetHolder = mock(FacetHolder.class);
        when(facetHolder.getObjectManager()).thenReturn(objectManager);

        final ObjectAction owningAction = mock(ObjectAction.class);
        when(owningAction.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(
                LogicalType.eager(InvocationTarget.class, "demo.Customer"),
                "updateName"));

        final Method method = InvocationTarget.class.getDeclaredMethod(methodName);
        return new ActionExecutor(
                mock(MetaModelContext.class),
                facetHolder,
                InteractionInitiatedBy.PASS_THROUGH,
                owningAction,
                _MethodFacades.testing.regular(method),
                head,
                Can.empty(),
                mock(ActionInvocationFacetAbstract.class));
    }

    private static String report(
            final String scenario,
            final List<String> checks,
            final List<String> events) {
        final List<String> lines = new ArrayList<>();
        lines.add("Scenario: " + scenario);
        lines.add("");
        lines.add("This approval checks:");
        checks.forEach(check -> lines.add("- " + check));
        lines.add("");
        lines.add("Observed lifecycle:");
        for (int i = 0; i < events.size(); i++) {
            lines.add((i + 1) + ". " + events.get(i));
        }
        return String.join("\n", lines);
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> events = new ArrayList<>();

        @Override
        public void onStart(final Observation.Context context) {
            final String parentName = context.getParentObservation() != null
                    ? context.getParentObservation().getContextView().getName()
                    : "<none>";
            record("onStart(name=" + context.getName()
                    + ", parent=" + parentName
                    + ", causeway.action.id=" + value(context, "causeway.action.id")
                    + ", causeway.execution.initiatedBy="
                    + value(context, "causeway.execution.initiatedBy") + ")");
        }

        @Override
        public void onScopeOpened(final Observation.Context context) {
            record("onScopeOpened(name=" + context.getName() + ")");
        }

        @Override
        public void onError(final Observation.Context context) {
            record("onError(type=" + context.getError().getClass().getSimpleName()
                    + ", message=" + context.getError().getMessage() + ")");
        }

        @Override
        public void onScopeClosed(final Observation.Context context) {
            record("onScopeClosed(name=" + context.getName() + ")");
        }

        @Override
        public void onStop(final Observation.Context context) {
            record("onStop(name=" + context.getName() + ")");
        }

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }

        private static String value(final Observation.Context context, final String key) {
            final KeyValue keyValue = context.getLowCardinalityKeyValue(key);
            return keyValue != null ? keyValue.getValue() : "<absent>";
        }

        private void record(final String event) {
            events.add(event);
        }
    }

    public static final class InvocationTarget {
        public String succeed() {
            return "ok";
        }

        public String fail() {
            throw new IllegalStateException("action failed");
        }
    }
}
