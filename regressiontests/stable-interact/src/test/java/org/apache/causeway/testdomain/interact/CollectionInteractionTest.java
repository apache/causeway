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
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.model.interaction.InteractionDemo;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

import lombok.val;

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
class CollectionInteractionTest extends InteractionTestAbstract {

    @Test
    void multiSelect() {

        val tester =
                testerFactory.collectionTester(InteractionDemo.class, "items", Where.ANYWHERE);
        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();

        val expectedElements = tester.streamCollectionElements()
                .map(MmUnwrapUtil::single)
                .collect(Collectors.toList());
        assertEquals(4, expectedElements.size());

        tester.assertCollectionElements(expectedElements);

        val tableTester = tester.tableTester();

        tableTester.assertUnfilteredDataElements(expectedElements);

        // toggle on 'second' and 'last' item for selection
        tableTester.assertDataRowSelectionWhenToggledOn(List.of(1, 3), List.of(
                expectedElements.get(1),
                expectedElements.get(3)));

        // toggle off 'second' for selection, 'last' item should remain
        tableTester.assertDataRowSelectionWhenToggledOff(List.of(1), List.of(
                expectedElements.get(3)));

        // toggle all on
        tableTester.getDataTable().getSelectAllToggle().setValue(true);
        tableTester.assertDataRowSelectionIsAll();

        // toggle all off
        tableTester.getDataTable().getSelectAllToggle().setValue(false);
        tableTester.assertDataRowSelectionIsEmpty();

    }

    @Test
    void columns() {

        val tableTester =
                testerFactory.collectionTester(InteractionDemo.class, "items", Where.ANYWHERE)
                .tableTester();

        tableTester.assertColumnNames(List.of("Name", "Date"));

    }

    @Test
    void choicesFromMultiselect() {

        val collTester =
                testerFactory.collectionTester(InteractionDemo.class, "items", Where.ANYWHERE);
        collTester.assertVisibilityIsNotVetoed();
        collTester.assertUsabilityIsNotVetoed();

        val choiceElements = ((InteractionDemo)(collTester.getManagedCollectionIfAny().orElseThrow()
                .getOwner()
                .getPojo()))
                .getItems();
        assertEquals(4, choiceElements.size());

        val tableTester = collTester.tableTester();
        // toggle on 'second' and 'last' item for selection
        tableTester.assertDataRowSelectionWhenToggledOn(List.of(1, 3), List.of(
                choiceElements.get(1),
                choiceElements.get(3)));

        val table = tableTester.getDataTable();

        val actionInteraction = table
                .startAssociatedActionInteraction("doSomethingWithItems", Where.OBJECT_FORMS);

        val actTester = testerFactory.actionTesterForSpecificInteraction(InteractionDemo.class, actionInteraction);
        actTester.assertVisibilityIsNotVetoed();
        actTester.assertUsabilityIsNotVetoed();

        val expectedParamDefault = List.of(
                choiceElements.get(1),
                choiceElements.get(3));

        // verify param defaults are seeded with choices from selection
        actTester.assertParameterValues(true,
                arg0->assertEquals(expectedParamDefault, arg0, ()->"param 0 mismatch"),
                arg1->assertEquals(expectedParamDefault, arg1, ()->"param 1 mismatch"));
    }

}
