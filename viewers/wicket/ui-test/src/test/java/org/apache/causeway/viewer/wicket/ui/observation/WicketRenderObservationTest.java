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
package org.apache.causeway.viewer.wicket.ui.observation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.opentelemetry.api.trace.Span;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer;
import org.apache.causeway.core.config.observation.ObservationClosure;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.causeway.viewer.wicket.ui.components.actions.ActionParametersPanel;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.WicketTester;

class WicketRenderObservationTest {

    private static final String OBJECT_TYPE = "demo.Customer";

    @Test
    void descriptorsUseStableNamesAndSafeIdentifiers() {
        final ObservationRegistry registry = registryWith(new RecordingHandler());
        final WicketRenderObservationDescriptor descriptor =
                WicketRenderObservationDescriptor.property(OBJECT_TYPE, OBJECT_TYPE + "#name");
        final Observation observation = descriptor.customize(
                Observation.createNotStarted(
                        descriptor.getRegion().getObservationName(), registry));

        assertEquals("causeway.wicket.property.render", observation.getContext().getName());
        assertEquals("render property name", observation.getContext().getContextualName());
        assertEquals(OBJECT_TYPE,
                observation.getContext().getLowCardinalityKeyValue("causeway.object.type").getValue());
        assertEquals(OBJECT_TYPE + "#name",
                observation.getContext().getLowCardinalityKeyValue("causeway.property.id").getValue());
        assertNull(observation.getContext().getLowCardinalityKeyValue("causeway.action.id"));
        final WicketRenderObservationDescriptor defaultFieldset =
                WicketRenderObservationDescriptor.fieldset(OBJECT_TYPE, null);
        assertEquals("<default>", defaultFieldset.getMemberId());
        assertEquals("render fieldset default", defaultFieldset.getContextualName());
        assertEquals("render fieldset identity",
                WicketRenderObservationDescriptor.fieldset(
                        OBJECT_TYPE, "identity").getContextualName());
        assertEquals("initialize collection orders",
                WicketRenderObservationDescriptor.collectionInitialization(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("prepare collection orders",
                WicketRenderObservationDescriptor.collectionPreparation(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("prepare row demo.Order",
                WicketRenderObservationDescriptor.rowPreparation(
                        "demo.Order", OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render collection orders",
                WicketRenderObservationDescriptor.collection(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render table orders",
                WicketRenderObservationDescriptor.table(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render table header orders",
                WicketRenderObservationDescriptor.tableHeader(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render table body orders",
                WicketRenderObservationDescriptor.tableBody(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render table footer orders",
                WicketRenderObservationDescriptor.tableFooter(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render row demo.Order",
                WicketRenderObservationDescriptor.row(
                        "demo.Order", OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render action updateName",
                WicketRenderObservationDescriptor.action(
                        OBJECT_TYPE, OBJECT_TYPE + "#updateName()").getContextualName());
    }

    @Test
    void pageAndPromptDescriptorsUseMeaningfulContextualNames() {
        final ObservationRegistry registry = registryWith(new RecordingHandler());
        final WicketRenderObservationDescriptor preparation =
                WicketRenderObservationDescriptor.pagePreparation(OBJECT_TYPE);
        final WicketRenderObservationDescriptor page =
                WicketRenderObservationDescriptor.page(OBJECT_TYPE);
        final WicketRenderObservationDescriptor prompt =
                WicketRenderObservationDescriptor.actionPrompt(
                        OBJECT_TYPE,
                        OBJECT_TYPE + "#updateName()",
                        "updateName");

        final Observation preparationObservation = preparation.customize(
                Observation.createNotStarted(
                        preparation.getRegion().getObservationName(), registry));
        final Observation pageObservation = page.customize(
                Observation.createNotStarted(page.getRegion().getObservationName(), registry));
        final Observation promptObservation = prompt.customize(
                Observation.createNotStarted(prompt.getRegion().getObservationName(), registry));

        assertEquals("causeway.wicket.page.prepare",
                preparationObservation.getContext().getName());
        assertEquals("prepare demo.Customer",
                preparationObservation.getContext().getContextualName());
        assertEquals("causeway.wicket.page.render", pageObservation.getContext().getName());
        assertEquals("render demo.Customer", pageObservation.getContext().getContextualName());
        assertEquals("causeway.wicket.action.prompt.render", promptObservation.getContext().getName());
        assertEquals("prompt demo.Customer#updateName",
                promptObservation.getContext().getContextualName());
        assertEquals(OBJECT_TYPE,
                promptObservation.getContext().getLowCardinalityKeyValue(
                        "causeway.object.type").getValue());
        assertEquals(OBJECT_TYPE + "#updateName()",
                promptObservation.getContext().getLowCardinalityKeyValue(
                        "causeway.action.id").getValue());
    }

    @Test
    void onlyFullPageAndPromptDescriptorsNominateSemanticTraceNames() {
        final Span pageSpan = recordingSpan();
        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(pageSpan)) {
            WicketRenderObservationDescriptor.pagePreparation(OBJECT_TYPE)
                    .nominateSemanticTraceName();
            WicketRenderObservationDescriptor.property(
                    OBJECT_TYPE, OBJECT_TYPE + "#name")
                    .nominateSemanticTraceName();
            WicketRenderObservationDescriptor.page(OBJECT_TYPE)
                    .nominateSemanticTraceName();
        }
        verify(pageSpan).updateName("view demo.Customer");
        verify(pageSpan).setAttribute(
                CausewaySemanticTraceNamer.OBJECT_TYPE_ATTRIBUTE, OBJECT_TYPE);

        final Span promptSpan = recordingSpan();
        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(promptSpan)) {
            WicketRenderObservationDescriptor.actionPrompt(
                    OBJECT_TYPE, OBJECT_TYPE + "#updateName()", "updateName")
                    .nominateSemanticTraceName();
        }
        verify(promptSpan).updateName("prompt demo.Customer#updateName");
        verify(promptSpan).setAttribute(
                CausewaySemanticTraceNamer.ACTION_ID_ATTRIBUTE,
                OBJECT_TYPE + "#updateName()");

        final Span ajaxSpan = recordingSpan();
        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(ajaxSpan)) {
            WicketRenderObservationDescriptor.collection(
                    OBJECT_TYPE, OBJECT_TYPE + "#orders")
                    .nominateSemanticTraceName();
            WicketRenderObservationDescriptor.table(
                    OBJECT_TYPE, OBJECT_TYPE + "#orders")
                    .nominateSemanticTraceName();
            WicketRenderObservationDescriptor.property(
                    OBJECT_TYPE, OBJECT_TYPE + "#name")
                    .nominateSemanticTraceName();
        }
        verify(ajaxSpan, never()).updateName(anyString());

        final Span preparationSpan = recordingSpan();
        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(preparationSpan)) {
            WicketRenderObservationDescriptor.pagePreparation(OBJECT_TYPE)
                    .nominateSemanticTraceName();
        }
        verify(preparationSpan, never()).updateName(anyString());
    }

    @Test
    void pageAndPromptNamesFallBackToSimpleLogicalType() {
        final String longObjectType =
                "aVeryLongApplicationNamespaceThatExceedsTheLimit.Customer";

        assertEquals("prepare Customer",
                WicketRenderObservationDescriptor.pagePreparation(
                        longObjectType).getContextualName());
        assertEquals("render Customer",
                WicketRenderObservationDescriptor.page(
                        longObjectType).getContextualName());
        assertEquals("prepare row Customer",
                WicketRenderObservationDescriptor.rowPreparation(
                        longObjectType, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("render row Customer",
                WicketRenderObservationDescriptor.row(
                        longObjectType, OBJECT_TYPE + "#orders").getContextualName());
        assertEquals("prompt Customer#updateName",
                WicketRenderObservationDescriptor.actionPrompt(
                        longObjectType,
                        longObjectType + "#updateName()",
                        "updateName").getContextualName());
    }

    @Test
    void preparationOwnsPreRenderWorkAndClosesBeforeRendering() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final PreparedContainer page = new PreparedContainer(
                    "component", integration, OBJECT_TYPE);
            page.add(new WebMarkupContainer("child") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onConfigure() {
                    integration.createNotStarted(
                            getClass(), "causeway.property.access")
                            .contextualName("prop demo.Customer#calculatedTotal")
                            .observe(() -> {});
                    super.onConfigure();
                }
            });

            tester.startComponentInPage(page, Markup.of(
                    "<div wicket:id='component'><span wicket:id='child'></span></div>"));

            assertEquals(List.of(
                    "causeway.wicket.page.prepare<-null",
                    "causeway.property.access<-causeway.wicket.page.prepare",
                    "causeway.wicket.page.render<-null"), handler.parents);
            assertEquals(List.of(
                    "prepare demo.Customer",
                    "prop demo.Customer#calculatedTotal",
                    "render demo.Customer"), handler.contextualNames);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void collectionAndRowPreparationNestBeforeCollectionRendering() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final PreparedContainer page = new PreparedContainer(
                    "component", integration, OBJECT_TYPE);
            final PreparedContainer collection = new PreparedContainer(
                    "collection",
                    integration,
                    WicketRenderObservationDescriptor.collectionPreparation(
                            OBJECT_TYPE, OBJECT_TYPE + "#orders"),
                    WicketRenderObservationDescriptor.collection(
                            OBJECT_TYPE, OBJECT_TYPE + "#orders"));
            collection.add(new WebMarkupContainer("row") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onConfigure() {
                    WicketPreparationObservation.observe(
                            collection,
                            WicketRenderObservationDescriptor.rowPreparation(
                                    "demo.Order", OBJECT_TYPE + "#orders"),
                            () -> integration.createNotStarted(
                                    getClass(), "SELECT orders")
                                    .contextualName("SELECT orders")
                                    .observe(() -> {}));
                    super.onConfigure();
                }
            });
            page.add(collection);

            tester.startComponentInPage(page, Markup.of(
                    "<div wicket:id='component'>"
                    + "<div wicket:id='collection'><span wicket:id='row'></span></div>"
                    + "</div>"));

            assertEquals(List.of(
                    "causeway.wicket.page.prepare<-null",
                    "causeway.wicket.collection.prepare<-causeway.wicket.page.prepare",
                    "causeway.wicket.collection.row.prepare<-causeway.wicket.collection.prepare",
                    "SELECT orders<-causeway.wicket.collection.row.prepare",
                    "causeway.wicket.page.render<-null",
                    "causeway.wicket.collection.render<-causeway.wicket.page.render"),
                    handler.parents);
            assertEquals(List.of(
                    "prepare demo.Customer",
                    "prepare collection orders",
                    "prepare row demo.Order",
                    "SELECT orders",
                    "render demo.Customer",
                    "render collection orders"),
                    handler.contextualNames);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void collectionInitializationOwnsConstructionTimeWorkAndClosesSynchronously() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final PreparedContainer page = new PreparedContainer(
                    "component", integration, OBJECT_TYPE);
            page.add(new WebMarkupContainer("collection") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onConfigure() {
                    WicketPreparationObservation.observe(
                            page,
                            WicketRenderObservationDescriptor.collectionInitialization(
                                    OBJECT_TYPE, OBJECT_TYPE + "#orders"),
                            () -> integration.createNotStarted(
                                    getClass(), "SELECT collection")
                                    .contextualName("SELECT collection")
                                    .observe(() -> {}));
                    super.onConfigure();
                }
            });

            tester.startComponentInPage(page, Markup.of(
                    "<div wicket:id='component'><span wicket:id='collection'></span></div>"));

            assertEquals(List.of(
                    "causeway.wicket.page.prepare<-null",
                    "causeway.wicket.collection.initialize<-causeway.wicket.page.prepare",
                    "SELECT collection<-causeway.wicket.collection.initialize",
                    "causeway.wicket.page.render<-null"), handler.parents);
            assertEquals(List.of(
                    "prepare demo.Customer",
                    "initialize collection orders",
                    "SELECT collection",
                    "render demo.Customer"), handler.contextualNames);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void preparationWithNoopRegistryLeavesRenderingUnchanged() {
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(ObservationRegistry.NOOP);
        final WicketTester tester = new WicketTester();
        try {
            final PreparedContainer page = new PreparedContainer(
                    "component", integration, OBJECT_TYPE);

            tester.startComponentInPage(
                    page, Markup.of("<div wicket:id='component'></div>"));

            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void preparationFailureIsRecordedAndScopeIsClosed() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final PreparedContainer page = new PreparedContainer(
                    "component", integration, OBJECT_TYPE);
            page.add(new WebMarkupContainer("child") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onConfigure() {
                    throw new IllegalStateException("preparation failed");
                }
            });

            assertThrows(RuntimeException.class, () -> tester.startComponentInPage(
                    page,
                    Markup.of("<div wicket:id='component'><span wicket:id='child'></span></div>")));

            assertTrue(handler.errors.stream().anyMatch(
                    error -> error.startsWith("error:causeway.wicket.page.prepare:")));
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void collectionInitializationFailureIsRecordedAndScopeIsClosed() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final HasMetaModelContext context = new ObservationContext(integration);

        final IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> WicketPreparationObservation.observe(
                        context,
                        WicketRenderObservationDescriptor.collectionInitialization(
                                OBJECT_TYPE, OBJECT_TYPE + "#orders"),
                        () -> {
                            throw new IllegalStateException("initialization failed");
                        }));

        assertEquals("initialization failed", failure.getMessage());
        assertEquals(List.of(
                "error:causeway.wicket.collection.initialize:initialization failed"),
                handler.errors);
        assertNull(integration.observationRegistry().getCurrentObservation());
    }

    @Test
    void rowPreparationFailureIsRecordedAndScopeIsClosed() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final HasMetaModelContext context = new ObservationContext(integration);

        final IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> WicketPreparationObservation.observe(
                        context,
                        WicketRenderObservationDescriptor.rowPreparation(
                                "demo.Order", OBJECT_TYPE + "#orders"),
                        () -> {
                            throw new IllegalStateException("row preparation failed");
                        }));

        assertEquals("row preparation failed", failure.getMessage());
        assertEquals(List.of(
                "error:causeway.wicket.collection.row.prepare:row preparation failed"),
                handler.errors);
        assertNull(integration.observationRegistry().getCurrentObservation());
    }

    @Test
    void nestedLifecycleClosesScopesInReverseOrder() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final WicketRenderObservationTracker.State state = new WicketRenderObservationTracker.State();

        final WicketRenderObservationBehavior page = behaviorFor(
                WicketRenderObservationDescriptor.page(OBJECT_TYPE));
        final ObservationClosure pageClosure = closure(registry, page.descriptor());
        page.activate(pageClosure);
        state.register(page, pageClosure);
        assertEquals("causeway.wicket.page.render", currentName(registry));

        final WicketRenderObservationBehavior property = behaviorFor(
                WicketRenderObservationDescriptor.property(OBJECT_TYPE, OBJECT_TYPE + "#name"));
        final ObservationClosure propertyClosure = closure(registry, property.descriptor());
        property.activate(propertyClosure);
        state.register(property, propertyClosure);
        assertEquals("causeway.wicket.property.render", currentName(registry));

        state.complete(property);
        assertEquals("causeway.wicket.page.render", currentName(registry));
        assertFalse(property.isActive());

        state.complete(page);
        assertNull(currentName(registry));
        assertFalse(page.isActive());
        assertTrue(state.isEmpty());
        assertEquals(List.of(
                "start:causeway.wicket.page.render",
                "scope-opened:causeway.wicket.page.render",
                "start:causeway.wicket.property.render",
                "scope-opened:causeway.wicket.property.render",
                "scope-closed:causeway.wicket.property.render",
                "stop:causeway.wicket.property.render",
                "scope-closed:causeway.wicket.page.render",
                "stop:causeway.wicket.page.render"), handler.events);
    }

    @Test
    void failureCleanupMarksAndClosesAllActiveObservationsIdempotently() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final WicketRenderObservationTracker.State state = new WicketRenderObservationTracker.State();
        final WicketRenderObservationBehavior page = behaviorFor(
                WicketRenderObservationDescriptor.page(OBJECT_TYPE));
        final WicketRenderObservationBehavior tableBody = behaviorFor(
                WicketRenderObservationDescriptor.tableBody(
                        OBJECT_TYPE, OBJECT_TYPE + "#orders"));

        final ObservationClosure pageClosure = closure(registry, page.descriptor());
        final ObservationClosure tableBodyClosure = closure(registry, tableBody.descriptor());
        page.activate(pageClosure);
        tableBody.activate(tableBodyClosure);
        state.register(page, pageClosure);
        state.register(tableBody, tableBodyClosure);

        final RuntimeException failure = new RuntimeException("render failed");
        state.cleanup(failure);
        state.cleanup(failure);

        assertNull(currentName(registry));
        assertFalse(page.isActive());
        assertFalse(tableBody.isActive());
        assertTrue(state.isEmpty());
        assertEquals(List.of(
                "error:causeway.wicket.collection.table.body.render:render failed",
                "error:causeway.wicket.page.render:render failed"), handler.errors);
    }

    @Test
    void actualWicketRenderingPreservesComponentTreeParentage() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final ObservedContainer page = new ObservedContainer(
                    "component", integration,
                    WicketRenderObservationDescriptor.page(OBJECT_TYPE));
            page.add(new ObservedContainer(
                    "prompt", integration,
                    WicketRenderObservationDescriptor.actionPrompt(
                            OBJECT_TYPE,
                            OBJECT_TYPE + "#updateName()",
                            "updateName")));

            tester.startComponentInPage(page, Markup.of(
                    "<div wicket:id='component'><span wicket:id='prompt'></span></div>"));

            assertEquals(List.of(
                    "causeway.wicket.page.render<-null",
                    "causeway.wicket.action.prompt.render<-causeway.wicket.page.render"),
                    handler.parents);
            assertEquals(List.of(
                    "render demo.Customer",
                    "prompt demo.Customer#updateName"), handler.contextualNames);
            assertEquals(List.of(
                    "start:causeway.wicket.page.render",
                    "scope-opened:causeway.wicket.page.render",
                    "start:causeway.wicket.action.prompt.render",
                    "scope-opened:causeway.wicket.action.prompt.render",
                    "scope-closed:causeway.wicket.action.prompt.render",
                    "stop:causeway.wicket.action.prompt.render",
                    "scope-closed:causeway.wicket.page.render",
                    "stop:causeway.wicket.page.render"), handler.events);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void collectionRowRenderingPreservesComponentTreeParentage() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final ObservedContainer collection = new ObservedContainer(
                    "component", integration,
                    WicketRenderObservationDescriptor.collection(
                            OBJECT_TYPE, OBJECT_TYPE + "#orders"));
            final ObservedContainer row = new ObservedContainer(
                    "row", integration,
                    WicketRenderObservationDescriptor.row(
                            "demo.Order", OBJECT_TYPE + "#orders"));
            row.add(new ObservedContainer(
                    "property", integration,
                    WicketRenderObservationDescriptor.property(
                            "demo.Order", "demo.Order#total")));
            final ObservedContainer secondRow = new ObservedContainer(
                    "secondRow", integration,
                    WicketRenderObservationDescriptor.row(
                            "demo.Order", OBJECT_TYPE + "#orders"));
            secondRow.add(new ObservedContainer(
                    "action", integration,
                    WicketRenderObservationDescriptor.action(
                            "demo.Order", "demo.Order#update()")));
            collection.add(row, secondRow);

            tester.startComponentInPage(collection, Markup.of(
                    "<div wicket:id='component'>"
                    + "<div wicket:id='row'><span wicket:id='property'></span></div>"
                    + "<div wicket:id='secondRow'><span wicket:id='action'></span></div>"
                    + "</div>"));

            assertEquals(List.of(
                    "causeway.wicket.collection.render<-null",
                    "causeway.wicket.collection.row.render<-causeway.wicket.collection.render",
                    "causeway.wicket.property.render<-causeway.wicket.collection.row.render",
                    "causeway.wicket.collection.row.render<-causeway.wicket.collection.render",
                    "causeway.wicket.action.render<-causeway.wicket.collection.row.render"),
                    handler.parents);
            assertEquals(List.of(
                    "render collection orders",
                    "render row demo.Order",
                    "render property total",
                    "render row demo.Order",
                    "render action update"),
                    handler.contextualNames);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void aggregateTablePhasesNestRowsBeneathTheBodyUsingAncestorContext() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final String collectionId = OBJECT_TYPE + "#orders";
            final ObservedContainer collection = new ObservedContainer(
                    "component", integration,
                    WicketRenderObservationDescriptor.collection(
                            OBJECT_TYPE, collectionId));
            final WebMarkupContainer table = WicketRenderObservationBehavior.addTo(
                    new WebMarkupContainer("table"),
                    WicketRenderObservationDescriptor.table(
                            OBJECT_TYPE, collectionId));
            final WebMarkupContainer header = WicketRenderObservationBehavior.addTo(
                    new WebMarkupContainer("header"),
                    WicketRenderObservationDescriptor.tableHeader(
                            OBJECT_TYPE, collectionId));
            final WebMarkupContainer body = WicketRenderObservationBehavior.addTo(
                    new WebMarkupContainer("body"),
                    WicketRenderObservationDescriptor.tableBody(
                            OBJECT_TYPE, collectionId));
            final WebMarkupContainer footer = WicketRenderObservationBehavior.addTo(
                    new WebMarkupContainer("footer"),
                    WicketRenderObservationDescriptor.tableFooter(
                            OBJECT_TYPE, collectionId));
            body.add(new ObservedContainer(
                    "row", integration,
                    WicketRenderObservationDescriptor.row(
                            "demo.Order", collectionId)));
            table.add(header, body, footer);
            collection.add(table);

            tester.startComponentInPage(collection, Markup.of(
                    "<div wicket:id='component'>"
                    + "<div wicket:id='table'>"
                    + "<div wicket:id='header'></div>"
                    + "<div wicket:id='body'><div wicket:id='row'></div></div>"
                    + "<div wicket:id='footer'></div>"
                    + "</div>"
                    + "</div>"));

            assertEquals(List.of(
                    "causeway.wicket.collection.render<-null",
                    "causeway.wicket.collection.table.render<-causeway.wicket.collection.render",
                    "causeway.wicket.collection.table.header.render<-causeway.wicket.collection.table.render",
                    "causeway.wicket.collection.table.body.render<-causeway.wicket.collection.table.render",
                    "causeway.wicket.collection.row.render<-causeway.wicket.collection.table.body.render",
                    "causeway.wicket.collection.table.footer.render<-causeway.wicket.collection.table.render"),
                    handler.parents);
            assertEquals(List.of(
                    "render collection orders",
                    "render table orders",
                    "render table header orders",
                    "render table body orders",
                    "render row demo.Order",
                    "render table footer orders"), handler.contextualNames);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void noopObservationLeavesWicketRenderingUnchanged() {
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(ObservationRegistry.NOOP);
        final WicketTester tester = new WicketTester();
        try {
            final ObservedContainer prompt = new ObservedContainer(
                    "component", integration,
                    WicketRenderObservationDescriptor.actionPrompt(
                            OBJECT_TYPE,
                            OBJECT_TYPE + "#updateName()",
                            "updateName"));

            tester.startComponentInPage(prompt, Markup.of(
                    "<span wicket:id='component'>value</span>"));

            tester.assertComponent("component", ObservedContainer.class);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void ajaxPartialRenderingObservesOnlyTheTargetedRegion() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final WebMarkupContainer root = new WebMarkupContainer("component");
            final ObservedContainer property = new ObservedContainer(
                    "property", integration,
                    WicketRenderObservationDescriptor.property(
                            OBJECT_TYPE, OBJECT_TYPE + "#name"));
            property.setOutputMarkupId(true);
            final AjaxLink<Void> refresh = new AjaxLink<Void>("refresh") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(final AjaxRequestTarget target) {
                    target.add(property);
                }
            };
            root.add(refresh, property);
            tester.startComponentInPage(root, Markup.of(
                    "<div wicket:id='component'>"
                    + "<a wicket:id='refresh'>refresh</a>"
                    + "<span wicket:id='property'></span>"
                    + "</div>"));
            handler.events.clear();

            tester.executeAjaxEvent(refresh, "click");

            assertEquals(List.of(
                    "start:causeway.wicket.property.render",
                    "scope-opened:causeway.wicket.property.render",
                    "scope-closed:causeway.wicket.property.render",
                    "stop:causeway.wicket.property.render"), handler.events);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void ajaxPartialCollectionRenderingObservesOnlyParticipatingRowsAndCells() {
        final RecordingHandler handler = new RecordingHandler();
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registryWith(handler));
        final WicketTester tester = new WicketTester();
        try {
            final WebMarkupContainer root = new WebMarkupContainer("component");
            final ObservedContainer collection = new ObservedContainer(
                    "collection", integration,
                    WicketRenderObservationDescriptor.collection(
                            OBJECT_TYPE, OBJECT_TYPE + "#orders"));
            collection.setOutputMarkupId(true);
            final ObservedContainer row = new ObservedContainer(
                    "row", integration,
                    WicketRenderObservationDescriptor.row(
                            "demo.Order", OBJECT_TYPE + "#orders"));
            row.add(new ObservedContainer(
                    "property", integration,
                    WicketRenderObservationDescriptor.property(
                            "demo.Order", "demo.Order#total")));
            collection.add(row);
            final AjaxLink<Void> refresh = new AjaxLink<Void>("refresh") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(final AjaxRequestTarget target) {
                    target.add(collection);
                }
            };
            root.add(refresh, collection);
            tester.startComponentInPage(root, Markup.of(
                    "<div wicket:id='component'>"
                    + "<a wicket:id='refresh'>refresh</a>"
                    + "<div wicket:id='collection'>"
                    + "<div wicket:id='row'><span wicket:id='property'></span></div>"
                    + "</div>"
                    + "</div>"));
            handler.parents.clear();
            handler.contextualNames.clear();

            tester.executeAjaxEvent(refresh, "click");

            assertEquals(List.of(
                    "causeway.wicket.collection.render<-null",
                    "causeway.wicket.collection.row.render<-causeway.wicket.collection.render",
                    "causeway.wicket.property.render<-causeway.wicket.collection.row.render"),
                    handler.parents);
            assertEquals(List.of(
                    "render collection orders",
                    "render row demo.Order",
                    "render property total"),
                    handler.contextualNames);
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void serializationRetainsDescriptorButNotActiveTelemetry() throws Exception {
        final WicketRenderObservationBehavior original = behaviorFor(
                WicketRenderObservationDescriptor.actionPrompt(
                        OBJECT_TYPE, OBJECT_TYPE + "#updateName()", "updateName"));
        original.activate(new ObservationClosure());

        final WicketRenderObservationBehavior restored = roundTrip(original);

        assertFalse(restored.isActive());
        assertEquals("causeway.wicket.action.prompt.render",
                restored.descriptor().getRegion().getObservationName());
        assertEquals("prompt demo.Customer#updateName",
                restored.descriptor().getContextualName());
        assertEquals(OBJECT_TYPE, restored.descriptor().getObjectType());
        assertEquals(OBJECT_TYPE + "#updateName()", restored.descriptor().getMemberId());
    }

    @Test
    void actionParametersPanelHasOnePromptObservationBehavior() {
        final Identifier actionId = Identifier.actionIdentifier(
                LogicalType.eager(Object.class, OBJECT_TYPE), "updateName");
        final ObjectAction action = mock(ObjectAction.class);
        when(action.getFeatureIdentifier()).thenReturn(actionId);
        final ActionModel actionModel = mock(ActionModel.class);
        when(actionModel.getAction()).thenReturn(action);

        final WicketTester tester = new WicketTester();
        try {
            final ActionParametersPanel panel =
                    new ActionParametersPanel("prompt", actionModel);

            assertEquals(1,
                    panel.getBehaviors(WicketRenderObservationBehavior.class).size());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void propertyPolicyIncludesRegularAndParentedTableProperties() {
        final Identifier propertyId = Identifier.propertyIdentifier(
                LogicalType.eager(Object.class, OBJECT_TYPE), "name");
        final OneToOneAssociation property = mock(OneToOneAssociation.class);
        when(property.getFeatureIdentifier()).thenReturn(propertyId);

        final ScalarPropertyModel regularProperty = mock(ScalarPropertyModel.class);
        when(regularProperty.getRenderingHint()).thenReturn(RenderingHint.REGULAR);
        when(regularProperty.getMetaModel()).thenReturn(property);

        final ScalarPropertyModel tableProperty = mock(ScalarPropertyModel.class);
        when(tableProperty.getRenderingHint()).thenReturn(RenderingHint.PARENTED_PROPERTY_COLUMN);
        when(tableProperty.getMetaModel()).thenReturn(property);

        final ScalarModel parameter = mock(ScalarParameterModel.class);
        when(parameter.getRenderingHint()).thenReturn(RenderingHint.REGULAR);

        final Optional<WicketRenderObservationDescriptor> included =
                WicketRenderObservationPolicy.propertyDescriptor(regularProperty);
        assertTrue(included.isPresent());
        assertEquals(OBJECT_TYPE + "#name", included.get().getMemberId());
        assertTrue(WicketRenderObservationPolicy.propertyDescriptor(tableProperty).isPresent());
        assertTrue(WicketRenderObservationPolicy.propertyDescriptor(parameter).isEmpty());
    }

    @Test
    void actionPolicyIncludesObjectFormAndParentedTableActionsUnassociatedWithParameters() {
        final Identifier actionId = Identifier.actionIdentifier(
                LogicalType.eager(Object.class, OBJECT_TYPE), "updateName");
        final ObjectAction action = mock(ObjectAction.class);
        when(action.getFeatureIdentifier()).thenReturn(actionId);

        final ActionModel entityAction = mock(ActionModel.class);
        when(entityAction.getAssociatedParameter()).thenReturn(Optional.empty());
        when(entityAction.getAction()).thenReturn(action);

        final ActionModel parameterAction = mock(ActionModel.class);
        when(parameterAction.getAssociatedParameter()).thenReturn(
                Optional.of(mock(ScalarParameterModel.class)));

        final Optional<WicketRenderObservationDescriptor> included =
                WicketRenderObservationPolicy.actionDescriptor(entityAction, Where.OBJECT_FORMS);
        assertTrue(included.isPresent());
        assertEquals(OBJECT_TYPE + "#updateName()", included.get().getMemberId());
        assertTrue(WicketRenderObservationPolicy.actionDescriptor(
                entityAction, Where.PARENTED_TABLES).isPresent());
        assertTrue(WicketRenderObservationPolicy.actionDescriptor(
                entityAction, Where.STANDALONE_TABLES).isEmpty());
        assertTrue(WicketRenderObservationPolicy.actionDescriptor(
                entityAction, Where.ALL_TABLES).isEmpty());
        assertTrue(WicketRenderObservationPolicy.actionDescriptor(
                entityAction, Where.ANYWHERE).isEmpty());
        assertTrue(WicketRenderObservationPolicy.actionDescriptor(
                parameterAction, Where.OBJECT_FORMS).isEmpty());
    }

    private static WicketRenderObservationBehavior behaviorFor(
            final WicketRenderObservationDescriptor descriptor) {
        return new WicketRenderObservationBehavior(descriptor);
    }

    private static ObservationClosure closure(
            final ObservationRegistry registry,
            final WicketRenderObservationDescriptor descriptor) {
        return new ObservationClosure().startAndOpenScope(descriptor.customize(
                Observation.createNotStarted(descriptor.getRegion().getObservationName(), registry)));
    }

    private static String currentName(final ObservationRegistry registry) {
        final Observation current = registry.getCurrentObservation();
        return current != null ? current.getContext().getName() : null;
    }

    private static ObservationRegistry registryWith(final RecordingHandler handler) {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(final T object) throws Exception {
        final byte[] serialized;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(object);
            serialized = bytes.toByteArray();
        }
        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(serialized))) {
            return (T) input.readObject();
        }
    }

    private static final class ObservationContext
    implements HasMetaModelContext {

        private final CausewayObservationIntegration integration;

        private ObservationContext(
                final CausewayObservationIntegration integration) {
            this.integration = integration;
        }

        @Override
        public <T> Optional<T> lookupService(final Class<T> serviceClass) {
            return serviceClass == CausewayObservationIntegration.class
                    ? Optional.of(serviceClass.cast(integration))
                    : Optional.empty();
        }
    }

    private static final class ObservedContainer
    extends WebMarkupContainer
    implements HasMetaModelContext {

        private static final long serialVersionUID = 1L;
        private final CausewayObservationIntegration integration;

        private ObservedContainer(
                final String id,
                final CausewayObservationIntegration integration,
                final WicketRenderObservationDescriptor descriptor) {
            super(id);
            this.integration = integration;
            WicketRenderObservationBehavior.addTo(this, descriptor);
        }

        @Override
        public <T> Optional<T> lookupService(final Class<T> serviceClass) {
            return serviceClass == CausewayObservationIntegration.class
                    ? Optional.of(serviceClass.cast(integration))
                    : Optional.empty();
        }
    }

    private static final class PreparedContainer
    extends WebMarkupContainer
    implements HasMetaModelContext {

        private static final long serialVersionUID = 1L;
        private final CausewayObservationIntegration integration;
        private final WicketPreparationObservation preparation;

        private PreparedContainer(
                final String id,
                final CausewayObservationIntegration integration,
                final String objectType) {
            this(
                    id,
                    integration,
                    WicketRenderObservationDescriptor.pagePreparation(objectType),
                    WicketRenderObservationDescriptor.page(objectType));
        }

        private PreparedContainer(
                final String id,
                final CausewayObservationIntegration integration,
                final WicketRenderObservationDescriptor preparationDescriptor,
                final WicketRenderObservationDescriptor renderDescriptor) {
            super(id);
            this.integration = integration;
            this.preparation = new WicketPreparationObservation(
                    preparationDescriptor);
            WicketRenderObservationBehavior.addTo(this, renderDescriptor);
        }

        @Override
        protected void onConfigure() {
            preparation.configure(this, () -> super.onConfigure());
        }

        @Override
        protected void onBeforeRender() {
            preparation.beforeRender(() -> super.onBeforeRender());
        }

        @Override
        protected void onDetach() {
            preparation.detach(() -> super.onDetach());
        }

        @Override
        public <T> Optional<T> lookupService(final Class<T> serviceClass) {
            return serviceClass == CausewayObservationIntegration.class
                    ? Optional.of(serviceClass.cast(integration))
                    : Optional.empty();
        }
    }

    private static Span recordingSpan() {
        final Span span = mock(Span.class);
        when(span.isRecording()).thenReturn(true);
        return span;
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> events = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> parents = new ArrayList<>();
        private final List<String> contextualNames = new ArrayList<>();

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }

        @Override
        public void onStart(final Observation.Context context) {
            events.add("start:" + context.getName());
            contextualNames.add(context.getContextualName());
            parents.add(context.getName() + "<-" + (context.getParentObservation() != null
                    ? context.getParentObservation().getContextView().getName()
                    : "null"));
        }

        @Override
        public void onScopeOpened(final Observation.Context context) {
            events.add("scope-opened:" + context.getName());
        }

        @Override
        public void onError(final Observation.Context context) {
            errors.add("error:" + context.getName() + ":" + context.getError().getMessage());
        }

        @Override
        public void onScopeClosed(final Observation.Context context) {
            events.add("scope-closed:" + context.getName());
        }

        @Override
        public void onStop(final Observation.Context context) {
            events.add("stop:" + context.getName());
        }
    }
}
