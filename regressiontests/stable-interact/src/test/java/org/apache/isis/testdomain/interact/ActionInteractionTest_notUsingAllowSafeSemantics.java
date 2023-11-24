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
package org.apache.isis.testdomain.interact;

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.security.authorization.Authorizor;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.testdomain.util.interaction.InteractionTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,
                ActionInteractionTest_notUsingAllowSafeSemantics.AuthorizorDenyUse.class
        },
        properties = {
                "isis.security.actionsWithSafeSemanticsRequireOnlyViewingPermission=FALSE",
                "isis.core.meta-model.introspector.mode=FULL",
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class ActionInteractionTest_notUsingAllowSafeSemantics extends InteractionTestAbstract {

    @Service
    @Named("regressiontests.AuthorizorDenyUse")
    @javax.annotation.Priority(PriorityPrecedence.EARLY)
    @Qualifier("Testing")
    public static class AuthorizorDenyUse implements Authorizor {

        @Override
        public boolean isVisible(final InteractionContext authentication, final Identifier identifier) {
            return true; // grant view of any action (for testing)
        }

        @Override
        public boolean isUsable(final InteractionContext authentication, final Identifier identifier) {
            return false; // deny use of any action (for testing)
        }

    }

    @Test
    void assert_prereq() {
        val config = super.objectManager.getConfiguration();
        assertFalse(config.getSecurity().isActionsWithSafeSemanticsRequireOnlyViewingPermission());
    }

    @Test
    void whenSafeAction_shouldDenyUse() {
        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "actSafely", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();
        val veto = actionInteraction.getInteractionVeto().orElseThrow(); // should not throw
        assertEquals("Not authorized to edit", veto.getReasonAsString().orElse(null));
    }

    @Test
    void whenNonSafeAction_shouldDenyUse() {
        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "actUnsafely", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();
        val veto = actionInteraction.getInteractionVeto().orElseThrow(); // should not throw
        assertEquals("Not authorized to edit", veto.getReasonAsString().orElse(null));
    }

}
