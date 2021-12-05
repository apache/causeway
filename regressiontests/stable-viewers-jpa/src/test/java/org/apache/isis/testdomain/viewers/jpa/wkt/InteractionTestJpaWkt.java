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
package org.apache.isis.testdomain.viewers.jpa.wkt;

import javax.inject.Inject;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.RegressionTestAbstract;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.WicketTesterFactory;
import org.apache.isis.testdomain.jpa.JpaTestFixtures;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testdomain.viewers.jdo.wkt.TestAppJpaWkt;

import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.FAVORITE_BOOK_ENTITY_LINK;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.FAVORITE_BOOK_ENTITY_LINK_TITLE;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.FAVORITE_BOOK_SCALAR_NAME;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.OPEN_SAMPLE_ACTION;
import static org.apache.isis.testdomain.conf.Configuration_usingWicket.EntityPageTester.OPEN_SAMPLE_ACTION_TITLE;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                Configuration_usingWicket.class
        },
        properties = {
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class InteractionTestJpaWkt extends RegressionTestAbstract {

    @Inject private WicketTesterFactory wicketTesterFactory;
    @Inject private JpaTestFixtures testFixtures;

    private EntityPageTester wktTester;

    @BeforeEach
    void setUp() throws InterruptedException {

        wktTester = wicketTesterFactory.createTester();

        run(()->{
            testFixtures.setUp3Books();
        });
    }

    @AfterEach
    void cleanUp() {
        wktTester.destroy();
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
            wktTester.assertPageTitle("JpaInventoryJaxbVm; 3 products");

            //wktTester.dumpComponentTree(comp->true);

            assertFavoriteBookIsPopulated();

        });

    }

    @Test
    void load_viewmodel_with_referenced_entities_via_action() {
        val pageParameters = call(()->{
            val testHomePage = new TestAppJpaWkt.TestHomePage();
            return wktTester.createPageParameters(testHomePage);
        });

        //System.err.printf("pageParameters %s%n", pageParameters);

        run(()->{
            wktTester.startEntityPage(pageParameters);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("Hello, __system");

            wktTester.assertLabel(OPEN_SAMPLE_ACTION_TITLE, "Open Sample Page");

            // click action "Open Sample Page" and render resulting Viewmodel
            if(false){ //XXX broken
                wktTester.clickLink(OPEN_SAMPLE_ACTION);
                wktTester.assertHeaderBrandText("Smoke Tests");
                wktTester.assertPageTitle("JpaInventoryJaxbVm; 3 products");
            }

            assertFavoriteBookIsPopulated();

        });

    }

    //TODO simulate change of a String property -> should yield a new Title and serialized URL link
    //TODO simulate interaction with choice provider, where entries are entities -> should be attached, eg. test whether we can generate a title for these

    // -- HELPER

    private void assertFavoriteBookIsPopulated() {
        wktTester.assertLabel(FAVORITE_BOOK_SCALAR_NAME, "Favorite Book");
        wktTester.assertComponent(FAVORITE_BOOK_ENTITY_LINK, BookmarkablePageLink.class);

        val expectedLinkTitle = JpaBook.fromDto(BookDto.sample()).title();
        wktTester.assertLabel(FAVORITE_BOOK_ENTITY_LINK_TITLE, expectedLinkTitle);
    }

}