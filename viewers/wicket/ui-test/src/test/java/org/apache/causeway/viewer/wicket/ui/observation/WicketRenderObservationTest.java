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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.ObservationClosure;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;

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
        assertEquals("causeway.wicket.property.render", observation.getContext().getContextualName());
        assertEquals(OBJECT_TYPE,
                observation.getContext().getLowCardinalityKeyValue("causeway.object.type").getValue());
        assertEquals(OBJECT_TYPE + "#name",
                observation.getContext().getLowCardinalityKeyValue("causeway.property.id").getValue());
        assertNull(observation.getContext().getLowCardinalityKeyValue("causeway.action.id"));
        assertEquals("<default>",
                WicketRenderObservationDescriptor.fieldset(OBJECT_TYPE, null).getMemberId());
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
        final WicketRenderObservationBehavior collection = behaviorFor(
                WicketRenderObservationDescriptor.collection(OBJECT_TYPE, OBJECT_TYPE + "#orders"));

        final ObservationClosure pageClosure = closure(registry, page.descriptor());
        final ObservationClosure collectionClosure = closure(registry, collection.descriptor());
        page.activate(pageClosure);
        collection.activate(collectionClosure);
        state.register(page, pageClosure);
        state.register(collection, collectionClosure);

        final RuntimeException failure = new RuntimeException("render failed");
        state.cleanup(failure);
        state.cleanup(failure);

        assertNull(currentName(registry));
        assertFalse(page.isActive());
        assertFalse(collection.isActive());
        assertTrue(state.isEmpty());
        assertEquals(List.of(
                "error:causeway.wicket.collection.render:render failed",
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
                    "property", integration,
                    WicketRenderObservationDescriptor.property(
                            OBJECT_TYPE, OBJECT_TYPE + "#name")));

            tester.startComponentInPage(page, Markup.of(
                    "<div wicket:id='component'><span wicket:id='property'></span></div>"));

            assertEquals(List.of(
                    "causeway.wicket.page.render<-null",
                    "causeway.wicket.property.render<-causeway.wicket.page.render"), handler.parents);
            assertEquals(List.of(
                    "start:causeway.wicket.page.render",
                    "scope-opened:causeway.wicket.page.render",
                    "start:causeway.wicket.property.render",
                    "scope-opened:causeway.wicket.property.render",
                    "scope-closed:causeway.wicket.property.render",
                    "stop:causeway.wicket.property.render",
                    "scope-closed:causeway.wicket.page.render",
                    "stop:causeway.wicket.page.render"), handler.events);
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
            final ObservedContainer property = new ObservedContainer(
                    "component", integration,
                    WicketRenderObservationDescriptor.property(
                            OBJECT_TYPE, OBJECT_TYPE + "#name"));

            tester.startComponentInPage(property, Markup.of(
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
    void serializationRetainsDescriptorButNotActiveTelemetry() throws Exception {
        final WicketRenderObservationBehavior original = behaviorFor(
                WicketRenderObservationDescriptor.fieldset(OBJECT_TYPE, "identity"));
        original.activate(new ObservationClosure());

        final WicketRenderObservationBehavior restored = roundTrip(original);

        assertFalse(restored.isActive());
        assertEquals("causeway.wicket.fieldset.render",
                restored.descriptor().getRegion().getObservationName());
        assertEquals(OBJECT_TYPE, restored.descriptor().getObjectType());
        assertEquals("identity", restored.descriptor().getMemberId());
    }

    @Test
    void propertyPolicyIncludesOnlyRegularEntityProperties() {
        final Identifier propertyId = Identifier.propertyIdentifier(
                LogicalType.eager(Object.class, OBJECT_TYPE), "name");
        final OneToOneAssociation property = mock(OneToOneAssociation.class);
        when(property.getFeatureIdentifier()).thenReturn(propertyId);

        final ScalarPropertyModel regularProperty = mock(ScalarPropertyModel.class);
        when(regularProperty.getRenderingHint()).thenReturn(RenderingHint.REGULAR);
        when(regularProperty.getMetaModel()).thenReturn(property);

        final ScalarPropertyModel tableProperty = mock(ScalarPropertyModel.class);
        when(tableProperty.getRenderingHint()).thenReturn(RenderingHint.PARENTED_PROPERTY_COLUMN);

        final ScalarModel parameter = mock(ScalarParameterModel.class);
        when(parameter.getRenderingHint()).thenReturn(RenderingHint.REGULAR);

        final Optional<WicketRenderObservationDescriptor> included =
                WicketRenderObservationPolicy.propertyDescriptor(regularProperty);
        assertTrue(included.isPresent());
        assertEquals(OBJECT_TYPE + "#name", included.get().getMemberId());
        assertTrue(WicketRenderObservationPolicy.propertyDescriptor(tableProperty).isEmpty());
        assertTrue(WicketRenderObservationPolicy.propertyDescriptor(parameter).isEmpty());
    }

    @Test
    void actionPolicyIncludesOnlyObjectFormActionsUnassociatedWithParameters() {
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

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> events = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> parents = new ArrayList<>();

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }

        @Override
        public void onStart(final Observation.Context context) {
            events.add("start:" + context.getName());
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
