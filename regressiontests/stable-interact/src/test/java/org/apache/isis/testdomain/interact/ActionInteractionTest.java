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

import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.DemoEnum;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.testdomain.model.interaction.InteractionDemo_biArgEnabled;
import org.apache.isis.testdomain.model.interaction.InteractionDemo_biListOfString;
import org.apache.isis.testdomain.model.interaction.InteractionDemo_multiEnum;
import org.apache.isis.testdomain.model.interaction.InteractionDemo_multiInt;
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
class ActionInteractionTest extends InteractionTestAbstract {

    @Test
    void whenEnabled_shouldHaveNoVeto() {

        val tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgEnabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();
    }

    @Test
    void whenDisabled_shouldHaveVeto() {

        val tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsVetoedWith("Disabled for demonstration.");
    }



    @Test
    void whenEnabled_shouldProvideActionMetadata() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val actionMeta = managedAction.getAction();
        assertEquals(2, actionMeta.getParameterCount());

    }

    @Test
    void mixinWhenDisabled_shouldProvideActionMetadata() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgDisabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertFalse(actionInteraction.getManagedAction().isPresent()); // since usability should be vetoed
        assertTrue(actionInteraction.getMetamodel().isPresent()); // but should always provide access to metamodel

        val actionMeta = actionInteraction.getMetamodel().get();
        assertEquals(2, actionMeta.getParameterCount());
    }

    @Test
    void whenEnabled_shouldAllowInvocation() {

        val tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgEnabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();
        tester.assertInvocationResult(99, UnaryOperator.identity());


        val capturedCommands = tester.getCapturedCommands();
        assertEquals(1, capturedCommands.size());
        val command = capturedCommands.getElseFail(1);
        assertEquals("regressiontests.InteractionDemo#noArgEnabled",
                command.getLogicalMemberIdentifier());

        // test feature-identifier to command matching ...
        val act = tester.getActionMetaModelElseFail();
        assertTrue(IdentifierUtil.isCommandForMember(command, act));
    }

    @Test
    void whenDisabled_shouldVetoInvocation() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        assertFalse(actionInteraction.startParameterNegotiation().isPresent());

        // even though when assembling valid parameters ...
        val pendingArgs = startActionInteractionOn(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS)
                .startParameterNegotiation().get();

        // we should not be able to invoke the action
        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isRight());

        val tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsVetoedWith("Disabled for demonstration.");
        assertThrows(IllegalAccessException.class, ()->tester.assertInvocationResult(99));
    }

    @Test
    void mixinWithParams_shouldProduceCorrectResult() throws Throwable {

        val tester =
                testerFactory.actionTester(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();
        tester.assertInvocationResult(46, arg0->12, arg1->34);

        val capturedCommands = tester.getCapturedCommands();
        assertEquals(1, capturedCommands.size());
        val command = capturedCommands.getElseFail(1);
        assertEquals("regressiontests.InteractionDemo#biArgEnabled",
                command.getLogicalMemberIdentifier());

        // test feature-identifier to command matching ...
        val act = tester.getActionMetaModelElseFail();
        assertTrue(IdentifierUtil.isCommandForMember(command, act));
    }

    @Test
    void withTooManyParams_shouldIgnoreOverflow() throws Throwable {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        val params = Can.of(objectManager.adapt(12), objectManager.adapt(34), objectManager.adapt(99));

        val pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);

        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isLeft());

        assertEquals(46, (int)resultOrVeto.leftIfAny().getPojo());
    }

    @Test
    void withTooLittleParams_shouldIgnoreUnderflow() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        val params = Can.of(objectManager.adapt(12));

        val pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);

        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isLeft());

        assertEquals(12, (int)resultOrVeto.leftIfAny().getPojo());

    }

    @Test
    void shouldProvideParameterDefaults() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val pendingArgs = managedAction.startParameterNegotiation();

        val expectedDefaults = Can.of(
                new InteractionDemo_biArgEnabled(null).defaultA(null),
                0);
        val actualDefaults = pendingArgs.getParamValues();

        assertComponentWiseUnwrappedEquals(expectedDefaults, actualDefaults);

    }

    @Test
    void whenHavingChoices_shouldProvideProperParameterDefaults() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "multiInt", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val pendingArgs = managedAction.startParameterNegotiation();

        val mixin = new InteractionDemo_multiInt(null);
        val expectedDefaults = Can.<Integer>of(
                mixin.defaultA(null),
                mixin.defaultB(null),
                mixin.defaultC(null));

        assertComponentWiseUnwrappedEquals(expectedDefaults, pendingArgs.getParamValues());

        // when changing the first parameter, consecutive parameters should not be affected
        // (unless they are depending on this choice ... subject to another test)

        int choiceParamNr = 0;

        SimulatedUiChoices uiParam = new SimulatedUiChoices();
        uiParam.bind(pendingArgs, choiceParamNr); // bind to param that has choices
        uiParam.simulateChoiceSelect(3); // select 4th choice

        val expectedParamsAfter = Can.<Integer>of(
                mixin.choicesA(null)[3],
                mixin.defaultB(null),
                mixin.defaultC(null));

        assertComponentWiseUnwrappedEquals(expectedParamsAfter, pendingArgs.getParamValues());

    }

    @Test
    void whenHavingEnumChoices_shouldProvideProperParameterDefaults() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "multiEnum", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val pendingArgs = managedAction.startParameterNegotiation();

        val mixin = new InteractionDemo_multiEnum(null);
        val expectedDefaults = Can.<DemoEnum>of(
                mixin.defaultA(null),
                mixin.defaultB(null),
                mixin.defaultC(null));

        assertComponentWiseUnwrappedEquals(expectedDefaults, pendingArgs.getParamValues());

        // when changing the first parameter, consecutive parameters should not be affected
        // (unless they are depending on this choice ... subject to another test)

        int choiceParamNr = 0;

        SimulatedUiChoices uiParam = new SimulatedUiChoices();
        uiParam.bind(pendingArgs, choiceParamNr); // bind to param that has choices
        uiParam.simulateChoiceSelect(3); // select 4th choice

        val expectedParamsAfter = Can.<DemoEnum>of(
                DemoEnum.values()[3],
                mixin.defaultB(null),
                mixin.defaultC(null));

        assertComponentWiseUnwrappedEquals(expectedParamsAfter, pendingArgs.getParamValues());
    }

    @Test
    void whenHavingListOfStringChoices_shouldProvideProperParameterDefaults() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biListOfString", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val managedAction = actionInteraction.getManagedAction().get(); // should not throw
        val pendingArgs = managedAction.startParameterNegotiation();

        val mixin = new InteractionDemo_biListOfString(null);
        val expectedDefaults = Can.<List<String>>of(
                mixin.defaultA(null),
                mixin.defaultB(null));

        assertComponentWiseUnwrappedEquals(expectedDefaults, pendingArgs.getParamValues());

        // when changing the first parameter, consecutive parameters should not be affected
        // (unless they are depending on this choice ... subject to another test)

        int choiceParamNr = 0;

        SimulatedUiChoices uiParamA = new SimulatedUiChoices();
        uiParamA.bind(pendingArgs, choiceParamNr); // bind to param that has choices
        uiParamA.simulateMultiChoiceSelect(0, 2); // select first and 3rd choice

        val expectedParamsAfter = Can.<List<String>>of(
                _Lists.of(
                        mixin.defaultA(null).get(0),
                        mixin.defaultA(null).get(2)
                        ),
                _Lists.of(
                        mixin.defaultB(null).get(0)
                        ));

        assertComponentWiseUnwrappedEquals(expectedParamsAfter, pendingArgs.getParamValues());
    }


    @Test
    void shouldProvideChoices() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");

        val managedAction = actionInteraction.getManagedAction().get();
        val pendingArgs = managedAction.startParameterNegotiation();

        val param0Choices = pendingArgs.getObservableParamChoices(0); // observable
        val param1Choices = pendingArgs.getObservableParamChoices(1); // observable

        assertTrue(param0Choices.getValue().isEmpty());

        val expectedChoices = new InteractionDemo_biArgEnabled(null).choicesB(null);
        val actualChoices = param1Choices.getValue();

        assertComponentWiseUnwrappedEquals(expectedChoices, actualChoices);
    }

    @Test
    void shouldProvideParameterBinding() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");

        val managedAction = actionInteraction.getManagedAction().get();
        val pendingArgs = managedAction.startParameterNegotiation();

        final int firstParamNr = 0;

        SimulatedUiComponent uiParam0 = new SimulatedUiComponent();
        uiParam0.bind(pendingArgs.getParamModels().getElseFail(firstParamNr)); // bind to first param

        // UI component's value should be initialized to initial defaults
        assertEquals(new InteractionDemo_biArgEnabled(null).defaultA(null), uiParam0.getValue().getPojo());

        uiParam0.simulateValueChange(6);
        // simulated change should have been propagated to the backend/model
        assertEquals(6, pendingArgs.getParamValue(firstParamNr).getPojo());

        final int choiceParamNr = 1;

        SimulatedUiChoices uiParam1 = new SimulatedUiChoices();
        uiParam1.bind(pendingArgs, choiceParamNr); // bind to param that has choices
        uiParam1.simulateChoiceSelect(2); // select 3rd choice

        val expectedChoices = new InteractionDemo_biArgEnabled(null).choicesB(null);
        val expectedChoice = expectedChoices[2]; // actual 3rd choice

        Object actualChoiceAsSeenByBackend = pendingArgs.getParamValue(choiceParamNr).getPojo();
        Object actualChoiceAsSeenByUi = uiParam1.getValue().getPojo();

        assertEquals(expectedChoice, actualChoiceAsSeenByBackend);
        assertEquals(expectedChoice, actualChoiceAsSeenByUi);

        // ensure backend changes are reflected back to the UI

        val expectedChoiceAfterBackendUpdated = expectedChoices[0]; // actual first choice
        val newParamValue = pendingArgs
                .adaptParamValuePojo(choiceParamNr, expectedChoiceAfterBackendUpdated);

        val bindableParamValue = pendingArgs.getBindableParamValue(choiceParamNr);
        bindableParamValue.setValue(newParamValue);

        actualChoiceAsSeenByBackend = pendingArgs.getParamValue(choiceParamNr).getPojo();
        actualChoiceAsSeenByUi = uiParam1.getValue().getPojo();

        assertEquals(expectedChoiceAfterBackendUpdated, actualChoiceAsSeenByBackend);
        assertEquals(expectedChoiceAfterBackendUpdated, actualChoiceAsSeenByUi);

    }

    @Test
    void whenNonScalarResult_shouldHaveDataTable() {

        val tester =
                testerFactory.actionTester(InteractionDemo.class, "limitedItems", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();

        val choiceElements = ((InteractionDemo)(tester.getManagedActionElseFail()
                .getOwner()
                .getPojo()))
                .getItems();
        assertEquals(4, choiceElements.size());

        val tableTester = tester.tableTester(arg0->2); // 2 expected rows in the resulting table

        tableTester.assertUnfilteredDataElements(List.of(
                choiceElements.get(0),
                choiceElements.get(1)));
    }

  //TODO also deal with non-scalar parameter values
  //TODO test whether actions do emit their domain events
  //TODO test whether actions can be vetoed via domain event interception
  //TODO test whether interactions spawn their own transactions, commands, interactions(applib)

}
