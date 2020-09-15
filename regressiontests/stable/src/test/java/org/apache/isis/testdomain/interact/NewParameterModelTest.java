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

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo_biArgEnabled;
import org.apache.isis.testdomain.model.interaction.InteractionNpmDemo;
import org.apache.isis.testdomain.model.interaction.InteractionNpmDemo_biArgEnabled;

import lombok.val;

@Smoketest
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
class NewParameterModelTest extends InteractionTestAbstract {
    
    //InteractionNpmDemo_biArgDisabled#validateAct()

    @Test
    void metamodel_shouldBeValid() {
        assertMetamodelValid();
    }
    
    @Test
    void paramAnnotations_whenNpm_shouldBeRecognized() {

        val param0Metamodel = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .getMetamodel().get().getParameters().getElseFail(0);
        
        // as with first param's @Parameter(maxLength = 2)
        val maxLengthFacet = param0Metamodel.getFacet(MaxLengthFacet.class);
        
        // as with first param's @ParameterLayout(describedAs = "first")
        val describedAsFacet = param0Metamodel.getFacet(DescribedAsFacet.class);
        
        assertNotNull(maxLengthFacet);
        assertNotNull(describedAsFacet);

        assertEquals(2, maxLengthFacet.value());
        assertEquals("first", describedAsFacet.value());
    }
    
    @Test
    void actionInteraction_withParams_shouldProduceCorrectResult() throws Throwable {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();
        
        val params = Can.of(objectManager.adapt(12), objectManager.adapt(34));
        
        val pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);
        
        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isLeft());
        
        assertEquals(46, (int)resultOrVeto.leftIfAny().getPojo());
    }

    @Test
    void actionInteraction_withTooManyParams_shouldIgnoreOverflow() throws Throwable {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();
        
        val params =  Can.of(objectManager.adapt(12), objectManager.adapt(34), objectManager.adapt(99));
        
        val pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);
        
        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isLeft());
        
        assertEquals(46, (int)resultOrVeto.leftIfAny().getPojo());
    }
    
    @Test
    void actionInteraction_withTooLittleParams_shouldIgnoreUnderflow() throws Throwable {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();
        
        val params = Can.<ManagedObject>of();
        
        val pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);
        
        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isLeft());
        
        assertEquals(5, (int)resultOrVeto.leftIfAny().getPojo());
    }
    
    @Test
    void actionInteraction_shouldProvideParameterDefaults() {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val pendingArgs = managedAction.getInteractionHead().defaults();
     
        val expectedDefaults = Can.of(
                new InteractionDemo_biArgEnabled(null).defaultA(null),
                0);
        val actualDefaults = pendingArgs.getParamValues().stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList());
        
        assertComponentWiseEquals(expectedDefaults, actualDefaults);
    }
    
    @Test 
    void actionInteraction_shouldProvideChoices() {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");

        val managedAction = actionInteraction.getManagedAction().get(); 
        val pendingArgs = managedAction.startParameterNegotiation();

        val param0Choices = pendingArgs.getObservableParamChoices(0); // observable
        val param1Choices = pendingArgs.getObservableParamChoices(1); // observable
        
        assertTrue(param0Choices.getValue().isEmpty());
        
        val expectedChoices = new InteractionNpmDemo_biArgEnabled(null).choicesB(null);
        val actualChoices = param1Choices.getValue();
        
        assertComponentWiseUnwrappedEquals(expectedChoices, actualChoices);
    }
    

}
