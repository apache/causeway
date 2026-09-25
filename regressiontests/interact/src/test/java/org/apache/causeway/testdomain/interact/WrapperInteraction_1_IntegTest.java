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
package org.apache.causeway.testdomain.interact;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.iactn.ActionInvocation.RuleChecking;
import org.apache.causeway.applib.services.wrapper.InvalidException;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo_biArgEnabled;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,
                WrapperInteraction_1_IntegTest.Customer.class,
                WrapperInteraction_1_IntegTest.ConcreteMixin.class,
                WrapperInteraction_1_IntegTest.ConcreteMixin2.class,
                WrapperInteraction_1_IntegTest.InvocationProbe.class,
                WrapperInteraction_1_IntegTest.InvocationProbe_outer.class,
                WrapperInteraction_1_IntegTest.InvocationProbe_inner.class,
                WrapperInteraction_1_IntegTest.InvocationProbe_skipRules.class,
                WrapperInteraction_1_IntegTest.InvocationProbe_direct.class,
        }
)
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class WrapperInteraction_1_IntegTest
extends InteractionTestAbstract {

    @Data @DomainObject(nature = Nature.VIEW_MODEL)
    static class Customer {
        String name;
        @Action public String who() { return name; }
    }

    // an abstract mixin class
    static abstract class MixinAbstract<T extends Object> {
        public T act(final String startTime, final String endTime) {
            return null;
        }
    }

    @Action
    @RequiredArgsConstructor
    public static class ConcreteMixin
    extends MixinAbstract<String> {
        @SuppressWarnings("unused")
        private final Customer mixee;
        @Override
        public String act(final String startTime, final String endTime) {
            return "acted";
        }
    }

    @Action
    @RequiredArgsConstructor
    public static class ConcreteMixin2
    extends MixinAbstract<String> {
        @SuppressWarnings("unused")
        private final Customer mixee;

        @Override
        public String act(final String startTime, final String endTime) {
            return "acted2";
        }
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class InvocationProbe {
        private org.apache.causeway.applib.services.iactnlayer.InteractionService interactionService;
        private WrapperFactory wrapperFactory;
        private boolean outerCurrent;
        private boolean innerCurrent;
        private boolean outerCurrentDuringInner;
        private boolean outerRestored;
        private boolean skipRulesCurrent;
        private boolean directCurrent;
        private InvocationProbe_outer outerMixin;
    }

    @Action
    @RequiredArgsConstructor
    public static class InvocationProbe_outer {
        private final InvocationProbe mixee;

        public void act() {
            mixee.outerMixin = this;
            mixee.outerCurrent = mixee.interactionService
                    .isCurrentActionInvocation(this, "act", RuleChecking.CHECKED);
            mixee.wrapperFactory.wrapMixin(InvocationProbe_inner.class, mixee).act();
            mixee.outerRestored = mixee.interactionService
                    .isCurrentActionInvocation(this, "act", RuleChecking.CHECKED);
        }
    }

    @Action
    @RequiredArgsConstructor
    public static class InvocationProbe_inner {
        private final InvocationProbe mixee;

        public void act() {
            mixee.innerCurrent = mixee.interactionService
                    .isCurrentActionInvocation(this, "act", RuleChecking.CHECKED);
            mixee.outerCurrentDuringInner = mixee.interactionService
                    .isCurrentActionInvocation(mixee.outerMixin, "act", RuleChecking.CHECKED);
        }
    }

    @Action
    @RequiredArgsConstructor
    public static class InvocationProbe_skipRules {
        private final InvocationProbe mixee;

        public void act() {
            mixee.skipRulesCurrent = mixee.interactionService
                    .isCurrentActionInvocation(this, "act", RuleChecking.SKIPPED);
        }
    }

    @Action
    @RequiredArgsConstructor
    public static class InvocationProbe_direct {
        private final InvocationProbe mixee;

        public void act() {
            mixee.directCurrent = mixee.interactionService
                    .isCurrentActionInvocation(this, "act");
        }
    }

    @Inject SpecificationLoader specificationLoader;

    @Test
    void mixinMemberNamedFacet_whenSharingSameAbstractMixin() {
        val objectSpec = specificationLoader.specForType(Customer.class).get();

        assertEquals(
                2L,
                objectSpec.streamRuntimeActions(MixedIn.INCLUDED)
                .filter(ObjectAction::isMixedIn)
                .peek(act->{
                    //System.out.println("act: " + act);
                    val memberNamedFacet = act.getFacet(MemberNamedFacet.class);
                    assertNotNull(memberNamedFacet);
                    assertTrue(memberNamedFacet.getSpecialization().isLeft());
                })
                .count());
    }

    @Test
    void mixinActionValidation() {
        InvalidException cause = assertThrows(InvalidException.class, ()-> {
            wrapMixin(ConcreteMixin.class, new Customer()).act(null, "17:00");
        });
        assertEquals("'Start Time' is mandatory", cause.getMessage());

        InvalidException cause2 = assertThrows(InvalidException.class, ()-> {
            wrapMixin(ConcreteMixin2.class, new Customer()).act(null, "17:00");
        });
        assertEquals("'Start Time' is mandatory", cause2.getMessage());
    }

    @Test
    void regularPropertyAccess() {
        assertEquals("initial", wrapper.wrap(new InteractionDemo()).getString2());
    }

    @Test
    void mixinActionAccess() {
        assertEquals(3, wrapper.wrapMixin(InteractionDemo_biArgEnabled.class, new InteractionDemo()).act(1, 2));
    }

    @Test
    void currentActionInvocationTracksNestedWrapperExecution() {
        var probe = new InvocationProbe();
        probe.interactionService = interactionService;
        probe.wrapperFactory = wrapper;

        assertTrue(interactionService.currentActionInvocation().isEmpty());

        wrapper.wrapMixin(InvocationProbe_outer.class, probe).act();

        assertTrue(probe.outerCurrent);
        assertTrue(probe.innerCurrent);
        assertFalse(probe.outerCurrentDuringInner);
        assertTrue(probe.outerRestored);
        assertTrue(interactionService.currentActionInvocation().isEmpty());
    }

    @Test
    void currentActionInvocationIncludesSkipRulesWrapperExecution() {
        var probe = new InvocationProbe();
        probe.interactionService = interactionService;

        wrapper.wrapMixin(
                InvocationProbe_skipRules.class,
                probe,
                SyncControl.control().withSkipRules())
                .act();

        assertTrue(probe.skipRulesCurrent);
    }

    @Test
    void currentActionInvocationExcludesDirectJavaCalls() {
        var probe = new InvocationProbe();
        probe.interactionService = interactionService;

        new InvocationProbe_direct(probe).act();

        assertFalse(probe.directCurrent);
    }


}
