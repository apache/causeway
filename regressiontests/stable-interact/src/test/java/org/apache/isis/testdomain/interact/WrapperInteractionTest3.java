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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.util.interaction.InteractionTestAbstract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,

                WrapperInteractionTest3.Task.class,
                WrapperInteractionTest3.Task.Succeeded.class,
                WrapperInteractionTest3.Task.Failed.class,
        }
)
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class WrapperInteractionTest3
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
        public class Succeeded
        extends MixinAbstract {
            public List<Task.Outcome> choices0Act() { return Task.Outcome.successes(); }
        }

        @Action
        public class Failed
        extends MixinAbstract {
            public List<Task.Outcome> choices0Act() { return Task.Outcome.failures(); }
        }

        // an abstract mixin class
        abstract class MixinAbstract {
            public Task act(Task.Outcome outcome) {
                Task.this.outcome = outcome;
                return Task.this;
            }
        }
    }

    @Inject SpecificationLoader specificationLoader;

    @Test
    void mixinMemberNamedFacet_whenSharingSameAbstractMixin() {

        val objectSpec1 = specificationLoader.specForType(Task.Succeeded.class).get();
        val objectSpec2 = specificationLoader.specForType(Task.Failed.class).get();

        assertTrue(objectSpec1.isMixin());
        assertTrue(objectSpec2.isMixin());

        assertEquals("Succeeded", objectSpec1.getSingularName());
        assertEquals("Failed", objectSpec2.getSingularName());

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

    @Test @Disabled("TODO[ISIS-1720]")
    void mixinActionValidation() {

        final Task task = new Task();

        wrapMixin(Task.Succeeded.class, task).act(Task.Outcome.SUPER);
        Assertions.assertThat(task).extracting(Task::getOutcome).isEqualTo(Task.Outcome.SUPER);

        wrapMixin(Task.Failed.class, task).act(Task.Outcome.JUST_GIVE_UP);
        Assertions.assertThat(task).extracting(Task::getOutcome).isEqualTo(Task.Outcome.JUST_GIVE_UP);
    }

}
