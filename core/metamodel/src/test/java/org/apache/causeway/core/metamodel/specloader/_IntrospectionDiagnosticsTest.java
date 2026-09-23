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
package org.apache.causeway.core.metamodel.specloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistryDefault;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.postprocessors.members.navigation.SynthesizeNavigationActionsPostProcessor;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.IntrospectionState;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.RequiredArgsConstructor;
import lombok.val;

class _IntrospectionDiagnosticsTest {

    @Test
    void recordsOrderedEntriesWithStatesAndCaller() {
        final List<List<_IntrospectionDiagnostics.Entry>> observed = new ArrayList<>();

        try (_IntrospectionDiagnostics.Observation observation = _IntrospectionDiagnostics.observe(observed::add);
                _IntrospectionDiagnostics.Scope outer = _IntrospectionDiagnostics.enter(
                        Alpha.class,
                        IntrospectionState.FULLY_INTROSPECTED,
                        IntrospectionState.NOT_INTROSPECTED);
                _IntrospectionDiagnostics.Scope inner = _IntrospectionDiagnostics.enter(
                        Beta.class,
                        IntrospectionState.TYPE_INTROSPECTED,
                        IntrospectionState.TYPE_BEING_INTROSPECTED)) {
            assertEquals(2, observed.size());
            final List<_IntrospectionDiagnostics.Entry> deepest = observed.get(1);
            assertEquals(List.of(Alpha.class.getName(), Beta.class.getName()), typeNames(deepest));
            assertEquals(0, deepest.get(0).depth());
            assertEquals(IntrospectionState.FULLY_INTROSPECTED, deepest.get(0).requestedState());
            assertEquals(IntrospectionState.NOT_INTROSPECTED, deepest.get(0).currentState());
            assertTrue(deepest.get(0).caller().contains(getClass().getSimpleName()));
        }
    }

    @Test
    void reportsOnceWhileNestedScopesUnwindAndStartsCleanAfterward() {
        final List<String> reports = new ArrayList<>();

        try (_IntrospectionDiagnostics.Scope outer = _IntrospectionDiagnostics.enter(
                Alpha.class, IntrospectionState.FULLY_INTROSPECTED, IntrospectionState.NOT_INTROSPECTED);
                _IntrospectionDiagnostics.Scope inner = _IntrospectionDiagnostics.enter(
                        Beta.class, IntrospectionState.FULLY_INTROSPECTED, null)) {
            _IntrospectionDiagnostics.reportStackOverflow(reports::add);
            _IntrospectionDiagnostics.reportStackOverflow(reports::add);
        }

        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains(Alpha.class.getName()));
        assertTrue(reports.get(0).contains(Beta.class.getName()));
        assertTrue(reports.get(0).contains("current=unavailable"));

        try (_IntrospectionDiagnostics.Scope ignored = _IntrospectionDiagnostics.enter(
                Gamma.class, IntrospectionState.TYPE_INTROSPECTED, IntrospectionState.NOT_INTROSPECTED)) {
            _IntrospectionDiagnostics.reportStackOverflow(reports::add);
        }
        assertEquals(2, reports.size());
        assertTrue(reports.get(1).contains(Gamma.class.getName()));
        assertFalse(reports.get(1).contains(Alpha.class.getName()));
    }

    @Test
    void isolatesConcurrentThreads() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            final Future<List<String>> alpha = executor.submit(() -> observeThread(Alpha.class, Beta.class, barrier));
            final Future<List<String>> gamma = executor.submit(() -> observeThread(Gamma.class, Delta.class, barrier));

            assertEquals(List.of(Alpha.class.getName(), Beta.class.getName()), alpha.get());
            assertEquals(List.of(Gamma.class.getName(), Delta.class.getName()), gamma.get());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void representativeFixtureExposesMixinAndActionElementTypeLoading() {
        final List<List<_IntrospectionDiagnostics.Entry>> observed = new ArrayList<>();
        final List<String> reports = new ArrayList<>();
        val mmc = newMetamodelContext(NodeC_as.class, NodeB_cs.class, NodeA_bs.class);

        final ObjectSpecification nodeA;
        try (_IntrospectionDiagnostics.Observation observation = _IntrospectionDiagnostics.observe(snapshot -> {
            observed.add(snapshot);
            if(reports.isEmpty()
                    && snapshot.stream().anyMatch(entry -> entry.typeName().equals(NodeA_bs.class.getName()))) {
                _IntrospectionDiagnostics.reportStackOverflow(reports::add);
            }
        })) {
            nodeA = mmc.getSpecificationLoader().loadSpecification(NodeA.class);
            new SynthesizeNavigationActionsPostProcessor(mmc).postProcessObject(nodeA);
            mmc.getSpecificationLoader().loadSpecification(NodeC_as.class)
                    .streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
                    .forEach(ObjectAction::getElementType);
        }

        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains(NodeA.class.getName()));
        assertTrue(reports.get(0).contains(NodeA_bs.class.getName()));
        assertTrue(reports.get(0).contains("createMixedInAssociation"));

        final List<_IntrospectionDiagnostics.Entry> entries = observed.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        assertTrue(entries.stream().anyMatch(entry -> entry.typeName().equals(NodeA_bs.class.getName())));
        assertTrue(entries.stream().anyMatch(entry -> entry.typeName().equals(NodeB_cs.class.getName())));
        assertTrue(entries.stream().anyMatch(entry -> entry.typeName().equals(NodeC_as.class.getName())));
        assertTrue(entries.stream().anyMatch(entry -> entry.caller().contains("createMixedInAssociation")),
                "expected mixed-in association loading in the diagnostic chain");
        assertTrue(entries.stream().anyMatch(entry -> entry.caller().contains("elementSpec")),
                () -> "expected action element-type loading in the diagnostic chain; callers were "
                        + entries.stream()
                                .map(_IntrospectionDiagnostics.Entry::caller)
                                .distinct()
                                .collect(Collectors.toList()));
    }

    private List<String> observeThread(
            final Class<?> outerType,
            final Class<?> innerType,
            final CyclicBarrier barrier) throws Exception {
        final List<List<_IntrospectionDiagnostics.Entry>> observed = new ArrayList<>();
        try (_IntrospectionDiagnostics.Observation observation = _IntrospectionDiagnostics.observe(observed::add);
                _IntrospectionDiagnostics.Scope outer = _IntrospectionDiagnostics.enter(
                        outerType, IntrospectionState.FULLY_INTROSPECTED, IntrospectionState.NOT_INTROSPECTED)) {
            barrier.await();
            try (_IntrospectionDiagnostics.Scope inner = _IntrospectionDiagnostics.enter(
                    innerType, IntrospectionState.FULLY_INTROSPECTED, IntrospectionState.TYPE_INTROSPECTED)) {
                barrier.await();
            }
        }
        return typeNames(observed.get(observed.size() - 1));
    }

    private List<String> typeNames(final List<_IntrospectionDiagnostics.Entry> entries) {
        return entries.stream()
                .map(_IntrospectionDiagnostics.Entry::typeName)
                .collect(Collectors.toList());
    }

    private MetaModelContext_forTesting newMetamodelContext(final Class<?>... mixinTypes) {
        val mmc = MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .causewayBeanTypeRegistry(new CausewayBeanTypeRegistryDefault(Can.ofArray(mixinTypes)
                        .map(mixinType -> CausewayBeanMetaData.notManaged(BeanSort.MIXIN, mixinType))))
                .build();
        mmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
        return mmc;
    }

    static class Alpha {}
    static class Beta {}
    static class Gamma {}
    static class Delta {}

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class NodeA {}

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class NodeB {}

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class NodeC {}

    @RequiredArgsConstructor
    @org.apache.causeway.applib.annotation.Collection
    static class NodeA_bs {
        @SuppressWarnings("unused")
        private final NodeA mixee;
        @MemberSupport public List<NodeB> coll() {
            return new ArrayList<>();
        }
    }

    @RequiredArgsConstructor
    @org.apache.causeway.applib.annotation.Collection
    static class NodeB_cs {
        @SuppressWarnings("unused")
        private final NodeB mixee;
        @MemberSupport public List<NodeC> coll() {
            return new ArrayList<>();
        }
    }

    @Action(typeOf = NodeA.class)
    @RequiredArgsConstructor
    static class NodeC_as {
        @SuppressWarnings("unused")
        private final NodeC mixee;
        public List<NodeA> act() {
            return new ArrayList<>();
        }
    }
}
