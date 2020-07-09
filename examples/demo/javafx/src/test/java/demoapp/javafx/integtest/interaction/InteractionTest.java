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
package demoapp.javafx.integtest.interaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;

import lombok.val;

import demoapp.dom.tooltip.TooltipDemo;
import demoapp.javafx.integtest.DemoFxTestAbstract;

class InteractionTest extends DemoFxTestAbstract {

    @Test 
    void actionInteraction_whenEnabled_shouldHaveNoVeto() {
        
        val managedAction = startActionInteractionOn(TooltipDemo.class, "noArgAction")
                .getManagedAction().get(); // should not throw  
        
        assertTrue(managedAction.checkVisibility(Where.OBJECT_FORMS).isEmpty()); // is visible
        assertTrue(managedAction.checkUsability(Where.OBJECT_FORMS).isEmpty()); // can invoke 
    }
    
    @Test 
    void actionInteraction_whenDisabled_shouldHaveVeto() {
        
        val managedAction = startActionInteractionOn(TooltipDemo.class, "disabledAction")
                .getManagedAction().get(); // should not throw  
        
        
        assertTrue(managedAction.checkVisibility(Where.OBJECT_FORMS).isEmpty()); // is visible
        
        // cannot invoke
        val veto = managedAction.checkUsability(Where.OBJECT_FORMS).get(); // should not throw
        assertNotNull(veto);
        
        assertEquals("Disabled for demonstration.", veto.getReason());
    }
    
    @Test 
    void actionInteraction_whenEnabled_shouldProvideProperDecoratorModels() {
        
        val actionInteraction = startActionInteractionOn(TooltipDemo.class, "noArgAction")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);
        
        val disablingUiModel = DisablingUiModel.of(actionInteraction);
        assertTrue(disablingUiModel.isEmpty());
    }
    
    @Test 
    void actionInteraction_whenDisabled_shouldProvideProperDecoratorModels() {
        
        val actionInteraction = startActionInteractionOn(TooltipDemo.class, "disabledAction")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);
        
        val disablingUiModel = DisablingUiModel.of(actionInteraction).get();
        assertEquals("Disabled for demonstration.", disablingUiModel.getReason());
    }
    
    @Test 
    void test0() {
        
        val actionInteraction = startActionInteractionOn(TooltipDemo.class, "withArguments")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);
        
        actionInteraction.getManagedAction().get(); // should not throw
        
    }
    
    
}
