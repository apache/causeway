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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,
                WrapperInteractionTest4.Task.class,
                WrapperInteractionTest4.Task.Succeeded.class,
                WrapperInteractionTest4.Task.Failed.class,
        }
)
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class WrapperInteractionTest4
extends InteractionTestAbstract {

    @Data @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Task {

        @RequiredArgsConstructor
        enum Outcome {
            SUPER(true),
            GREAT(true),
            OK(true),
            BAD(false),
            TERRIBLE(false),
            JUST_GIVE_UP(false),;

            final boolean representsSuccess;

            public static List<Outcome> successes() {
                return Arrays.stream(Outcome.values()).filter(x -> x.representsSuccess).collect(Collectors.toList());
            }
            public static List<Outcome> failures() {
                return Arrays.stream(Outcome.values()).filter(x -> ! x.representsSuccess).collect(Collectors.toList());
            }
        }

        @Property(optionality = Optionality.OPTIONAL)
        Outcome outcome;

        @Action
        public static class Succeeded
        extends MixinAbstract {
            public Succeeded(final Task task) { super(task); }
            public List<Task.Outcome> choices0Act() { return Task.Outcome.successes(); }
        }

        @Action
        public static class Failed
        extends MixinAbstract {
            public Failed(final Task task) { super(task); }
            public List<Task.Outcome> choices0Act() { return Task.Outcome.failures(); }
        }

        // an abstract mixin class
        @RequiredArgsConstructor
        abstract static class MixinAbstract {
            private final Task task;
            public Task act(final Task.Outcome outcome) {
                task.outcome = outcome;
                return task;
            }
        }
    }


    @Inject SpecificationLoader specificationLoader;

    @Test
    void mixinMemberNamedFacet_whenSharingSameAbstractMixin() {
        val objectSpec = specificationLoader.specForType(Task.class).get();

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

  //FIXME[CAUSEWAY-3207]
    @DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
    @Test
    void mixinActionValidation() {

        final Task task = new Task();

        wrapMixin(Task.Succeeded.class, task).act(Task.Outcome.SUPER);
        Assertions.assertThat(task).extracting(Task::getOutcome).isEqualTo(Task.Outcome.SUPER);

        wrapMixin(Task.Failed.class, task).act(Task.Outcome.JUST_GIVE_UP);
        Assertions.assertThat(task).extracting(Task::getOutcome).isEqualTo(Task.Outcome.JUST_GIVE_UP);
    }

}
