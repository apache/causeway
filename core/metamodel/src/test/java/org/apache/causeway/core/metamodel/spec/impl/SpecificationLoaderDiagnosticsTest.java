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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanTypeClassifier;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor.Substitution;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionRequest;
import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionState;

class SpecificationLoaderDiagnosticsTest {
    @Test void nestedRealLoadBoundaryRetainsFramesAndOriginalOverflowThenCleansUp() throws Exception {
        var f = new Fixture();
        var a = f.cached(A.class);
        var b = f.cached(B.class);
        var c = f.cached(C.class);
        var original = new StackOverflowError("controlled nested failure");
        doAnswer(call -> f.loader.loadSpecification(B.class, IntrospectionRequest.TYPE_ONLY))
                .when(a.handler).introspect(IntrospectionRequest.FULL);
        doAnswer(call -> f.loader.loadSpecification(C.class, IntrospectionRequest.FULL))
                .when(b.handler).introspect(IntrospectionRequest.TYPE_ONLY);
        doThrow(original).when(c.handler).introspect(IntrospectionRequest.FULL);
        var captures = new ArrayList<List<_IntrospectionDiagnostics.Entry>>();
        try (var ignored = _IntrospectionDiagnostics.capture(captures::add)) {
            assertSame(original, assertThrows(StackOverflowError.class, () -> f.loader.loadSpecification(A.class, IntrospectionRequest.FULL)));
            var deepest = captures.stream().filter(entries -> entries.size() == 3).reduce((first, last) -> last).orElseThrow();
            assertEquals(List.of(A.class.getName(), B.class.getName(), C.class.getName()), deepest.stream().map(_IntrospectionDiagnostics.Entry::typeName).toList());
            assertEquals(IntrospectionRequest.TYPE_ONLY, deepest.get(1).requestedRequest());
            assertEquals("TYPE_INTROSPECTED", deepest.getLast().currentState());
            assertTrue(deepest.getLast().caller().contains(getClass().getName()));
            assertSame(a.spec, f.loader.loadSpecification(A.class, IntrospectionRequest.REGISTER));
            assertEquals(1, captures.getLast().size());
            assertEquals(0, captures.getLast().getFirst().depth());
        }
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void phaseDowngradeAndCacheIdentityRemainUnchanged() throws Exception {
        var f = new Fixture();
        var cached = f.cached(A.class);
        f.loader.phase = SpecificationLoaderDefault.Phase.INTROSPECTING_MIXINS;
        var captures = new ArrayList<List<_IntrospectionDiagnostics.Entry>>();
        try (var ignored = _IntrospectionDiagnostics.capture(captures::add)) {
            assertSame(cached.spec, f.loader.loadSpecification(A.class, IntrospectionRequest.FULL));
            assertEquals(IntrospectionRequest.FULL, captures.getLast().getFirst().requestedRequest());
            assertEquals(IntrospectionRequest.TYPE_ONLY, captures.getLast().getFirst().effectiveRequest());
        }
        verify(cached.handler).introspect(IntrospectionRequest.TYPE_ONLY);
        when(cached.spec.isMixin()).thenReturn(true);
        assertSame(cached.spec, f.loader.loadSpecification(A.class, IntrospectionRequest.FULL));
        verify(cached.handler).introspect(IntrospectionRequest.FULL);
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void substitutionFilteredAndNullRequestsPreserveExistingOutcomes() throws Exception {
        var f = new Fixture();
        var substituted = f.cached(B.class);
        when(f.substitutions.getSubstitution(A.class)).thenReturn(Substitution.replaceWith(B.class));
        when(f.substitutions.getSubstitution(C.class)).thenReturn(Substitution.neverIntrospect());
        var captures = new ArrayList<List<_IntrospectionDiagnostics.Entry>>();
        try (var ignored = _IntrospectionDiagnostics.capture(captures::add)) {
            assertNull(f.loader.loadSpecification((Class<?>)null, IntrospectionRequest.FULL));
            assertTrue(captures.isEmpty());
            assertSame(substituted.spec, f.loader.loadSpecification(A.class, IntrospectionRequest.REGISTER));
            assertEquals(A.class.getName(), captures.getLast().getFirst().typeName());
            assertEquals(B.class.getName(), captures.getLast().getFirst().substitutedTypeName());
            assertNull(f.loader.loadSpecification(C.class, IntrospectionRequest.FULL));
        }
        verifyNoInteractions(f.classifier);
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void aliasRegistrationStillUsesOriginalTypeOnlyRequest() throws Exception {
        var f = new Fixture();
        var cached = f.cached(A.class);
        var resolver = mock(LogicalTypeResolver.class);
        set(f.loader, "logicalTypeResolver", resolver);
        when(cached.spec.aliases()).thenReturn(Can.of(LogicalType.eager(A.class, "diagnostic.Alias")));
        assertSame(cached.spec, f.loader.loadSpecification(A.class, IntrospectionRequest.TYPE_ONLY));
        verify(resolver).registerAliases(cached.spec);
        f.loader.loadSpecification(A.class, IntrospectionRequest.FULL);
        verifyNoMoreInteractions(resolver);
    }

    @Test void nonOverflowFailureIsUnchangedAndStateIsReleased() throws Exception {
        var f = new Fixture();
        var cached = f.cached(A.class);
        var failure = new IllegalStateException("normal failure");
        doThrow(failure).when(cached.handler).introspect(IntrospectionRequest.FULL);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> f.loader.loadSpecification(A.class, IntrospectionRequest.FULL)));
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void passiveStateReaderSupportsLegacyAndFacadeWithoutForcingIntrospection() throws Exception {
        var reader = SpecificationLoaderDefault.class.getDeclaredMethod("currentStateForDiagnostics", ObjectSpecificationInternal.class);
        reader.setAccessible(true);
        var f = new Fixture();
        var legacy = f.cached(A.class);
        assertEquals("TYPE_INTROSPECTED", reader.invoke(null, legacy.spec));
        verify(legacy.handler, never()).introspect(any());
        when(legacy.handler.introspectionState()).thenThrow(new IllegalStateException("diagnostic state unavailable"));
        assertNull(reader.invoke(null, legacy.spec));
        var facade = mock(ObjectSpecificationFacade.class);
        var metadata = List.<ObjectSpecificationFacade.ObjectMetaData>of(
                mock(ObjectSpecificationFacade.ObjectMetaDataInitial.class),
                mock(ObjectSpecificationFacade.ObjectMetaDataTypeOnly.class),
                mock(ObjectSpecificationFacade.ObjectMetaDataFull.class));
        var names = List.of("NOT_INTROSPECTED", "TYPE_INTROSPECTED", "FULLY_INTROSPECTED");
        for (int i = 0; i < metadata.size(); i++) {
            when(facade.objectMetaData()).thenReturn(metadata.get(i));
            assertEquals(names.get(i), reader.invoke(null, facade));
        }
        verify(facade, never()).isFullyIntrospected();
    }

    private record Cached(ObjectSpecificationDefault spec, IntrospectionStateHandler handler) { }
    private static final class Fixture {
        final CausewayBeanTypeClassifier classifier = mock(CausewayBeanTypeClassifier.class);
        final ClassSubstitutorRegistry substitutions = mock(ClassSubstitutorRegistry.class);
        final SpecificationLoaderDefault loader;
        Fixture() {
            when(substitutions.getSubstitution(any())).thenReturn(Substitution.neverReplaceClass());
            loader = new SpecificationLoaderDefault(mock(ProgrammingModel.class), mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS),
                    mock(CausewaySystemEnvironment.class), mock(ServiceRegistry.class), classifier, CausewayBeanTypeRegistry.empty(),
                    () -> mock(ValueSemanticsResolver.class), substitutions);
        }
        @SuppressWarnings("unchecked")
        Cached cached(final Class<?> type) throws Exception {
            var spec = mock(ObjectSpecificationDefault.class);
            var handler = mock(IntrospectionStateHandler.class);
            when(spec.introspectionStateHandler()).thenReturn(handler);
            when(handler.introspectionState()).thenReturn(IntrospectionState.TYPE_INTROSPECTED);
            when(spec.aliases()).thenReturn(Can.empty());
            var field = SpecificationLoaderDefault.class.getDeclaredField("cache");
            field.setAccessible(true);
            ((Map<Class<?>, ObjectSpecificationInternal>)field.get(loader)).put(type, spec);
            return new Cached(spec, handler);
        }
    }
    private static void set(final Object target, final String name, final Object value) throws Exception {
        var field = SpecificationLoaderDefault.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
    private static final class A { }
    private static final class B { }
    private static final class C { }
}
