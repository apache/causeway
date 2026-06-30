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
package org.apache.causeway.testdomain.domainmodel;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.domainmodel.recordingnav.Configuration_usingRecordingNavigation;
import org.apache.causeway.testdomain.domainmodel.recordingnav.RecNavCycleA;
import org.apache.causeway.testdomain.domainmodel.recordingnav.RecNavCycleB;
import org.apache.causeway.testdomain.domainmodel.recordingnav.RecNavParent;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

/**
 * Boots the framework with command-log recording-support enabled over a domain that includes two view models
 * referencing each other via mixed-in parented collections (a cycle).
 *
 * <p>
 * Reaching the test body proves the metamodel was created without recursing without bound during boot - the
 * CAUSEWAY-4039 StackOverflow regression, which occurred while synthesizing the synthetic navigation
 * ("selector") actions for parented collections. The assertions further confirm the navigation actions are
 * synthesized (for own and mixed-in/cyclic collections) by SynthesizeNavigationActionsPostProcessor during
 * the post-processing phase.
 */
@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingRecordingNavigation.class,
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE",
                "causeway.extensions.command-log.recording-support=ENABLED",
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel,
})
class RecordingNavigation_IntegTest extends CausewayIntegrationTestAbstract {

    private static final String PREFIX =
            ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil.ACTION_ID_PREFIX;

    @Inject private SpecificationLoader specificationLoader;

    @Test
    void cyclic_mixedIn_collections_boot_and_synthesize_navigation_actions() {
        val specA = specificationLoader.specForTypeElseFail(RecNavCycleA.class);
        val specB = specificationLoader.specForTypeElseFail(RecNavCycleB.class);

        assertTrue(specA.getAction(PREFIX + "bs", MixedIn.INCLUDED).isPresent(),
                "expected navigation action for mixed-in collection 'bs' on RecNavCycleA");
        assertTrue(specB.getAction(PREFIX + "as", MixedIn.INCLUDED).isPresent(),
                "expected navigation action for mixed-in collection 'as' on RecNavCycleB");
    }

    @Test
    void own_collection_synthesizes_navigation_action() {
        val parentSpec = specificationLoader.specForTypeElseFail(RecNavParent.class);

        assertTrue(parentSpec.getAction(PREFIX + "children", MixedIn.INCLUDED).isPresent(),
                "expected navigation action for own collection 'children' on RecNavParent");
    }

}
