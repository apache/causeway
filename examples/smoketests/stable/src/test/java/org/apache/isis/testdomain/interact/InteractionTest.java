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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;

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
class InteractionTest extends InteractionTestAbstract {

    @Test 
    void actionInteraction_whenEnabled_shouldHaveNoVeto() {

        val managedAction = startActionInteractionOn(InteractionDemo.class, "noArgEnabled")
                .getManagedAction().get(); // should not throw  

        assertFalse(managedAction.checkVisibility(Where.OBJECT_FORMS).isPresent()); // is visible
        assertFalse(managedAction.checkUsability(Where.OBJECT_FORMS).isPresent()); // can invoke 
    }
    
    @Test 
    void actionInteraction_whenDisabled_shouldHaveVeto() {

        val managedAction = startActionInteractionOn(InteractionDemo.class, "noArgDisabled")
                .getManagedAction().get(); // should not throw  


        assertFalse(managedAction.checkVisibility(Where.OBJECT_FORMS).isPresent()); // is visible

        // cannot invoke
        val veto = managedAction.checkUsability(Where.OBJECT_FORMS).get(); // should not throw
        assertNotNull(veto);

        assertEquals("Disabled for demonstration.", veto.getReason());
    }

    @Test 
    void actionInteraction_whenEnabled_shouldProvideProperDecoratorModels() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgEnabled")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);

        val disablingUiModel = DisablingUiModel.of(actionInteraction);
        assertFalse(disablingUiModel.isPresent());
    }

    @Test 
    void actionInteraction_whenDisabled_shouldProvideProperDecoratorModels() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgDisabled")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);

        val disablingUiModel = DisablingUiModel.of(actionInteraction).get();
        assertEquals("Disabled for demonstration.", disablingUiModel.getReason());
    }

    @Test 
    void actionInteraction_whenEnabled_shouldProvideActionMetadata() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val actionMeta = managedAction.getAction();
        assertEquals(2, actionMeta.getParameterCount());

    }

    @Test //TODO API not yet provides a convenient means to get the action-meta when not usable    
    void mixinActionInteraction_whenDisabled_shouldProvideActionMetadata() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgDisabled")
                .checkVisibility(Where.OBJECT_FORMS);
        
        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        
        actionInteraction.checkUsability(Where.OBJECT_FORMS);
        
        val actionMeta = managedAction.getAction();
        assertEquals(2, actionMeta.getParameterCount());
    }
    
    @Test
    void actionInteraction_whenEnabled_shouldAllowInvocation() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgEnabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);
        
        val result = actionInteraction.getResultElseThrow(veto->fail(veto.toString()));
        assertEquals(99, (int)result.getActionReturnedObject().getPojo());    
    }

    @Test
    void actionInteraction_whenDisabled_shouldVetoInvocation() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgDisabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);

        assertThrows(IllegalAccessException.class, ()->{
            actionInteraction.getResultElseThrow(veto->_Exceptions.illegalAccess("%s", veto.toString()));    
        });
    }
    
    @Test
    void actionInteraction_withParams_shouldProduceCorrectResult() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);
        
        val params = Can.of(objectManager.adapt(12), objectManager.adapt(34));
        
        actionInteraction.useParameters(__->params, 
                (managedParameter, veto)-> fail(veto.toString()));
        
        val result = actionInteraction.getResultElseThrow(veto->fail(veto.toString()));
        assertEquals(46, (int)result.getActionReturnedObject().getPojo());
    }

    @Test
    void actionInteraction_withTooManyParams_shouldIgnoreOverflow() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);
        
        val params = Can.of(objectManager.adapt(12), objectManager.adapt(34), objectManager.adapt(99));
        
        actionInteraction.useParameters(__->params, 
                (managedParameter, veto)-> fail(veto.toString()));
        
        val result = actionInteraction.getResultElseThrow(veto->fail(veto.toString()));
        assertEquals(46, (int)result.getActionReturnedObject().getPojo());
    }
    
    @Test
    void actionInteraction_withTooLittleParams_shouldFail() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled")
        .checkVisibility(Where.OBJECT_FORMS)
        .checkUsability(Where.OBJECT_FORMS);
        
        val params = Can.of(objectManager.adapt(12));
        
        assertThrows(NoSuchElementException.class, ()->{
            
            actionInteraction.useParameters(__->params, 
                    (managedParameter, veto)-> fail(veto.toString()));
        });

    }
    

//    @Test //TODO simplify the API
//    void actionInteraction_shouldProvideChoices() {
//
//        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled")
//                .checkVisibility(Where.OBJECT_FORMS)
//                .checkUsability(Where.OBJECT_FORMS);
//
//        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
//        val actionMeta = managedAction.getAction();
//        assertEquals(2, actionMeta.getParameterCount());
//
//    }
    
  //TODO test whether actions do emit their domain events
  //TODO test whether actions can be vetoed via domain event interception


}
