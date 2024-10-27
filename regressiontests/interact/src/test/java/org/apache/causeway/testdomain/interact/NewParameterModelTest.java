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

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo_biArgEnabled;
import org.apache.causeway.testdomain.model.interaction.InteractionNpmDemo;
import org.apache.causeway.testdomain.model.interaction.InteractionNpmDemo_biArgEnabled;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //CausewayPresets.DebugMetaModel,
    //CausewayPresets.DebugProgrammingModel,
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class NewParameterModelTest extends InteractionTestAbstract {

    //InteractionNpmDemo_biArgDisabled#validateAct()

    @Test
    void metamodel_shouldBeValid() {
        assertMetamodelValid();
    }

    @ParameterizedTest
    @ValueSource(strings = {"biArgEnabled", "patEnabled"})
    void paramAnnotations_whenNpm_shouldBeRecognized(final String mixinName) {

        var param0Metamodel = startActionInteractionOn(InteractionNpmDemo.class, mixinName, Where.OBJECT_FORMS)
                .getMetamodel().get().getParameters().getElseFail(0);

        // as with first param's @Parameter(maxLength = 2)
        var maxLengthFacet = param0Metamodel.getFacet(MaxLengthFacet.class);

        // as with first param's @ParameterLayout(describedAs = "first")
        var describedAsFacet = param0Metamodel.getFacet(ParamDescribedFacet.class);

        assertNotNull(maxLengthFacet);
        assertNotNull(describedAsFacet);

        assertEquals(2, maxLengthFacet.value());
        assertEquals("first", describedAsFacet.text());
    }

    @Test
    void actionInteraction_withParams_shouldProduceCorrectResult() throws Throwable {

        var actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        var params = Can.of(objectManager.adapt(12), objectManager.adapt(34));

        var pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);

        var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isSuccess());

        assertEquals(46, (int)resultOrVeto.getSuccessElseFail().getPojo());
    }

    @Test
    void actionInteraction_withTooManyParams_shouldIgnoreOverflow() throws Throwable {

        var actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        var params =  Can.of(objectManager.adapt(12), objectManager.adapt(34), objectManager.adapt(99));

        var pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);

        var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isSuccess());

        assertEquals(46, (int)resultOrVeto.getSuccessElseFail().getPojo());
    }

    @Test
    void actionInteraction_withTooLittleParams_shouldIgnoreUnderflow() throws Throwable {

        var actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        var params = Can.<ManagedObject>of();

        var pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);

        var resultOrVeto = actionInteraction.invokeWith(pendingArgs);

        assertTrue(resultOrVeto.isSuccess());

        assertEquals(5, (int)resultOrVeto.getSuccessElseFail().getPojo());
    }

    @Test
    void actionInteraction_shouldProvideParameterDefaults() {

        var actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        var managedAction = actionInteraction.getManagedAction().get(); // should not throw
        var pendingArgs = managedAction.startParameterNegotiation();

        var expectedDefaults = Can.of(
                new InteractionDemo_biArgEnabled(null).defaultA(null),
                0);
        var actualDefaults = pendingArgs.getParamValues().stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList());

        assertComponentWiseEquals(expectedDefaults, actualDefaults);
    }

    @ParameterizedTest
    @ValueSource(strings = {"biArgEnabled", "patEnabled"})
    void actionInteraction_shouldProvideChoices(final String mixinName) {

        var actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, mixinName, Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");

        var managedAction = actionInteraction.getManagedAction().get();
        var pendingArgs = managedAction.startParameterNegotiation();

        var param0Choices = pendingArgs.getObservableParamChoices(0); // observable
        var param1Choices = pendingArgs.getObservableParamChoices(1); // observable

        assertTrue(param0Choices.getValue().isEmpty());

        var expectedChoices = new InteractionNpmDemo_biArgEnabled(null).choicesB(null);
        var actualChoices = param1Choices.getValue();

        assertComponentWiseUnwrappedEquals(expectedChoices, actualChoices);

    }

}
