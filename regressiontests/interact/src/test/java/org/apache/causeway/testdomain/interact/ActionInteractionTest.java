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

import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.model.interaction.DemoEnum;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo_biArgEnabled;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo_biListOfString;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo_multiEnum;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo_multiInt;
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
class ActionInteractionTest extends InteractionTestAbstract {

    @Test
    void whenEnabled_shouldHaveNoVeto() {

        var tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgEnabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();
    }

    @Test
    void whenDisabled_shouldHaveVeto() {

        var tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsVetoedWith("Disabled for demonstration.");
    }

    @Test
    void whenEnabled_shouldProvideActionMetadata() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        var managedAction = actionInteraction.getManagedAction().get(); // should not throw
        var actionMeta = managedAction.getAction();
        assertEquals(2, actionMeta.getParameterCount());

    }

    @Test
    void mixinWhenDisabled_shouldProvideActionMetadata() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgDisabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertFalse(actionInteraction.getManagedAction().isPresent()); // since usability should be vetoed
        assertTrue(actionInteraction.getMetamodel().isPresent()); // but should always provide access to metamodel

        var actionMeta = actionInteraction.getMetamodel().get();
        assertEquals(2, actionMeta.getParameterCount());
    }

    @Test
    void whenEnabled_shouldAllowInvocation() {

        var tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgEnabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();
        tester.assertInvocationResult(99, UnaryOperator.identity());

        var capturedCommands = tester.getCapturedCommands();
        assertEquals(1, capturedCommands.size());
        var command = capturedCommands.getElseFail(1);
        assertEquals("regressiontests.InteractionDemo#noArgEnabled",
                command.getLogicalMemberIdentifier());

        // test feature-identifier to command matching ...
        var act = tester.getActionMetaModelElseFail();
        InteractionHead head = tester.getActionMetaModelElseFail().interactionHead(tester.getActionOwnerElseFail());
        assertTrue(IdentifierUtil.isCommandForMember(command, head, act));
    }

    @Test
    void whenDisabled_shouldVetoInvocation() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        assertFalse(actionInteraction.startParameterNegotiation().isPresent());

        // even though when assembling valid parameters ...
        var pendingArgs = startActionInteractionOn(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS)
                .startParameterNegotiation().get();

        // we should not be able to invoke the action
        var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isFailure());

        var tester =
                testerFactory.actionTester(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsVetoedWith("Disabled for demonstration.");
        assertThrows(IllegalAccessException.class, ()->tester.assertInvocationResult(99));
    }

    @Test
    void mixinWithParams_shouldProduceCorrectResult() throws Throwable {

        var tester =
                testerFactory.actionTester(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();
        tester.assertInvocationResult(46, arg0->12, arg1->34);

        var capturedCommands = tester.getCapturedCommands();
        assertEquals(1, capturedCommands.size());
        var command = capturedCommands.getElseFail(1);
        assertEquals("regressiontests.InteractionDemo#biArgEnabled",
                command.getLogicalMemberIdentifier());

        // test feature-identifier to command matching ...
        var act = tester.getActionMetaModelElseFail();
        InteractionHead head = tester.getActionMetaModelElseFail().interactionHead(tester.getActionOwnerElseFail());
        assertTrue(IdentifierUtil.isCommandForMember(command, head, act));
    }

    @Test
    void withTooManyParams_shouldIgnoreOverflow() throws Throwable {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        var params = Can.of(objectManager.adapt(12), objectManager.adapt(34), objectManager.adapt(99));

        var pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);

        var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isSuccess());

        assertEquals(46, (int)resultOrVeto.getSuccessElseFail().getPojo());
    }

    @Test
    void withTooLittleParams_shouldIgnoreUnderflow() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        var params = Can.of(objectManager.adapt(12));

        var pendingArgs = actionInteraction.startParameterNegotiation().get();
        pendingArgs.setParamValues(params);

        var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isSuccess());

        assertEquals(12, (int)resultOrVeto.getSuccessElseFail().getPojo());

    }

    @Test
    void shouldProvideParameterDefaults() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        var managedAction = actionInteraction.getManagedAction().get(); // should not throw
        var pendingArgs = managedAction.startParameterNegotiation();

        var expectedDefaults = Can.of(
                new InteractionDemo_biArgEnabled(null).defaultA(null),
                0);
        var actualDefaults = pendingArgs.getParamValues();

        assertComponentWiseUnwrappedEquals(expectedDefaults, actualDefaults);

    }

    @Test
    void whenHavingChoices_shouldProvideProperParameterDefaults() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "multiInt", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        var managedAction = actionInteraction.getManagedAction().get(); // should not throw
        var pendingArgs = managedAction.startParameterNegotiation();

        var mixin = new InteractionDemo_multiInt(null);
        var expectedDefaults = Can.<Integer>of(
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

        var expectedParamsAfter = Can.<Integer>of(
                mixin.choicesA(null)[3],
                mixin.defaultB(null),
                mixin.defaultC(null));

        assertComponentWiseUnwrappedEquals(expectedParamsAfter, pendingArgs.getParamValues());

    }

    @Test
    void whenHavingEnumChoices_shouldProvideProperParameterDefaults() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "multiEnum", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        var managedAction = actionInteraction.getManagedAction().get(); // should not throw
        var pendingArgs = managedAction.startParameterNegotiation();

        var mixin = new InteractionDemo_multiEnum(null);
        var expectedDefaults = Can.<DemoEnum>of(
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

        var expectedParamsAfter = Can.<DemoEnum>of(
                DemoEnum.values()[3],
                mixin.defaultB(null),
                mixin.defaultC(null));

        assertComponentWiseUnwrappedEquals(expectedParamsAfter, pendingArgs.getParamValues());
    }

    @Test
    void whenHavingListOfStringChoices_shouldProvideProperParameterDefaults() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biListOfString", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        var managedAction = actionInteraction.getManagedAction().get(); // should not throw
        var pendingArgs = managedAction.startParameterNegotiation();

        var mixin = new InteractionDemo_biListOfString(null);
        var expectedDefaults = Can.<List<String>>of(
                mixin.defaultA(null),
                mixin.defaultB(null));

        assertComponentWiseUnwrappedEquals(expectedDefaults, pendingArgs.getParamValues());

        // when changing the first parameter, consecutive parameters should not be affected
        // (unless they are depending on this choice ... subject to another test)

        int choiceParamNr = 0;

        SimulatedUiChoices uiParamA = new SimulatedUiChoices();
        uiParamA.bind(pendingArgs, choiceParamNr); // bind to param that has choices
        uiParamA.simulateMultiChoiceSelect(0, 2); // select first and 3rd choice

        var expectedParamsAfter = Can.<List<String>>of(
                _Lists.ofNullable(
                        mixin.defaultA(null).get(0),
                        mixin.defaultA(null).get(2)
                        ),
                _Lists.ofNullable(
                        mixin.defaultB(null).get(0)
                        ));

        assertComponentWiseUnwrappedEquals(expectedParamsAfter, pendingArgs.getParamValues());
    }

    @Test
    void shouldProvideChoices() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");

        var managedAction = actionInteraction.getManagedAction().get();
        var pendingArgs = managedAction.startParameterNegotiation();

        var param0Choices = pendingArgs.getObservableParamChoices(0); // observable
        var param1Choices = pendingArgs.getObservableParamChoices(1); // observable

        assertTrue(param0Choices.getValue().isEmpty());

        var expectedChoices = new InteractionDemo_biArgEnabled(null).choicesB(null);
        var actualChoices = param1Choices.getValue();

        assertComponentWiseUnwrappedEquals(expectedChoices, actualChoices);
    }

    @Test
    void actionAnnotation_withChoicesFrom_shouldProvideChoices() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "doSomethingWithItems", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");

        var managedAction = actionInteraction.getManagedAction().get();
        var pendingArgs = managedAction.startParameterNegotiation();

        var param0Choices = pendingArgs.getObservableParamChoices(0); // observable
        var param1Choices = pendingArgs.getObservableParamChoices(1); // observable

        assertFalse(param0Choices.getValue().isEmpty());
        assertFalse(param1Choices.getValue().isEmpty());

        assertComponentWiseUnwrappedEquals(
                new InteractionDemo().getItems(),
                param0Choices.getValue());

        assertComponentWiseUnwrappedEquals(
                new InteractionDemo().getItems(),
                param1Choices.getValue());
    }

    @Test
    void shouldProvideParameterBinding() {

        var actionInteraction = startActionInteractionOn(InteractionDemo.class, "biArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        assertTrue(actionInteraction.getManagedAction().isPresent(), "action is expected to be usable");

        var managedAction = actionInteraction.getManagedAction().get();
        var pendingArgs = managedAction.startParameterNegotiation();

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

        var expectedChoices = new InteractionDemo_biArgEnabled(null).choicesB(null);
        var expectedChoice = expectedChoices[2]; // actual 3rd choice

        Object actualChoiceAsSeenByBackend = pendingArgs.getParamValue(choiceParamNr).getPojo();
        Object actualChoiceAsSeenByUi = uiParam1.getValue().getPojo();

        assertEquals(expectedChoice, actualChoiceAsSeenByBackend);
        assertEquals(expectedChoice, actualChoiceAsSeenByUi);

        // ensure backend changes are reflected back to the UI

        var expectedChoiceAfterBackendUpdated = expectedChoices[0]; // actual first choice
        var newParamValue = pendingArgs
                .adaptParamValuePojo(choiceParamNr, expectedChoiceAfterBackendUpdated);

        var bindableParamValue = pendingArgs.getBindableParamValue(choiceParamNr);
        bindableParamValue.setValue(newParamValue);

        actualChoiceAsSeenByBackend = pendingArgs.getParamValue(choiceParamNr).getPojo();
        actualChoiceAsSeenByUi = uiParam1.getValue().getPojo();

        assertEquals(expectedChoiceAfterBackendUpdated, actualChoiceAsSeenByBackend);
        assertEquals(expectedChoiceAfterBackendUpdated, actualChoiceAsSeenByUi);

    }

    @Test
    void whenNonScalarResult_shouldHaveDataTable() {

        var tester =
                testerFactory.actionTester(InteractionDemo.class, "limitedItems", Where.OBJECT_FORMS);

        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();

        var choiceElements = ((InteractionDemo)(tester.getManagedActionElseFail()
                .getOwner()
                .getPojo()))
                .getItems();
        assertEquals(4, choiceElements.size());

        var tableTester = tester.tableTester(arg0->2); // 2 expected rows in the resulting table

        tableTester.assertUnfilteredDataElements(List.of(
                choiceElements.get(0),
                choiceElements.get(1)));
    }

  //TODO also deal with non-scalar parameter values
  //TODO test whether actions do emit their domain events
  //TODO test whether actions can be vetoed via domain event interception
  //TODO test whether interactions spawn their own transactions, commands, interactions(applib)

}
