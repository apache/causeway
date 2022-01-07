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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotations.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
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
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class PropertyBindingTest extends InteractionTestAbstract {

    PropertyNegotiationModel proposalA;
    PropertyNegotiationModel proposalB;
    
    SimulatedUiComponent uiPropStringMultiline;
    
    // UI components for the new value negotiation dialog 
    SimulatedUiComponent uiPropA;
    SimulatedUiChoices uiPropAChoices;
    SimulatedUiAutoComplete uiPropBAutoComplete;
    
    @BeforeEach
    void setUpSimulatedUi() {

        val propertyInteractionA = startPropertyInteractionOn(InteractionDemo.class, "stringMultiline", Where.OBJECT_FORMS);  
        assertTrue(propertyInteractionA.getManagedProperty().isPresent(), "prop is expected to be editable");
        
        val propertyInteractionB = startPropertyInteractionOn(InteractionDemo.class, "string2", Where.OBJECT_FORMS);  
        assertTrue(propertyInteractionB.getManagedProperty().isPresent(), "prop is expected to be editable");
        
        val managedPropA = propertyInteractionA.getManagedProperty().get();
        this.proposalA = managedPropA.startNegotiation();
        
        val managedPropB = propertyInteractionB.getManagedProperty().get();
        this.proposalB = managedPropB.startNegotiation();
        
        // setting up and binding all the simulated UI components
        
        uiPropStringMultiline = new SimulatedUiComponent();
        uiPropA = new SimulatedUiComponent();
        uiPropAChoices = new SimulatedUiChoices();
        uiPropBAutoComplete = new SimulatedUiAutoComplete();
        
        uiPropStringMultiline.bind(managedPropA);
        
        uiPropA.bind(proposalA);
        uiPropAChoices.bind(proposalA);
        uiPropBAutoComplete.bind(proposalB);
        
        // verify that initial defaults are as expected
        
        assertEquals("initial", uiPropStringMultiline.getValue().getPojo());
        assertEquals("initial", uiPropA.getValue().getPojo());
        
        // verify that initial choices are as expected
        
        assertTrue(managedPropA.getMetaModel().hasChoices());
        assertComponentWiseUnwrappedEquals(new String[] {"Hello", "World"}, uiPropAChoices.getChoices());
        
        assertTrue(managedPropB.getMetaModel().hasAutoComplete());
        assertComponentWiseUnwrappedEquals(new String[] {}, uiPropBAutoComplete.getChoices());
        
        // verify that initial validation messages are all empty, 
        // because we don't validate anything until a user initiated submit attempt occurs 
        
        assertEmpty(uiPropA.getValidationMessage());
        
        // verify that validation feedback is not active
        
        assertFalse(proposalA.getObservableValidationFeedbackActive().getValue());
        assertFalse(proposalB.getObservableValidationFeedbackActive().getValue());
        
    }
    
    @Test
    void propA_whenChanging_shouldPropagteNewValue() {

        uiPropA.simulateValueChange("Hi");
        assertEquals("Hi", proposalA.getValue().getValue().getPojo());

        assertNull(proposalA.getValidationMessage().getValue());
        proposalA.submit();
        assertEquals("Hi", uiPropStringMultiline.getValue().getPojo());
    }
    
    @Test
    void propB_whenSettingSearchArgument_shouldProvideChoices() {
        
        // verify that changing the search argument fires change event
        assertDoesIncrement(
                uiPropBAutoComplete::getChoiceBoxUpdateEventCount,
                ()->uiPropBAutoComplete.setSimulatedSearchArgument("H")); // select "Hello"
                        
        
        // verify that no additional changes are triggered
        assertDoesNotIncrement(        
                uiPropBAutoComplete::getChoiceBoxUpdateEventCount,
                ()->assertComponentWiseUnwrappedEquals(new String[] {"Hello"}, uiPropBAutoComplete.getChoices()));
        
        // TODO such a change might set or clear propA validation message once validation feedback is active
    }

    
}
