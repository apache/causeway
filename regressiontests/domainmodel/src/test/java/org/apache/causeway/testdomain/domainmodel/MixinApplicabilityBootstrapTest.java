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
package org.apache.causeway.testdomain.domainmodel;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.domainmodel.mixinapplicability.Configuration_usingMixinApplicability;
import org.apache.causeway.testdomain.domainmodel.mixinapplicability.Configuration_usingMixinApplicability.Base;
import org.apache.causeway.testdomain.domainmodel.mixinapplicability.Configuration_usingMixinApplicability.Child;
import org.apache.causeway.testdomain.domainmodel.mixinapplicability.Configuration_usingMixinApplicability.Other;
import org.apache.causeway.testdomain.domainmodel.mixinapplicability.Configuration_usingMixinApplicability.Subtype;

class MixinApplicabilityBootstrapTest {
    @Test void inheritedAndOrdinaryInterfaceMixinsRetainContributionsWithoutUnrelatedMembers() {
        // given: an isolated model with base/interface mixins and unrelated counterparts
        var application = new SpringApplicationBuilder(Configuration_headless.class, Configuration_usingMixinApplicability.class)
                .web(WebApplicationType.NONE).logStartupInfo(false)
                .properties("spring.main.banner-mode=off", "causeway.core.meta-model.introspector.mode=FULL",
                        "causeway.core.meta-model.introspector.parallelize=false",
                        "causeway.core.meta-model.validator.explicit-object-type=FALSE",
                        "causeway.extensions.command-log.recording-support=ENABLED",
                        "logging.level.org.apache.causeway.core.metamodel=ERROR",
                        "logging.level.org.apache.causeway.core.metamodel.progmodel=ERROR");

        // when: bootstrapping the full model with command recording enabled
        try (var context = application.run()) {
            var loader = context.getBean(SpecificationLoader.class);
            var base = loader.specForTypeElseFail(Base.class);
            var subtype = loader.specForTypeElseFail(Subtype.class);
            var other = loader.specForTypeElseFail(Other.class);

            // then: the subtype retains the inherited property's and collection's identities/types
            var property = subtype.getAssociation("tag", MixedIn.INCLUDED).orElseThrow();
            assertEquals("tag", property.getId());
            assertEquals(String.class, property.getElementType().correspondingClass());

            var collection = subtype.getAssociation("children", MixedIn.INCLUDED).orElseThrow();
            assertEquals("children", collection.getId());
            assertEquals(Child.class, collection.getElementType().correspondingClass());

            // then: the ordinary interface contributes its action with the correct element type
            var action = subtype.getAction("alpha", MixedIn.INCLUDED).orElseThrow();
            assertEquals("alpha", action.getId());
            assertEquals(Child.class, action.getElementType().correspondingClass());

            // then: navigation synthesis and explicit action sequence are preserved
            assertTrue(subtype.getAction("__causeway_navigate_to_one_of_children", MixedIn.INCLUDED).isPresent());
            var ids = subtype.streamActions(ActionScope.ANY, MixedIn.INCLUDED).map(a -> a.getId()).toList();
            assertTrue(ids.containsAll(List.of("alpha", "zulu")));
            assertTrue(ids.indexOf("zulu") < ids.indexOf("alpha"), "Explicit sequence overrides class-name traversal");

            // then: positive controls show that each mixin was discovered on its own mixee
            assertTrue(base.getAssociation("tag", MixedIn.INCLUDED).isPresent());
            assertTrue(base.getAssociation("children", MixedIn.INCLUDED).isPresent());
            assertTrue(base.getAction("alpha", MixedIn.INCLUDED).isEmpty(), "Only the subtype implements Capability");
            assertTrue(other.getAction("foreignAction", MixedIn.INCLUDED).isPresent());
            assertTrue(other.getAssociation("foreignTag", MixedIn.INCLUDED).isPresent());
            assertTrue(other.getAssociation("foreignChildren", MixedIn.INCLUDED).isPresent());

            // then: unrelated mixins do not leak contributions across target types
            assertTrue(subtype.getAction("foreignAction", MixedIn.INCLUDED).isEmpty());
            assertTrue(subtype.getAssociation("foreignTag", MixedIn.INCLUDED).isEmpty());
            assertTrue(subtype.getAssociation("foreignChildren", MixedIn.INCLUDED).isEmpty());
            assertTrue(other.getAction("alpha", MixedIn.INCLUDED).isEmpty());
            assertTrue(other.getAssociation("tag", MixedIn.INCLUDED).isEmpty());
        }
    }
}
