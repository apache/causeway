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
package org.apache.isis.testdomain.viewers.jdo.wkt;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.RegressionTestAbstract;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.WicketTesterFactory;
import org.apache.isis.testdomain.jdo.JdoTestFixtures;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;

import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.INLINE_PROMPT_FORM_FIELD;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.INLINE_PROMPT_FORM_OK;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.INVENTORY_NAME_PROPERTY_EDIT_INLINE_PROMPT_FORM;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.INVENTORY_NAME_PROPERTY_EDIT_LINK;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.OPEN_SAMPLE_ACTION;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.OPEN_SAMPLE_ACTION_TITLE;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingWicket.class,
                //XrayEnable.class
                },
        properties = {
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class InteractionTestJdoWkt extends RegressionTestAbstract {

    @Inject private WicketTesterFactory wicketTesterFactory;
    @Inject private JdoTestFixtures testFixtures;

    private EntityPageTester wktTester;

    @BeforeEach
    void setUp() throws InterruptedException {

        wktTester = wicketTesterFactory.createTester(JdoBook::fromDto);

        run(()->{
            testFixtures.setUp3Books();
        });
    }

    @AfterEach
    void cleanUp() {
        wktTester.destroy();
        XrayUi.waitForShutdown();
    }

    @Test
    void load_viewmodel_with_referenced_entities_directly() {

        val pageParameters = call(()->{
            val inventoryJaxbVm = testFixtures.setUpViewmodelWith3Books();
            return wktTester.createPageParameters(inventoryJaxbVm);
        });

        //System.err.printf("pageParameters %s%n", pageParameters);

        run(()->{
            wktTester.startEntityPage(pageParameters);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("JdoInventoryJaxbVm; Bookstore; 3 products");

            //wktTester.dumpComponentTree(comp->true);

            wktTester.assertFavoriteBookIs(BookDto.sample());

        });

    }

    @Test
    void load_viewmodel_with_referenced_entities_via_action() {
        val pageParameters = call(()->{
            val testHomePage = new TestAppJdoWkt.TestHomePage();
            return wktTester.createPageParameters(testHomePage);
        });

        //System.err.printf("pageParameters %s%n", pageParameters);

        // open homepage for TestHomePage
        run(()->{
            wktTester.startEntityPage(pageParameters);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("Hello, __system");
            wktTester.assertLabel(OPEN_SAMPLE_ACTION_TITLE, "Open Sample Page");
        });

        // click action "Open Sample Page" and render resulting JdoInventoryJaxbVm
        run(()->{
            wktTester.clickLink(OPEN_SAMPLE_ACTION);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("JdoInventoryJaxbVm; Bookstore; 3 products");
            wktTester.assertFavoriteBookIs(BookDto.sample());
            wktTester.assertInventoryNameIs("Bookstore");
        });

        // simulate click on editable property -> should bring up the corresponding inline edit dialog
        run(()->{
            wktTester.assertBehavior(INVENTORY_NAME_PROPERTY_EDIT_LINK, AjaxEventBehavior.class);
            wktTester.executeAjaxEvent(INVENTORY_NAME_PROPERTY_EDIT_LINK, "click");
            wktTester.assertComponent(INVENTORY_NAME_PROPERTY_EDIT_INLINE_PROMPT_FORM, PromptFormAbstract.class);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("JdoInventoryJaxbVm; Bookstore; 3 products");
            wktTester.assertFavoriteBookIs(BookDto.sample());
            wktTester.assertInventoryNameIs("Bookstore");
        });

        // simulate change of a String property Name from 'Bookstore' -> 'Bookstore2'
        run(()->{
            val form = wktTester.newFormTester(INVENTORY_NAME_PROPERTY_EDIT_INLINE_PROMPT_FORM);
            form.setValue(INLINE_PROMPT_FORM_FIELD, "Bookstore2");
            form.submit();
        });

        // simulate click on form OK button -> expected to trigger the framework's property change execution
        run(()->{
            wktTester.assertComponent(INLINE_PROMPT_FORM_OK, IndicatingAjaxButton.class);
            wktTester.executeAjaxEvent(INLINE_PROMPT_FORM_OK, "click");
        });

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("[TEST] form submitted");
        });

        // ... should yield a new Title containing 'Bookstore2'
        run(()->{
            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("JdoInventoryJaxbVm; Bookstore2; 3 products");
            wktTester.assertFavoriteBookIs(BookDto.sample());
            wktTester.assertInventoryNameIs("Bookstore2");
        });

        //TODO simulate interaction with choice provider, where entries are entities -> should be attached, eg. test whether we can generate a title for these

    }

    // -- HELPER

}