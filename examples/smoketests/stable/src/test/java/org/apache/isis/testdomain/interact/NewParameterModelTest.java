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

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
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

    
    @Test
    void actionInteraction_withParams_shouldProduceCorrectResult() throws Throwable {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);
        
        val params = 
                //FIXME must instead support Can.of(objectManager.adapt(12), objectManager.adapt(34));
                Can.of(objectManager.adapt(new InteractionNpmDemo_biArgEnabled.Parameters(12, 34)));
        
        actionInteraction.useParameters(__->params, 
                (managedParameter, veto)-> fail(veto.toString()));
        
        val result = actionInteraction.getResultElseThrow(veto->fail(veto.toString()));
        assertEquals(46, (int)result.getActionReturnedObject().getPojo());
    }

    @Test
    void actionInteraction_withTooManyParams_shouldIgnoreOverflow() throws Throwable {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);
        
        val params = 
                //FIXME[ISIS-2362] must instead support Can.of(objectManager.adapt(12), objectManager.adapt(34), objectManager.adapt(99));
                Can.of(objectManager.adapt(new InteractionNpmDemo_biArgEnabled.Parameters(12, 34)), objectManager.adapt(99));
        
        actionInteraction.useParameters(__->params, 
                (managedParameter, veto)-> fail(veto.toString()));
        
        val result = actionInteraction.getResultElseThrow(veto->fail(veto.toString()));
        assertEquals(46, (int)result.getActionReturnedObject().getPojo());
    }
    
    @Test
    void actionInteraction_withTooLittleParams_shouldFail() {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);
        
        val params = Can.<ManagedObject>of();
        
        assertThrows(NoSuchElementException.class, ()->{
            
            actionInteraction.useParameters(__->params, 
                    (managedParameter, veto)-> fail(veto.toString()));
        });

    }
    
    @Test @Disabled("[ISIS-2362]")
    void actionInteraction_shouldProvideParameterDefaults() {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val pendingArgs = managedAction.getInteractionHead().defaults();
     
        val expectedDefaults = Can.of(
                new InteractionDemo_biArgEnabled(null).default0Act(),
                0);
        val actualDefaults = pendingArgs.getParamValues().stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList());
        
        assertComponentWiseEquals(expectedDefaults, actualDefaults);
    }
    
    @Test @Disabled("[ISIS-2362]")
    void actionInteraction_shouldProvideChoices() {

        val actionInteraction = startActionInteractionOn(InteractionNpmDemo.class, "biArgEnabled")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);

        //TODO simplify the API ...
        
        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val actionMeta = managedAction.getAction();
        
        val pendingArgs = managedAction.getInteractionHead().defaults();
        
        val paramMetaList = actionMeta.getParameters();
        
        val param0Meta = paramMetaList.getElseFail(0);
        val param1Meta = paramMetaList.getElseFail(1);
        
        //TODO we need to allow ui-component binding
        
        
        
        val choices0 = param0Meta.getChoices(pendingArgs, InteractionInitiatedBy.USER); 
        val choices1 = param1Meta.getChoices(pendingArgs, InteractionInitiatedBy.USER);
        
        assertTrue(choices0.isEmpty());
        
        val expectedChoices = new InteractionDemo_biArgEnabled(null).choices1Act();
        val actualChoices = choices1.map(ManagedObject::getPojo);
        
        assertComponentWiseEquals(expectedChoices, actualChoices);
        
    }


}
