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
package org.apache.causeway.core.metamodel.services.priming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationNaming;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

class PrimingRegistryDefaultObservationTest {

    static class Invoice {}

    private SpecificationLoader specificationLoader;
    private ServiceRegistry serviceRegistry;
    private ObjectSpecification invoiceSpecification;

    @BeforeEach
    void setUp() {
        specificationLoader = mock(SpecificationLoader.class);
        serviceRegistry = mock(ServiceRegistry.class);
        invoiceSpecification = specification(
                Invoice.class, "test.Invoice", "approve", "recalculate");
        when(specificationLoader.snapshotSpecifications())
                .thenReturn(Can.of(invoiceSpecification));
    }

    @Test
    void actionPrimerHasSemanticNameCanonicalMetadataAndNaturalParent() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final List<String> calls = new ArrayList<>();
        final PrimingRegistrar registrar = registry -> registry.action(
                Invoice.class,
                "approve",
                (invoice, arguments) -> calls.add("primer"));
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));
        final Observation parent = Observation.createNotStarted(
                "causeway.action.invocation", observationRegistry).start();

        try (Observation.Scope ignored = parent.openScope()) {
            registry.primeAction(
                    invoiceSpecification,
                    "approve",
                    new Invoice(),
                    Collections.emptyList());
        } finally {
            parent.stop();
        }

        assertEquals(Collections.singletonList("primer"), calls);
        final Snapshot primer = handler.single(
                PrimingRegistryDefault.ACTION_OBSERVATION_NAME);
        assertEquals("prime action test.Invoice#approve", primer.contextualName);
        assertEquals("causeway.action.invocation", primer.parentName);
        assertEquals("test.Invoice", primer.lowCardinality.get(
                PrimingRegistryDefault.OBJECT_TYPE_TAG));
        assertEquals("test.Invoice#approve()", primer.lowCardinality.get(
                PrimingRegistryDefault.ACTION_ID_TAG));
        assertEquals("PrimingRegistryDefault", primer.lowCardinality.get("causeway.bean"));
        assertEquals("metamodel", primer.lowCardinality.get("causeway.module"));
        assertFalse(primer.lowCardinality.keySet().stream().anyMatch(key ->
                key.contains("argument")
                || key.contains("target")
                || key.contains("primer.class")
                || key.contains("sequence")));
        assertNull(observationRegistry.getCurrentObservation());
    }

    @Test
    void everyMatchingActionPrimerGetsASeparateSiblingObservation() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final List<String> calls = new ArrayList<>();
        final PrimingRegistrar registrar = registry -> {
            registry.action(Invoice.class, "approve",
                    (invoice, arguments) -> calls.add("first"));
            registry.action(Invoice.class, "approve",
                    (invoice, arguments) -> calls.add("second"));
        };
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));
        final Observation parent = Observation.createNotStarted(
                "causeway.action.invocation", observationRegistry).start();

        try (Observation.Scope ignored = parent.openScope()) {
            registry.primeAction(
                    invoiceSpecification,
                    "approve",
                    new Invoice(),
                    Collections.emptyList());
        } finally {
            parent.stop();
        }

        assertEquals(List.of("first", "second"), calls);
        final List<Snapshot> primers = handler.named(
                PrimingRegistryDefault.ACTION_OBSERVATION_NAME);
        assertEquals(2, primers.size());
        assertTrue(primers.stream().allMatch(primer ->
                "causeway.action.invocation".equals(primer.parentName)));
        assertFalse(handler.names().contains("causeway.priming"));
    }

    @Test
    void everyMatchingViewPrimerGetsASeparateSiblingObservation() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final List<String> calls = new ArrayList<>();
        final PrimingRegistrar registrar = registry -> {
            registry.view(Invoice.class, invoice -> calls.add("first"));
            registry.view(Invoice.class, invoice -> calls.add("second"));
        };
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));
        final Observation request = Observation.createNotStarted(
                "http.request", observationRegistry).start();

        try (Observation.Scope ignored = request.openScope()) {
            registry.primeView(invoiceSpecification, new Invoice());
        } finally {
            request.stop();
        }

        assertEquals(List.of("first", "second"), calls);
        final List<Snapshot> primers = handler.named(
                PrimingRegistryDefault.VIEW_OBSERVATION_NAME);
        assertEquals(2, primers.size());
        assertTrue(primers.stream().allMatch(primer ->
                "http.request".equals(primer.parentName)));
        assertFalse(handler.names().contains("causeway.priming"));
    }

    @Test
    void viewPrimerIsARequestLevelSiblingOfLaterPagePreparation() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final PrimingRegistrar registrar = registry -> registry.view(
                Invoice.class, invoice -> {});
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));
        final Observation request = Observation.createNotStarted(
                "http.request", observationRegistry).start();

        try (Observation.Scope ignored = request.openScope()) {
            registry.primeView(invoiceSpecification, new Invoice());
            Observation.createNotStarted(
                    "causeway.wicket.page.prepare", observationRegistry)
                    .contextualName("prepare test.Invoice")
                    .observe(() -> {});
        } finally {
            request.stop();
        }

        final Snapshot primer = handler.single(
                PrimingRegistryDefault.VIEW_OBSERVATION_NAME);
        assertEquals("prime view test.Invoice", primer.contextualName);
        assertEquals("http.request", primer.parentName);
        assertEquals("test.Invoice", primer.lowCardinality.get(
                PrimingRegistryDefault.OBJECT_TYPE_TAG));
        assertNull(primer.lowCardinality.get(PrimingRegistryDefault.ACTION_ID_TAG));
        assertEquals("http.request", handler.single(
                "causeway.wicket.page.prepare").parentName);
    }

    @Test
    void contributedActionUsesRegisteredTargetIdentityRatherThanMixinIdentity() {
        invoiceSpecification = specification(Invoice.class, "test.Invoice");
        final ObjectAction contributedAction = mock(ObjectAction.class);
        when(contributedAction.getFeatureIdentifier()).thenReturn(
                Identifier.actionIdentifier(
                        LogicalType.eager(
                                Invoice.class, "implementation.Invoice_approveMixin"),
                        "approve"));
        when(invoiceSpecification.getAction("approve", MixedIn.INCLUDED))
                .thenReturn(Optional.of(contributedAction));
        when(specificationLoader.snapshotSpecifications())
                .thenReturn(Can.of(invoiceSpecification));
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final PrimingRegistrar registrar = registry -> registry.action(
                Invoice.class, "approve", (invoice, arguments) -> {});
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));

        registry.primeAction(
                invoiceSpecification,
                "approve",
                new Invoice(),
                Collections.emptyList());

        final Snapshot primer = handler.single(
                PrimingRegistryDefault.ACTION_OBSERVATION_NAME);
        assertEquals("prime action test.Invoice#approve", primer.contextualName);
        assertEquals("test.Invoice#approve()", primer.lowCardinality.get(
                PrimingRegistryDefault.ACTION_ID_TAG));
        assertFalse(primer.contextualName.contains("approveMixin"));
        assertFalse(primer.lowCardinality.values().stream().anyMatch(value ->
                value.contains("approveMixin")));
    }

    @Test
    void namesUseExistingCasePreservingBoundedPolicyAndKeepCompleteAttributes() {
        final String logicalTypeName =
                "org.example.application.verylongnamespace.MixedCaseInvoiceAggregate";
        invoiceSpecification = specification(
                Invoice.class, logicalTypeName, "RecalculateOutstandingBalance");
        when(specificationLoader.snapshotSpecifications())
                .thenReturn(Can.of(invoiceSpecification));
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final PrimingRegistrar registrar = registry -> registry.action(
                Invoice.class,
                "RecalculateOutstandingBalance",
                (invoice, arguments) -> {});
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));

        registry.primeAction(
                invoiceSpecification,
                "RecalculateOutstandingBalance",
                new Invoice(),
                Collections.emptyList());

        final Snapshot primer = handler.single(
                PrimingRegistryDefault.ACTION_OBSERVATION_NAME);
        assertEquals(CausewayObservationNaming.forLogicalMember(
                "prime action",
                logicalTypeName + "#RecalculateOutstandingBalance"),
                primer.contextualName);
        assertTrue(primer.contextualName.length()
                <= CausewayObservationNaming.MAX_CONTEXTUAL_NAME_LENGTH);
        assertTrue(primer.contextualName.contains("MixedCaseInvoiceAggregate"));
        assertEquals(logicalTypeName, primer.lowCardinality.get(
                PrimingRegistryDefault.OBJECT_TYPE_TAG));
        assertEquals(logicalTypeName + "#RecalculateOutstandingBalance()",
                primer.lowCardinality.get(PrimingRegistryDefault.ACTION_ID_TAG));
    }

    @Test
    void failureIsRecordedPropagatesUnchangedAndPreventsLaterPrimer() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final IllegalStateException expected = new IllegalStateException("primer failed");
        final List<String> calls = new ArrayList<>();
        final PrimingRegistrar registrar = registry -> {
            registry.view(Invoice.class, invoice -> {
                calls.add("first");
                throw expected;
            });
            registry.view(Invoice.class, invoice -> calls.add("second"));
        };
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));

        final IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> registry.primeView(invoiceSpecification, new Invoice()));

        assertSame(expected, actual);
        assertEquals(Collections.singletonList("first"), calls);
        assertEquals(Collections.singletonList(expected), handler.errors);
        assertEquals(1, handler.named(PrimingRegistryDefault.VIEW_OBSERVATION_NAME).size());
        assertNull(observationRegistry.getCurrentObservation());
    }

    @Test
    void errorIsRecordedAndPropagatesUnchanged() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final AssertionError expected = new AssertionError("primer error");
        final PrimingRegistrar registrar = registry -> registry.action(
                Invoice.class, "approve", (invoice, arguments) -> {
                    throw expected;
                });
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));

        final AssertionError actual = assertThrows(
                AssertionError.class,
                () -> registry.primeAction(
                        invoiceSpecification,
                        "approve",
                        new Invoice(),
                        Collections.emptyList()));

        assertSame(expected, actual);
        assertEquals(Collections.singletonList(expected), handler.errors);
        assertNull(observationRegistry.getCurrentObservation());
    }

    @Test
    void absentOrNoopObservationIntegrationDoesNotChangeCallbacks() {
        final List<String> calls = new ArrayList<>();
        final PrimingRegistrar registrar = registry -> {
            registry.action(Invoice.class, "approve",
                    (invoice, arguments) -> calls.add("action"));
            registry.view(Invoice.class, invoice -> calls.add("view"));
        };
        final PrimingRegistryDefault absent = initializedRegistry(registrar, null);

        absent.primeAction(invoiceSpecification, "approve", new Invoice(), List.of());
        absent.primeView(invoiceSpecification, new Invoice());

        final PrimingRegistryDefault noop = initializedRegistry(
                registrar,
                new CausewayObservationIntegration(ObservationRegistry.NOOP));
        noop.primeAction(invoiceSpecification, "approve", new Invoice(), List.of());
        noop.primeView(invoiceSpecification, new Invoice());

        assertEquals(List.of("action", "view", "action", "view"), calls);
    }

    @Test
    void noMatchingPrimerCreatesNoObservation() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry observationRegistry = registryWith(handler);
        final PrimingRegistrar registrar = registry -> registry.action(
                Invoice.class, "approve", (invoice, arguments) -> {});
        final PrimingRegistryDefault registry = initializedRegistry(
                registrar, new CausewayObservationIntegration(observationRegistry));

        registry.primeAction(
                invoiceSpecification,
                "recalculate",
                new Invoice(),
                Collections.emptyList());
        registry.primeView(invoiceSpecification, new Invoice());

        assertTrue(handler.snapshots.isEmpty());
    }

    private PrimingRegistryDefault initializedRegistry(
            final PrimingRegistrar registrar,
            final CausewayObservationIntegration integration) {
        when(serviceRegistry.select(PrimingRegistrar.class)).thenReturn(Can.of(registrar));
        if(integration != null) {
            doReturn(Optional.of(integration)).when(serviceRegistry)
                    .lookupService(CausewayObservationIntegration.class);
        } else {
            doReturn(Optional.empty()).when(serviceRegistry)
                    .lookupService(CausewayObservationIntegration.class);
        }
        final PrimingRegistryDefault registry = new PrimingRegistryDefault(
                specificationLoader, serviceRegistry);
        registry.onMetamodelAboutToBeLoaded();
        registry.onMetamodelLoaded();
        return registry;
    }

    private static ObjectSpecification specification(
            final Class<?> correspondingClass,
            final String logicalTypeName,
            final String... actionNames) {
        final ObjectSpecification specification = mock(ObjectSpecification.class);
        doReturn(correspondingClass).when(specification).getCorrespondingClass();
        when(specification.logicalTypeName()).thenReturn(logicalTypeName);
        for (String actionName : actionNames) {
            final ObjectAction action = mock(ObjectAction.class);
            final Identifier identifier = mock(Identifier.class);
            when(identifier.memberLogicalName()).thenReturn(actionName);
            when(action.getFeatureIdentifier()).thenReturn(identifier);
            when(specification.getAction(actionName, MixedIn.INCLUDED))
                    .thenReturn(Optional.of(action));
        }
        return specification;
    }

    private static ObservationRegistry registryWith(final RecordingHandler handler) {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    private static final class Snapshot {
        private final String name;
        private final String contextualName;
        private final String parentName;
        private final Map<String, String> lowCardinality;

        private Snapshot(final Observation.Context context) {
            this.name = context.getName();
            this.contextualName = context.getContextualName();
            this.parentName = context.getParentObservation() != null
                    ? context.getParentObservation().getContextView().getName()
                    : null;
            this.lowCardinality = new LinkedHashMap<>();
            for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
                lowCardinality.put(keyValue.getKey(), keyValue.getValue());
            }
        }
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {
        private final List<Snapshot> snapshots = new ArrayList<>();
        private final List<Throwable> errors = new ArrayList<>();

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }

        @Override
        public void onStart(final Observation.Context context) {
            snapshots.add(new Snapshot(context));
        }

        @Override
        public void onError(final Observation.Context context) {
            errors.add(context.getError());
        }

        private List<Snapshot> named(final String name) {
            final List<Snapshot> matches = new ArrayList<>();
            for (Snapshot snapshot : snapshots) {
                if(name.equals(snapshot.name)) {
                    matches.add(snapshot);
                }
            }
            return matches;
        }

        private Snapshot single(final String name) {
            final List<Snapshot> matches = named(name);
            assertEquals(1, matches.size(), () -> "Expected one " + name);
            return matches.get(0);
        }

        private List<String> names() {
            final List<String> names = new ArrayList<>();
            for (Snapshot snapshot : snapshots) {
                names.add(snapshot.name);
            }
            return names;
        }
    }
}
