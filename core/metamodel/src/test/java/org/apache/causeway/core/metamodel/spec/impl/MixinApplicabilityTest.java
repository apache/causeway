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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

/** Bootstrap loads all mixins; per-target discovery must only read applicable candidates. */
class MixinApplicabilityTest {
    @Test void exactSuperclassAndOrdinaryInterfaceCandidatesAreSelected() {
        var f = new Fixture();
        var exact = f.candidate(ExactMixin.class, Target.class);
        var inherited = f.candidate(BaseMixin.class, Base.class);
        var viaInterface = f.candidate(InterfaceMixin.class, Capability.class);
        f.candidate(UnrelatedMixin.class, Other.class);
        f.withoutFacet(MissingFacet.class);
        var selected = f.index().streamMixinSpecsFor(target(Target.class)).toList();
        assertEquals(3, selected.size());
        assertTrue(selected.containsAll(List.of(exact, inherited, viaInterface)));
    }

    @Test void emptyRegistryHasNoCandidates() {
        var index = new Fixture().index();
        assertEquals(0, index.streamMixinSpecs().count());
        assertEquals(0, index.streamMixinSpecsFor(target(Target.class)).count());
    }

    @Test void unrelatedAndMissingFacetCandidatesAreExcluded() {
        var f = new Fixture();
        f.candidate(UnrelatedMixin.class, Other.class);
        f.withoutFacet(MissingFacet.class);
        var index = f.index();
        assertEquals(1, index.streamMixinSpecs().count());
        assertEquals(0, index.streamMixinSpecsFor(target(Target.class)).count());
    }

    @Test void bothDiscoveryPathsReadApplicableMembersButNeverRejectedMembers() {
        var f = new Fixture();
        var accepted = f.candidate(BaseMixin.class, Base.class);
        var rejected = f.candidate(UnrelatedMixin.class, Other.class);
        var missing = f.withoutFacet(MissingFacet.class);
        var index = f.index();
        clearInvocations(f.loader); // Eager bootstrap requests are intentional, not per-target work.
        var factory = new MixedInMemberFactory(target(Target.class), index);
        assertTrue(factory.createMixedInActions().isEmpty());
        assertTrue(factory.createMixedInAssociations().isEmpty());
        verify(accepted, times(2)).streamActions(ActionScope.ANY, MixedIn.EXCLUDED);
        verify(rejected, never()).streamActions(ActionScope.ANY, MixedIn.EXCLUDED);
        verify(missing, never()).streamActions(ActionScope.ANY, MixedIn.EXCLUDED);
        verifyNoInteractions(f.loader);
    }

    @Test void repeatedSelectionReusesIndexWithoutReloadingCandidates() {
        var f = new Fixture();
        var candidate = f.candidate(BaseMixin.class, Base.class);
        var unrelated = f.candidate(UnrelatedMixin.class, Other.class);
        var index = f.index();
        verify(f.loader).specForTypeElseFail(BaseMixin.class);
        verify(f.loader).specForTypeElseFail(UnrelatedMixin.class);
        clearInvocations(f.loader);
        for (int i = 0; i < 10; i++) {
            assertEquals(List.of(candidate), index.streamMixinSpecsFor(target(Target.class)).toList());
            assertEquals(List.of(unrelated), index.streamMixinSpecsFor(target(Other.class)).toList());
        }
        verifyNoInteractions(f.loader);
    }

    @Test void contributingServiceRejectsObjectMixinBeforeMemberAccess() {
        var f = new Fixture();
        var candidate = f.candidate(ObjectMixin.class, Object.class);
        var index = f.index();
        var service = target(Target.class);
        when(service.beanSort()).thenReturn(BeanSort.MANAGED_BEAN_CONTRIBUTING);
        when(service.isEntityOrViewModelOrAbstract()).thenReturn(false);
        var factory = new MixedInMemberFactory(service, index);
        assertTrue(factory.createMixedInActions().isEmpty());
        verify(candidate, never()).streamActions(ActionScope.ANY, MixedIn.EXCLUDED);
    }

    @Test void ineligibleTargetDoesNotConsultDiscoveryStream() {
        var spec = target(Target.class);
        when(spec.isEntityOrViewModelOrAbstract()).thenReturn(false);
        when(spec.beanSort()).thenReturn(BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING);
        var streamer = mock(MixinSpecStreamer.class);
        var factory = new MixedInMemberFactory(spec, streamer);
        assertTrue(factory.createMixedInActions().isEmpty());
        assertTrue(factory.createMixedInAssociations().isEmpty());
        verifyNoInteractions(streamer);
    }

    private static ObjectSpecification target(final Class<?> type) {
        var spec = mock(ObjectSpecification.class);
        when(spec.correspondingClass()).thenAnswer(call -> type);
        when(spec.isEntityOrViewModelOrAbstract()).thenReturn(true);
        when(spec.beanSort()).thenReturn(BeanSort.VIEW_MODEL);
        return spec;
    }

    private static final class Fixture {
        final SpecificationLoader loader = mock(SpecificationLoader.class);
        final Map<Class<?>, ObjectSpecification> specs = new HashMap<>();
        Fixture() {
            when(loader.specForTypeElseFail(any())).thenAnswer(call -> specs.get(call.getArgument(0)));
        }
        ObjectSpecification withoutFacet(final Class<?> type) {
            var spec = mock(ObjectSpecification.class);
            when(spec.mixinFacet()).thenReturn(Optional.empty());
            specs.put(type, spec);
            return spec;
        }
        ObjectSpecification candidate(final Class<?> type, final Class<?> mixeeType) {
            var spec = withoutFacet(type);
            var facet = mock(MixinFacet.class);
            when(facet.mixeeType()).thenAnswer(call -> mixeeType);
            when(facet.mainMethodName()).thenReturn("act");
            when(facet.isMixinFor(any())).thenAnswer(call -> mixeeType.isAssignableFrom(call.getArgument(0)));
            when(spec.mixinFacet()).thenReturn(Optional.of(facet));
            when(spec.mixinFacetElseFail()).thenReturn(facet);
            when(spec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)).thenAnswer(call -> Stream.empty());
            return spec;
        }
        MixinSpecStreamerEager index() {
            var registry = mock(CausewayBeanTypeRegistry.class);
            when(registry.streamMixinTypes()).thenAnswer(call -> specs.keySet().stream().sorted(Comparator.comparing(Class::getName)));
            return new MixinSpecStreamerEager(loader, registry);
        }
    }
    private interface Capability { }
    private static class Base { }
    private static final class Target extends Base implements Capability { }
    private static final class Other { }
    private static final class ExactMixin { }
    private static final class BaseMixin { }
    private static final class InterfaceMixin { }
    private static final class UnrelatedMixin { }
    private static final class ObjectMixin { }
    private static final class MissingFacet { }
}
