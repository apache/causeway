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

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.services.wrapper.InvalidException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Data;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,
                WrapperInteractionTest2.Customer.class,
                WrapperInteractionTest2.Customer.ConcreteMixin.class,
                WrapperInteractionTest2.Customer.ConcreteMixin2.class,
        }
)
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class WrapperInteractionTest2
extends InteractionTestAbstract {

    @Data @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Customer {
        String name;
        @Action public String who() { return name; }

        @Action
        public class ConcreteMixin
        extends MixinAbstract<String> {

            @Override
            public String act(String startTime, String endTime) {
                return "acted";
            }
        }

        @Action
        public class ConcreteMixin2
        extends MixinAbstract<String> {

            @Override
            public String act(String startTime, String endTime) {
                return "acted2";
            }
        }

    }

    // an abstract mixin class
    static abstract class MixinAbstract<T extends Object> {
        public T act(String startTime, String endTime) {
            return null;
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
            wrapMixin(Customer.ConcreteMixin.class, new Customer()).act(null, "17:00");
        });
        assertEquals("'Start Time' is mandatory", cause.getMessage());

        InvalidException cause2 = assertThrows(InvalidException.class, ()-> {
            wrapMixin(Customer.ConcreteMixin2.class, new Customer()).act(null, "17:00");
        });
        assertEquals("'Start Time' is mandatory", cause2.getMessage());
    }

}
