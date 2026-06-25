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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.NavigationActionSynthesis;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistryDefault;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.postprocessors.members.navigation.SynthesizeNavigationActionsPostProcessor;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Verifies that the {@code POST_PROCESS} navigation-action-synthesis strategy produces the same synthetic
 * navigation actions as the (default) {@code INLINE} strategy, for own and mixed-in parented collections,
 * including across a cyclic collection graph.
 *
 * <p>
 * The unit-test harness ({@link MetaModelContext_forTesting} + direct {@code loadSpecification}) does not run
 * the post-processing sweep (that only happens via {@code SpecificationLoader#createMetaModel()} at boot), so
 * under {@code POST_PROCESS} the synthesizing post-processor is invoked here exactly as the production sweep
 * would invoke it ({@link SynthesizeNavigationActionsPostProcessor#postProcessObject(ObjectSpecification)}).
 * The end-to-end StackOverflow regression on a cyclic graph is better covered by a full-boot integration test.
 */
class SynthesizeNavigationActionsPostProcessorTest {

    private static final String PREFIX =
            ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil.ACTION_ID_PREFIX;

    // -- OWN COLLECTION

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class NavParent {
        @Getter private final List<NavChild> children = new ArrayList<>();
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class NavChild {
        @Getter private String name;
    }

    // -- MIXED-IN COLLECTION

    @RequiredArgsConstructor
    @org.apache.causeway.applib.annotation.Collection
    static class NavParent_mixedChildren {
        private final NavParent mixee;
        @MemberSupport public List<NavChild> coll() {
            return mixee.getChildren();
        }
    }

    // -- CYCLE: two view models referencing each other via mixed-in collections

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class CycleA {
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class CycleB {
    }

    @RequiredArgsConstructor
    @org.apache.causeway.applib.annotation.Collection
    static class CycleA_bs {
        private final CycleA mixee;
        @MemberSupport public List<CycleB> coll() {
            return new ArrayList<>();
        }
    }

    @RequiredArgsConstructor
    @org.apache.causeway.applib.annotation.Collection
    static class CycleB_as {
        private final CycleB mixee;
        @MemberSupport public List<CycleA> coll() {
            return new ArrayList<>();
        }
    }

    @ParameterizedTest
    @EnumSource(NavigationActionSynthesis.class)
    void synthesizes_navigation_action_for_own_collection(final NavigationActionSynthesis strategy) {
        val mmc = newMetamodelContext(strategy);

        val parentSpec = mmc.getSpecificationLoader().loadSpecification(NavParent.class);
        synthesizeIfPostProcess(mmc, strategy, parentSpec);

        assertTrue(parentSpec.getAction(PREFIX + "children").isPresent(),
                () -> "expected own-collection navigation action under strategy " + strategy);
    }

    @ParameterizedTest
    @EnumSource(NavigationActionSynthesis.class)
    void synthesizes_navigation_action_for_mixed_in_collection(final NavigationActionSynthesis strategy) {
        val mmc = newMetamodelContext(strategy, NavParent_mixedChildren.class);

        val parentSpec = mmc.getSpecificationLoader().loadSpecification(NavParent.class);
        synthesizeIfPostProcess(mmc, strategy, parentSpec);

        assertTrue(parentSpec.getAction(PREFIX + "mixedChildren").isPresent(),
                () -> "expected mixed-in-collection navigation action under strategy " + strategy);
    }

    @ParameterizedTest
    @EnumSource(NavigationActionSynthesis.class)
    void synthesizes_navigation_actions_across_cyclic_collection_graph(final NavigationActionSynthesis strategy) {
        val mmc = newMetamodelContext(strategy, CycleA_bs.class, CycleB_as.class);

        val specA = mmc.getSpecificationLoader().loadSpecification(CycleA.class);
        val specB = mmc.getSpecificationLoader().loadSpecification(CycleB.class);

        // synthesis reads the *other* type's associations; must terminate and not throw
        assertDoesNotThrow(() -> synthesizeIfPostProcess(mmc, strategy, specA, specB),
                () -> "navigation-action synthesis failed on cyclic graph under strategy " + strategy);

        assertTrue(specA.getAction(PREFIX + "bs").isPresent(),
                () -> "expected navigation action on CycleA under strategy " + strategy);
        assertTrue(specB.getAction(PREFIX + "as").isPresent(),
                () -> "expected navigation action on CycleB under strategy " + strategy);
    }

    // -- HELPER

    /**
     * Under INLINE, synthesis already happened during introspection / first action access; this is a no-op.
     * Under POST_PROCESS, invoke the synthesizing post-processor as the production post-process sweep would.
     */
    private void synthesizeIfPostProcess(
            final MetaModelContext_forTesting mmc,
            final NavigationActionSynthesis strategy,
            final ObjectSpecification... specs) {
        if (!strategy.isPostProcess()) {
            return;
        }
        val postProcessor = new SynthesizeNavigationActionsPostProcessor(mmc);
        for (val spec : specs) {
            postProcessor.postProcessObject(spec);
        }
    }

    private MetaModelContext_forTesting newMetamodelContext(
            final NavigationActionSynthesis strategy,
            final Class<?>... mixinTypes) {
        val mmc = MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .causewayBeanTypeRegistry(new CausewayBeanTypeRegistryDefault(Can.ofArray(mixinTypes)
                        .map(mixinType -> CausewayBeanMetaData.notManaged(BeanSort.MIXIN, mixinType))))
                .build();
        val commandLog = mmc.getConfiguration().getExtensions().getCommandLog();
        commandLog.setRecordingSupport(RecordingSupport.ENABLED);
        commandLog.setNavigationActionSynthesis(strategy);
        return mmc;
    }

}
