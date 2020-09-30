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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.commons.InteractionTestAbstract;
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

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "negotiate", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

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
        
        // verify that changing the search argument fires change event
        assertDoesIncrement(
                uiParamC::getChoiceBoxUpdateEventCount,
                ()->uiParamC.setSimulatedSearchArgument("-")); // select for all negative and odd numbers
                        
        
        // verify that no additional changes are triggered
        assertDoesNotIncrement(        
                uiParamC::getChoiceBoxUpdateEventCount,
                ()->assertComponentWiseUnwrappedEquals(new int[] {-3, -1}, uiParamC.getChoices()));
        
        // TODO such a change might set or clear paramC validation message once validation feedback is active
    }

    @Test
    void paramRangeA_whenChanging_shouldUpdateParamAChoices() {

        // verify that changing paramRangeA fires change event
        assertDoesIncrement(
                uiParamRangeA::getSelectedItemUpdateEventCount,
                ()->uiParamRangeA.simulateChoiceSelect(NumberRange.NEGATIVE.ordinal()));
        
        assertEquals(NumberRange.NEGATIVE, uiParamRangeA.getValue().getPojo());
        assertComponentWiseUnwrappedEquals(NumberRange.NEGATIVE.numbers(), uiParamA.getChoices());

        // TODO such a change might set or clear paramA validation message once validation feedback is active
    }
    
    @Test
    void whenSimulatedSubmit_shouldActivateValidationFeedback_andPassAfterChangingParam() {
        
        // failed simulated submit attempt should trigger validation change listeners on the 'action'
        assertDoesIncrement(
                uiSubmit::getValidationUpdateEventCount,
                ()->uiSubmit.simulateSubmit());
        
        // simulated submit attempt, should have activate validation feedback
        assertTrue(pendingArgs.getObservableValidationFeedbackActive().getValue());

        // unless all validations give green light, submission must be vetoed
        assertEquals(null, uiSubmit.getResult().leftIfAny());
        assertEquals("invalid, sum must be zero, got -3", ""+uiSubmit.getResult().rightIfAny());
        
        // verify that changing paramA triggers validation change listeners on the 'action'
        assertDoesIncrement(
                uiSubmit::getValidationUpdateEventCount,
                ()->{
                    
                    // verify that changing paramA does not triggers validation change listeners on paramA,
                    // since paramA is already valid before the change
                    assertDoesNotIncrement(
                            uiParamA::getValidationUpdateEventCount,
                            ()->uiParamA.simulateChoiceSelect(3)); // change parameters, so we pass validation                    
                    
                });

        // verify that submission is granted now
        uiSubmit.simulateSubmit();

        // verify that we have the expected result returned from the action invocation 
        assertTrue(uiSubmit.getResult().isLeft());
        assertEquals(0, uiSubmit.getResult().leftIfAny().getPojo());
        
        //TODO exceptions that occur during action invocation could either be rendered 
        //     as message, error page or action validation message 
    }

    @Test
    void paramRangeA_whenChanging_shouldRenderParamAInvalid() {

        pendingArgs.activateValidationFeedback(); // turn on validation feedback for testing
        assertEquals(null, uiParamA.getValidationMessage()); // expected pre condition
        
        // verify that changing paramRangeA triggers validation change listeners on paramA
        assertDoesIncrement(
                uiParamA::getValidationUpdateEventCount,
                ()->uiParamRangeA.simulateChoiceSelect(NumberRange.NEGATIVE.ordinal()));
    
        // not only verify that paramA is invalid, but also that all pending args were considered
        assertEquals(
                "invalid, element not contained in NEGATIVE got 1, param set [1, -1, -3]", 
                uiParamA.getValidationMessage());
        
    }
    
    
    

}
