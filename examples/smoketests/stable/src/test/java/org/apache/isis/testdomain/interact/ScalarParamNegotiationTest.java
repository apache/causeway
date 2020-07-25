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

import static org.junit.jupiter.api.Assertions.*;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.testdomain.model.interaction.InteractionDemo_negotiate.Params.NumberRange;

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
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class ScalarParamNegotiationTest extends InteractionTestAbstract {

    private static enum NegotiationParams {
        RANGE_A,
        A,
        RANGE_B,
        B,
        RANGE_C,
        C;
    }
    
    ParameterNegotiationModel pendingArgs;
    
    SimulatedUiChoices uiParamRangeA;
    SimulatedUiChoices uiParamRangeB;
    SimulatedUiChoices uiParamRangeC;
    
    SimulatedUiChoices uiParamA;
    SimulatedUiChoices uiParamB;
    SimulatedUiAutoComplete uiParamC;
    
    SimulatedUiSubmit uiSubmit;
    
    @BeforeEach
    void setUpSimulatedUi() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "negotiate")
                .checkVisibility(Where.OBJECT_FORMS)
                .checkUsability(Where.OBJECT_FORMS);

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");
        
        val managedAction = actionInteraction.getManagedAction().get();
        pendingArgs = managedAction.startParameterNegotiation();
        
        // setting up and binding all the simulated UI components
        
        uiParamRangeA = new SimulatedUiChoices();
        uiParamRangeB = new SimulatedUiChoices();
        uiParamRangeC = new SimulatedUiChoices();
        
        uiParamA = new SimulatedUiChoices();
        uiParamB = new SimulatedUiChoices();
        uiParamC = new SimulatedUiAutoComplete();
        
        uiSubmit = new SimulatedUiSubmit();
        
        uiParamRangeA.bind(pendingArgs, NegotiationParams.RANGE_A.ordinal());
        uiParamRangeB.bind(pendingArgs, NegotiationParams.RANGE_B.ordinal());
        uiParamRangeC.bind(pendingArgs, NegotiationParams.RANGE_C.ordinal());
        
        uiParamA.bind(pendingArgs, NegotiationParams.A.ordinal());
        uiParamB.bind(pendingArgs, NegotiationParams.B.ordinal());
        uiParamC.bind(pendingArgs, NegotiationParams.C.ordinal());
        
        uiSubmit.bind(actionInteraction, pendingArgs);
        
        // verify that initial defaults are as expected
        
        assertEquals(NumberRange.POSITITVE, uiParamRangeA.getValue().getPojo());
        assertEquals(NumberRange.NEGATIVE, uiParamRangeB.getValue().getPojo());
        assertEquals(NumberRange.ODD, uiParamRangeC.getValue().getPojo());
        
        assertEquals(1, uiParamA.getValue().getPojo());
        assertEquals(-1, uiParamB.getValue().getPojo());
        assertEquals(-3, uiParamC.getValue().getPojo());
        
        // verify that initial choices are as expected
        
        assertComponentWiseUnwrappedEquals(NumberRange.POSITITVE.numbers(), uiParamA.getChoices());
        assertComponentWiseUnwrappedEquals(NumberRange.NEGATIVE.numbers(), uiParamB.getChoices());
        assertEmpty(uiParamC.getChoices()); // empty because the search argument is also empty
        
        // verify that initial validation messages are all empty, 
        // because we don't validate anything until a user initiated submit attempt occurs 
        
        assertEmpty(uiParamRangeA.getValidationMessage());
        assertEmpty(uiParamRangeB.getValidationMessage());
        assertEmpty(uiParamRangeC.getValidationMessage());
        
        assertEmpty(uiParamA.getValidationMessage());
        assertEmpty(uiParamB.getValidationMessage());
        assertEmpty(uiParamC.getValidationMessage());
        
        assertEmpty(uiSubmit.getValidationMessage());
        
        // verify that validation feedback is not active
        
        assertFalse(pendingArgs.getObservableValidationFeedbackActive().getValue());
        
    }
    
    @Test
    void paramC_whenSettingSearchArgument_shouldProvideChoices() {
        
        final int eventCount0 = uiParamC.getChoiceBoxUpdateEventCount().intValue();
        uiParamC.setSimulatedSearchArgument("-"); // select for all negative and odd numbers
        final int eventCount1 = uiParamC.getChoiceBoxUpdateEventCount().intValue();
     
        // verify that changing the search arg has triggered change listeners
        assertEquals(eventCount0 + 1, eventCount1);
        
        assertComponentWiseUnwrappedEquals(new int[] {-3, -1}, uiParamC.getChoices());
        final int eventCount2 = uiParamC.getChoiceBoxUpdateEventCount().intValue();
        
        // verify that no additional changes were triggered
        assertEquals(eventCount1, eventCount2);
        
        // TODO such a change might set or clear paramC validation message once validation feedback is active
    }

    @Test
    void paramRangeA_whenChanging_shouldUpdateParamAChoices() {

        final int eventCount0 = uiParamRangeA.getSelectedItemUpdateEventCount().intValue();
        uiParamRangeA.simulateChoiceSelect(NumberRange.NEGATIVE.ordinal());
        final int eventCount1 = uiParamRangeA.getSelectedItemUpdateEventCount().intValue();
        
        // verify that changing paramRangeA has triggered change listeners
        assertEquals(eventCount0 + 1, eventCount1);
        
        assertEquals(NumberRange.NEGATIVE, uiParamRangeA.getValue().getPojo());
        assertComponentWiseUnwrappedEquals(NumberRange.NEGATIVE.numbers(), uiParamA.getChoices());

        // TODO such a change might set or clear paramA validation message once validation feedback is active
    }
    
    @Test
    void whenSimulatedSubmit_shouldActivateValidationFeedback() {
        // simulated submit attempt, should activate validation feedback
        uiSubmit.simulateSubmit();
        assertTrue(pendingArgs.getObservableValidationFeedbackActive().getValue());
        //TODO unless all validations give green light, submission must be vetoed
        //TODO exceptions that occur during action invocation could either be rendered 
        //     as message, error page or action validation message 
    }
  

}
