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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.testdomain.util.interaction.InteractionTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class
        },
        properties = {
                "isis.core.meta-model.introspector.mode=FULL",
                "isis.applib.annotation.domain-object.editing=TRUE",
                "isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class PropertyInteractionTest extends InteractionTestAbstract {

    @Test
    void propertyInteraction_whenEnabled_shouldHaveNoVeto() {

        final var tester =
                testerFactory.propertyTester(InteractionDemo.class, "stringMultiline", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();

        // verify, that the meta-model is valid
        assertMetamodelValid();

        // verify, that we have the LabelAtFacet
        val labelAtFacet = tester.getFacetOnMemberElseFail(LabelAtFacet.class);
        val labelPos = labelAtFacet.label();
        assertEquals(LabelPosition.TOP, labelPos);

        // verify, that we have the MultiLineFacet
        val multiLineFacet = tester.getFacetOnMemberElseFail(MultiLineFacet.class);
        val numberOfLines = multiLineFacet.numberOfLines();
        assertEquals(3, numberOfLines);

        tester.assertValue("initial");
        tester.assertValueUpdateUsingNegotiation("new Value");
        tester.assertValueUpdateUsingNegotiationTextual("parsable Text");
    }

    @Test
    void propertyInteraction_whenDisabled_shouldHaveVeto() {

        val managedProperty = startPropertyInteractionOn(InteractionDemo.class, "stringDisabled", Where.OBJECT_FORMS)
                .getManagedProperty().get(); // should not throw


        assertFalse(managedProperty.checkVisibility().isPresent()); // is visible

        // verify we cannot edit
        val veto = managedProperty.checkUsability().get(); // should not throw
        assertNotNull(veto);

        assertEquals("Disabled for demonstration.", veto.getReason());
    }


}
