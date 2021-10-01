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
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
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
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class CollectionInteractionTest extends InteractionTestAbstract {

    @Test
    void multiSelect() {

        val tester =
                testerFactory.collectionTester(InteractionDemo.class, "items", Where.ANYWHERE);
        tester.assertVisibilityIsNotVetoed();
        tester.assertUsabilityIsNotVetoed();

        val expectedElements = tester.streamCollectionElements()
                .map(ManagedObjects.UnwrapUtil::single)
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

        tableTester.assertColumnNames(List.of("Name", "Calendar Entry"));

    }

    @Test @Disabled("FIXME[ISIS-2871]")
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

        //FIXME[ISIS-2871] bind action param defaults to table's selection model

        val actTester =
                testerFactory.actionTester(InteractionDemo.class, "doSomethingWithItems", Where.OBJECT_FORMS);
        actTester.assertVisibilityIsNotVetoed();
        actTester.assertUsabilityIsNotVetoed();

        // verify param defaults are seeded with choices from selection
        actTester.assertParameterValues(arg0->assertEquals(
                List.of(
                    choiceElements.get(1),
                    choiceElements.get(3)),
                arg0));

    }

}
