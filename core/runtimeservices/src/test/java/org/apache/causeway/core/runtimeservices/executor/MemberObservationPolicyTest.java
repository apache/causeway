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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.InteractionLayerTracker;
import org.apache.causeway.core.metamodel.execution.PropertyModifier;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.runtimeservices.publish.ExecutionPublisherDefault;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class MemberObservationPolicyTest {
    @Test void propertyMetadataDoesNotChangeInvocationOrIncludeValues() {
        var stopped = new ArrayList<Observation.Context>();
        var integration = integration(stopped);
        var service = new MemberExecutorServiceDefault(null, null, null, null, null, null, null, integration);
        for (String name : new String[]{"first", "second", null}) {
            var modifier = mock(PropertyModifier.class, RETURNS_DEEP_STUBS);
            var id = name == null ? null : Identifier.propertyIdentifier(LogicalType.fqcn(getClass()), name);
            when(modifier.owningProperty().getFeatureIdentifier()).thenReturn(id);
            when(modifier.interactionInitiatedBy()).thenReturn(InteractionInitiatedBy.PASS_THROUGH);
            var target = ManagedObject.unspecified();
            var head = modifier.head();
            doReturn(target).when(head).target();
            var spec = mock(org.apache.causeway.core.metamodel.spec.ObjectSpecification.class);
            var loader = mock(org.apache.causeway.core.metamodel.specloader.SpecificationLoader.class);
            when(spec.getSpecificationLoader()).thenReturn(loader);
            when(loader.specForType(String.class)).thenReturn(java.util.Optional.of(spec));
            var newValue = ManagedObject.other(spec, "sentinel-property-value");
            doReturn(newValue).when(modifier).newValue();
            assertSame(target, service.setOrClearProperty(modifier));
            verify(modifier).executeClearOrSetWithoutEvents(newValue);
            var context = stopped.get(stopped.size() - 1);
            assertEquals("causeway.member.property-update", context.getName());
            assertEquals(id == null ? "Property Update" : "Property Update " + id, context.getContextualName());
            assertEquals(id != null, context.getLowCardinalityKeyValue("causeway.member.id") != null);
            assertFalse(context.getAllKeyValues().toString().contains("sentinel"));
        }
    }
    public static class Target {
        public String call() { return "sentinel-result"; }
    }
    @Test void actionMetadataPreservesPhysicalInvocationAndStableOperation() throws Exception {
        var stopped = new ArrayList<Observation.Context>();
        var service = new MemberExecutorServiceDefault(null, null, null, null, null, null, null, integration(stopped));
        for (String name : new String[]{"first", "second"}) {
            var executor = mock(org.apache.causeway.core.metamodel.execution.ActionExecutor.class, RETURNS_DEEP_STUBS);
            var id = Identifier.actionIdentifier(LogicalType.fqcn(Target.class), name);
            when(executor.owningAction().getFeatureIdentifier()).thenReturn(id);
            when(executor.interactionInitiatedBy()).thenReturn(InteractionInitiatedBy.PASS_THROUGH);
            when(executor.arguments()).thenReturn(org.apache.causeway.commons.collections.Can.empty());
            var spec = mock(org.apache.causeway.core.metamodel.spec.ObjectSpecification.class);
            var loader = mock(org.apache.causeway.core.metamodel.specloader.SpecificationLoader.class);
            when(spec.getSpecificationLoader()).thenReturn(loader);
            when(loader.specForType(Target.class)).thenReturn(java.util.Optional.of(spec));
            var target = ManagedObject.other(spec, new Target());
            var head = executor.head();
            doReturn(target).when(head).target();
            var method = org.apache.causeway.commons.internal.reflection._MethodFacades.testing.regular(
                    Target.class.getDeclaredMethod("call"));
            doReturn(method).when(executor).method();
            var manager = executor.facetHolder().getObjectManager();
            doReturn(ManagedObject.unspecified()).when(manager).adapt("sentinel-result");
            assertSame(ManagedObject.unspecified(), service.invokeAction(executor));
            verify(manager).adapt("sentinel-result");
            var context = stopped.get(stopped.size() - 1);
            assertEquals("causeway.member.action", context.getName());
            assertEquals(org.apache.causeway.core.config.observation.CausewayObservationNaming.forMember(
                    "act", id.logicalTypeName(), id.memberLogicalName()), context.getContextualName());
            assertEquals(id.getLogicalIdentityString("#"), context.getLowCardinalityKeyValue("causeway.action.id").getValue());
            assertEquals(id.toString(), context.getLowCardinalityKeyValue("causeway.member.id").getValue());
            assertFalse(context.getAllKeyValues().toString().contains("sentinel"));
        }
    }
    @Test void mixedInAssociationResolutionSelectsActualContribution() throws Exception {
        var spec = mock(org.apache.causeway.core.metamodel.spec.ObjectSpecification.class);
        var loader = mock(org.apache.causeway.core.metamodel.specloader.SpecificationLoader.class);
        when(spec.getSpecificationLoader()).thenReturn(loader);
        when(loader.specForType(Target.class)).thenReturn(java.util.Optional.of(spec));
        var owner = ManagedObject.other(spec, new Target());
        var head = mock(org.apache.causeway.core.metamodel.interactions.InteractionHead.class);
        when(head.owner()).thenReturn(owner);
        var action = mock(org.apache.causeway.core.metamodel.spec.feature.ObjectAction.class);
        var property = association(action, true);
        var unrelated = association(mock(org.apache.causeway.core.metamodel.spec.feature.ObjectAction.class), false);
        var facet = mock(org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForMixedInPropertyOrCollection.class);
        var executor = new org.apache.causeway.core.metamodel.execution.ActionExecutor(null, null,
                InteractionInitiatedBy.PASS_THROUGH, action, null, head, null, facet, null);
        when(spec.streamAssociations(org.apache.causeway.core.metamodel.spec.feature.MixedIn.INCLUDED))
                .thenAnswer(__ -> java.util.stream.Stream.of(unrelated, property));
        assertSame(property, executor.mixedInAssociation().orElseThrow());
        when(spec.streamAssociations(org.apache.causeway.core.metamodel.spec.feature.MixedIn.INCLUDED))
                .thenAnswer(__ -> java.util.stream.Stream.of(unrelated));
        assertTrue(executor.mixedInAssociation().isEmpty());
        var ordinary = new org.apache.causeway.core.metamodel.execution.ActionExecutor(null, null,
                InteractionInitiatedBy.PASS_THROUGH, action, null, head, null,
                mock(org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract.class), null);
        assertTrue(ordinary.mixedInAssociation().isEmpty());
    }

    @Test void associationCategoriesAndFallbackKeepWorkObservable() throws Exception {
        for (int kind = 0; kind < 3; kind++) {
            var stopped = new ArrayList<Observation.Context>();
            var executor = mock(org.apache.causeway.core.metamodel.execution.ActionExecutor.class, RETURNS_DEEP_STUBS);
            var invokedId = Identifier.actionIdentifier(LogicalType.fqcn(Target.class), "call", String.class);
            when(executor.owningAction().getFeatureIdentifier()).thenReturn(invokedId);
            when(executor.interactionInitiatedBy()).thenReturn(InteractionInitiatedBy.PASS_THROUGH);
            when(executor.arguments()).thenReturn(org.apache.causeway.commons.collections.Can.empty());
            var spec = mock(org.apache.causeway.core.metamodel.spec.ObjectSpecification.class);
            var loader = mock(org.apache.causeway.core.metamodel.specloader.SpecificationLoader.class);
            when(spec.getSpecificationLoader()).thenReturn(loader);
            when(loader.specForType(Target.class)).thenReturn(java.util.Optional.of(spec));
            var head = executor.head();
            doReturn(ManagedObject.other(spec, new Target())).when(head).target();
            doReturn(org.apache.causeway.commons.internal.reflection._MethodFacades.testing.regular(Target.class.getDeclaredMethod("call")))
                    .when(executor).method();
            var manager = executor.facetHolder().getObjectManager();
            doReturn(ManagedObject.unspecified()).when(manager).adapt("sentinel-result");
            var association = association(executor.owningAction(), kind == 0);
            var associationId = Identifier.propertyIdentifier(LogicalType.eager(Target.class, "domain.Owner"), kind == 0 ? "name" : "items");
            when(association.getFeatureIdentifier()).thenReturn(associationId);
            when(executor.mixedInAssociation()).thenReturn(kind < 2 ? java.util.Optional.of(association) : java.util.Optional.empty());
            new MemberExecutorServiceDefault(null, null, null, null, null, null, null, integration(stopped)).invokeAction(executor);
            var context = stopped.get(stopped.size() - 1);
            var key = kind == 0 ? "causeway.property.id" : kind == 1 ? "causeway.collection.id" : "causeway.action.id";
            assertEquals(kind == 0 ? "causeway.property.access" : kind == 1 ? "causeway.collection.access" : "causeway.member.action", context.getName());
            assertEquals((kind < 2 ? associationId : invokedId).getLogicalIdentityString("#"), context.getLowCardinalityKeyValue(key).getValue());
            if (kind < 2) assertNull(context.getLowCardinalityKeyValue("causeway.action.id"));
            assertEquals(invokedId.toString(), context.getLowCardinalityKeyValue("causeway.member.id").getValue());
            assertFalse(context.getAllKeyValues().toString().contains("sentinel"));
        }
    }

    private org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation association(
            org.apache.causeway.core.metamodel.spec.feature.ObjectAction action, boolean singular) {
        var association = mock(org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation.class,
                withSettings().extraInterfaces(org.apache.causeway.core.metamodel.spec.feature.MixedInMember.class));
        when(((org.apache.causeway.core.metamodel.spec.feature.MixedInMember) association).hasMixinAction(action)).thenReturn(true);
        when(association.isSingular()).thenReturn(singular);
        return association;
    }

    @Test void subscriberCountsDoNotChangeNamesOrMeterDimensions() {
        var stopped = new ArrayList<Observation.Context>();
        var integration = integration(stopped);
        for (int count : new int[]{1, 3}) {
            var subscribers = new ArrayList<ExecutionSubscriber>();
            for (int i = 0; i < count; i++) {
                var subscriber = mock(ExecutionSubscriber.class);
                when(subscriber.isEnabled()).thenReturn(true);
                subscribers.add(subscriber);
            }
            var execution = mock(Execution.class);
            new ExecutionPublisherDefault(mock(InteractionLayerTracker.class), subscribers, integration)
                    .publishActionInvocation(execution);
            subscribers.forEach(s -> verify(s).onExecution(execution));
            var context = stopped.get(stopped.size() - 1);
            assertEquals("causeway.execution.publish", context.getName());
            assertEquals("Execution Publishing", context.getContextualName());
            assertNull(context.getLowCardinalityKeyValue("causeway.execution.subscriber-count"));
            assertEquals(Integer.toString(count), context.getHighCardinalityKeyValue("causeway.execution.subscriber-count").getValue());
        }
    }
    private CausewayObservationIntegration integration(List<Observation.Context> stopped) {
        var registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            public boolean supportsContext(Observation.Context context) { return true; }
            public void onStop(Observation.Context context) { stopped.add(context); }
        });
        return new CausewayObservationIntegration(registry);
    }
}
