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
 *
 */
package org.apache.causeway.regressiontests.layouts.integtest;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.mixins.rest.Object_openRestApi;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.causeway.regressiontests.layouts.integtest.model.Counter;
import org.apache.causeway.testdomain.util.interaction.DomainObjectTesterFactory.ActionTester;
import org.apache.causeway.viewer.wicket.applib.mixins.Object_clearHints;

import lombok.val;

@SpringBootTest(
        classes = Layout_Counter_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
public class Layout_Counter_IntegTest extends LayoutTestAbstract {

    @Inject private InteractionService interactionService;
    @Inject private BookmarkService bookmarkService;

    Bookmark target1;

    @BeforeEach
    void beforeEach() {
        interactionService.nextInteraction();

        Optional<Bookmark> bookmark = bookmarkService.bookmarkFor(newCounter("counter-1"));
        target1 = bookmark.orElseThrow();

        interactionService.nextInteraction();
    }

    protected Counter newCounter(final String name) {
        return Counter.builder().name(name).build();
    }

    /** @see Counter#actionNoPosition(String) */
    @Test
    void actionNoPosition() {
        val tester = actionTester("actionNoPosition");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(null);
        tester.assertLayoutGroup(null);
        tester.assertLayoutOrder("");
    }

    /** @see Counter#actionPositionBelow(String) */
    @Test
    void actionPositionBelow() {
        val tester = actionTester("actionPositionBelow");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(null);
        tester.assertLayoutGroup(null);
        tester.assertLayoutOrder("");
    }

    /** @see Counter#actionPositionPanel(String) */
    @Test
    void actionPositionPanel() {
        val tester = actionTester("actionPositionPanel");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(null);
        tester.assertLayoutGroup(null);
        tester.assertLayoutOrder("");
    }

    /** @see Counter#actionDetailsFieldSetNoPosition(String) */
    @Test
    void actionDetailsFieldSetNoPosition() {
        val tester = actionTester("actionDetailsFieldSetNoPosition");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN); // as of field-set fallback behavior
        tester.assertLayoutGroup("details");
        tester.assertLayoutOrder("1");
    }

    /** @see Counter#actionDetailsFieldSetPositionBelow(String) */
    @Test
    void actionDetailsFieldSetPositionBelow() {
        val tester = actionTester("actionDetailsFieldSetPositionBelow");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN); // as of field-set fallback behavior
        tester.assertLayoutGroup("details");
        tester.assertLayoutOrder("2");
    }

    /** @see Counter#actionDetailsFieldSetPositionPanel(String) */
    @Test
    void actionDetailsFieldSetPositionPanel() {
        val tester = actionTester("actionDetailsFieldSetPositionPanel");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL);
        tester.assertLayoutGroup("details");
        tester.assertLayoutOrder("3");
    }

    /** @see Counter#actionDetailsFieldSetPositionPanelDropdown(String) */
    @Test
    void actionDetailsFieldSetPositionPanelDropdown() {
        val tester = actionTester("actionDetailsFieldSetPositionPanelDropdown");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup("details");
        tester.assertLayoutOrder("4");
    }

    /** @see Counter#actionEmptyFieldSetNoPosition(String) */
    @Test
    void actionEmptyFieldSetNoPosition() {
        val tester = actionTester("actionEmptyFieldSetNoPosition");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN); // as of fallback behavior
        tester.assertLayoutGroup("empty");
        tester.assertLayoutOrder("1");
    }

    /** @see Counter#actionEmptyFieldSetPositionBelow(String) */
    @Test
    void actionEmptyFieldSetPositionBelow() {
        val tester = actionTester("actionEmptyFieldSetPositionBelow");
        tester.assertAssociatedPropertyId(null);
        // the 'empty' field-set cannot be used -> fallback to panel
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup("empty");
        tester.assertLayoutOrder("2");
    }

    /** @see Counter#actionEmptyFieldSetPositionPanel(String) */
    @Test
    void actionEmptyFieldSetPositionPanel() {
        val tester = actionTester("actionEmptyFieldSetPositionPanel");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL);
        tester.assertLayoutGroup("empty");
        tester.assertLayoutOrder("3");
    }

    /** @see Counter#actionEmptyFieldSetPositionPanelDropdown(String) */
    @Test
    void actionEmptyFieldSetPositionPanelDropdown() {
        val tester = actionTester("actionEmptyFieldSetPositionPanelDropdown");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup("empty");
        tester.assertLayoutOrder("4");
    }

    /** @see Counter#actionAssociatedWithNamePropertyNoPosition(String) */
    @Test
    void actionAssociatedWithNamePropertyNoPosition() {
        val tester = actionTester("actionAssociatedWithNamePropertyNoPosition");
        tester.assertAssociatedPropertyId("name");
        tester.assertLayoutPosition(ActionLayout.Position.BELOW); // as of fallback behavior
        tester.assertLayoutGroup("name"); // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        tester.assertLayoutOrder("");
    }

    /** @see Counter#actionAssociatedWithNamePropertyBelow(String) */
    @Test
    void actionAssociatedWithNamePropertyBelow() {
        val tester = actionTester("actionAssociatedWithNamePropertyBelow");
        tester.assertAssociatedPropertyId("name");
        tester.assertLayoutPosition(ActionLayout.Position.BELOW);
        tester.assertLayoutGroup("name"); // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        tester.assertLayoutOrder("");
    }

    /** @see Counter#actionAssociatedWithNamePropertyPanel(String) */
    @Test
    void actionAssociatedWithNamePropertyPanel() {
        val tester = actionTester("actionAssociatedWithNamePropertyPanel");
        tester.assertAssociatedPropertyId("name");
        tester.assertLayoutPosition(ActionLayout.Position.PANEL);
        tester.assertLayoutGroup("name"); // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        tester.assertLayoutOrder("");
    }

    /** @see Counter#actionAssociatedWithNamePropertyPanelDropdown(String) */
    @Test
    void actionAssociatedWithNamePropertyPanelDropdown() {
        val tester = actionTester("actionAssociatedWithNamePropertyPanelDropdown");
        tester.assertAssociatedPropertyId("name");
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup("name"); // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        tester.assertLayoutOrder("");
    }

    /** @see Counter#actionAssociatedWithNamePropertyAndDetailsFieldSetNoPosition(String) */
    @Test @Disabled //FIXME[CAUSEWAY-3655]
    void actionAssociatedWithNamePropertyAndDetailsFieldSetNoPosition() {
        val tester = actionTester("actionAssociatedWithNamePropertyAndDetailsFieldSetNoPosition");
        tester.assertAssociatedPropertyId("name");

        // actually not sure what to expect
        assertEquals(ActionLayout.Position.BELOW,
            tester.getManagedActionElseFail()
                .getAction()
                .lookupFacet(ActionPositionFacet.class)
                .map(ActionPositionFacet::position)
                .orElse(null),
                ()->"unmet precondition");

        tester.assertLayoutPosition(ActionLayout.Position.BELOW); // as of fallback behavior
        tester.assertLayoutGroup("details");
        tester.assertLayoutOrder("1");
    }

    /** @see Counter#actionAssociatedWithNameAndDetailsFieldSetPropertyBelow(String) */
    @Test @Disabled //FIXME[CAUSEWAY-3655]
    void actionAssociatedWithNameAndDetailsFieldSetPropertyBelow() {
        val tester = actionTester("actionAssociatedWithNameAndDetailsFieldSetPropertyBelow");
        tester.assertAssociatedPropertyId("name");

        // expected precondition
        assertEquals(ActionLayout.Position.BELOW,
            tester.getManagedActionElseFail()
                .getAction()
                .lookupFacet(ActionPositionFacet.class)
                .map(ActionPositionFacet::position)
                .orElse(null),
                ()->"unmet precondition");

        tester.assertLayoutPosition(ActionLayout.Position.BELOW);
        tester.assertLayoutGroup("details");
        tester.assertLayoutOrder("2");
    }

    /** @see Counter#actionAssociatedWithNamePropertyAndDetailsFieldSetPanel(String) */
    @Test
    void actionAssociatedWithNamePropertyAndDetailsFieldSetPanel() {
        val tester = actionTester("actionAssociatedWithNamePropertyAndDetailsFieldSetPanel");
        tester.assertAssociatedPropertyId("name");
        tester.assertLayoutPosition(ActionLayout.Position.PANEL);
        tester.assertLayoutGroup("details");
        tester.assertLayoutOrder("3");
    }

    /** @see Counter#actionAssociatedWithNamePropertyAndDetailsFieldSetPanelDropdown(String) */
    @Test
    void actionAssociatedWithNamePropertyAndDetailsFieldSetPanelDropdown() {
        val tester = actionTester("actionAssociatedWithNamePropertyAndDetailsFieldSetPanelDropdown");
        tester.assertAssociatedPropertyId("name");
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup("details"); // because "name" is in this fieldSet
        tester.assertLayoutOrder("4");
    }

    /** @see Counter#actionAssociatedWithNamePropertyButEmptyFieldSetNoPosition(String) */
    @Test
    void actionAssociatedWithNamePropertyButEmptyFieldSetNoPosition() {
        val tester = actionTester("actionAssociatedWithNamePropertyButEmptyFieldSetNoPosition");
        tester.assertAssociatedPropertyId("name");
        // we cannot use the 'empty' field-set, but the property 'name' is found
        tester.assertLayoutPosition(ActionLayout.Position.BELOW);
        tester.assertLayoutGroup("empty"); // ignored
        tester.assertLayoutOrder("1");
    }

    /** @see Counter#actionAssociatedWithNamePropertyAndSequenceNoPosition(String) */
    @Test
    void actionAssociatedWithNamePropertyAndSequenceNoPosition() {
        val tester = actionTester("actionAssociatedWithNamePropertyAndSequenceNoPosition");
        tester.assertAssociatedPropertyId("name");
        tester.assertLayoutPosition(ActionLayout.Position.BELOW); // as of fallback behavior
        tester.assertLayoutGroup("name"); // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        tester.assertLayoutOrder("1");
    }

    /** @see Object_openRestApi */
    @Test
    void openRestApi() {
        val tester = actionTester("openRestApi");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup(LayoutConstants.FieldSetId.METADATA);
        tester.assertLayoutOrder("750.1");
    }

    /** @see Object_clearHints */
    @Test
    void clearHints() {
        val tester = actionTester("clearHints");
        tester.assertAssociatedPropertyId(null);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL);
        tester.assertLayoutGroup(LayoutConstants.FieldSetId.METADATA);
        tester.assertLayoutOrder("400.1");
    }

    private ActionTester<Counter> actionTester(final String id) {
        return testerFactory.actionTester(Counter.class, id, Where.OBJECT_FORMS);
    }

}
