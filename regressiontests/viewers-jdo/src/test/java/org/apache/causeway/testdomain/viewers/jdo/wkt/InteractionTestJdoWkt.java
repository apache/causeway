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
package org.apache.causeway.testdomain.viewers.jdo.wkt;

import jakarta.inject.Inject;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.datanucleus.PropertyNames;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.exceptions.unrecoverable.BookmarkNotFoundException;
import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.testdomain.conf.Configuration_usingWicket;
import org.apache.causeway.testdomain.conf.Configuration_usingWicket.DomainObjectPageTester;
import org.apache.causeway.testdomain.conf.Configuration_usingWicket.DomainObjectPageTester.SimulatedProperties;
import org.apache.causeway.testdomain.conf.Configuration_usingWicket.DomainObjectPageTester.SimulatedProperty;
import org.apache.causeway.testdomain.conf.Configuration_usingWicket.WicketTesterFactory;
import org.apache.causeway.testdomain.jdo.RegressionTestWithJdoFixtures;
import org.apache.causeway.testdomain.jdo.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.panels.PromptFormAbstract;

import static org.apache.causeway.testdomain.conf.Configuration_usingWicket.DomainObjectPageTester.BOOK_DELETE_ACTION_JDO;
import static org.apache.causeway.testdomain.conf.Configuration_usingWicket.DomainObjectPageTester.OPEN_SAMPLE_ACTION;
import static org.apache.causeway.testdomain.conf.Configuration_usingWicket.DomainObjectPageTester.OPEN_SAMPLE_ACTION_TITLE;
import static org.apache.causeway.testdomain.conf.Configuration_usingWicket.DomainObjectPageTester.STANDALONE_COLLECTION_LABEL;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingWicket.class,
                //XrayEnable.class
                },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:InteractionTestJdoWkt",
                PropertyNames.PROPERTY_RETAIN_VALUES + "=false"  // default anyway
                /* TODO[CAUSEWAY-3486] default, but should be enforced by causeway:
                 * datanucleus.detachAllOnCommit = false */
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class InteractionTestJdoWkt extends RegressionTestWithJdoFixtures {

    @Inject private WicketTesterFactory wicketTesterFactory;

    private DomainObjectPageTester wktTester;

    // optimization: reuse Wicket application across tests
    private static _Refs.ObjectReference<DomainObjectPageTester> wktTesterHolder =
            _Refs.objectRef(null);

    @BeforeEach
    void setUp() throws InterruptedException {
        wktTester = wktTesterHolder.computeIfAbsent(()->
                wicketTesterFactory.createTester(JdoBook::fromDto));
    }

    @AfterAll
    static void cleanUp() {
        wktTesterHolder.getValue()
            .ifPresent(DomainObjectPageTester::destroy);
    }

    @Test
    void load_viewmodel_with_referenced_entities_directly() {

        var pageParameters = call(()->{
            var inventoryJaxbVm = testFixtures.createViewmodelWithCurrentBooks();
            return wktTester.createPageParameters(inventoryJaxbVm);
        });

        //System.err.printf("pageParameters %s%n", pageParameters);

        run(()->{
            wktTester.startDomainObjectPage(pageParameters);

            //XXX activate for test troubleshooting
            // wktTester.dumpComponentTree(comp->true);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("JdoInventoryJaxbVm; Bookstore; 3 products");

            wktTester.assertFavoriteBookIs(BookDto.sample());

        });

    }

    @Test
    void load_viewmodel_with_referenced_entities_via_action() {
        var pageParameters = call(()->{
            var testHomePage = new TestAppJdoWkt.TestHomePage();
            return wktTester.createPageParameters(testHomePage);
        });

        //System.err.printf("pageParameters %s%n", pageParameters);

        // open homepage for TestHomePage
        run(()->{
            wktTester.startDomainObjectPage(pageParameters);

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

        SimulatedProperty inventoryName = SimulatedProperties.INVENTORY_NAME;

        // simulate click on editable property -> should bring up the corresponding inline edit dialog
        run(()->{
            wktTester.assertBehavior(inventoryName.editLink(), AjaxEventBehavior.class);
            wktTester.executeAjaxEvent(inventoryName.editLink(), "click");
            wktTester.assertComponent(inventoryName.editInlinePromptForm(), PromptFormAbstract.class);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("JdoInventoryJaxbVm; Bookstore; 3 products");
            wktTester.assertFavoriteBookIs(BookDto.sample());
            wktTester.assertInventoryNameIs("Bookstore");
        });

        // simulate change of a String property Name from 'Bookstore' -> 'Bookstore2'
        run(()->{
            var form = wktTester.newFormTester(inventoryName.editInlinePromptForm());
            form.setValue(inventoryName.scalarField(), "Bookstore2");
            form.submit();
        });

        // simulate click on form OK button -> expected to trigger the framework's property change execution
        run(()->{
            wktTester.assertComponent(inventoryName.editInlinePromptFormOk(), IndicatingAjaxButton.class);
            wktTester.executeAjaxEvent(inventoryName.editInlinePromptFormOk(), "click");
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

    private ManagedObject bookAdapter;

    @Test
    void loadBookPage_Dune_then_change_Isbn() {
        var pageParameters = call(()->{

            var jdoBook = repositoryService.allInstances(JdoBook.class).stream()
            .filter(book->"Dune".equals(book.getName()))
            .findFirst()
            .orElseThrow();

            System.err.printf("--- adapt %n");
            bookAdapter = super.objectManager.adapt(jdoBook);

            return wktTester.createPageParameters(jdoBook);
        });

        //System.err.printf("pageParameters %s%n", pageParameters);

        assertEquals(ManagedObject.Specialization.ENTITY, bookAdapter.getSpecialization());
        assertTrue(bookAdapter.isBookmarkMemoized(), "bookAdapter should be bookmarked");

        // open Dune page
        run(()->{
            wktTester.startDomainObjectPage(pageParameters);
            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("Dune [ISBN-A]");
        });

        SimulatedProperty bookIsbn = SimulatedProperties.JDO_BOOK_ISBN;

        // simulate click on editable property -> should bring up the corresponding inline edit dialog
        run(()->{
            wktTester.assertBehavior(bookIsbn.editLink(), AjaxEventBehavior.class);
            wktTester.executeAjaxEvent(bookIsbn.editLink(), "click");
            wktTester.assertComponent(bookIsbn.editInlinePromptForm(), PromptFormAbstract.class);

            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("Dune [ISBN-A]");
        });

        // simulate change of a String property Name from 'ISBN-A' -> 'ISBN-XXXX'
        run(()->{
            var form = wktTester.newFormTester(bookIsbn.editInlinePromptForm());
            form.setValue(bookIsbn.scalarField(), "ISBN-XXXX");
            form.submit();

            var jpaBook = (JdoBook)bookAdapter.getPojo();
            assertEquals("ISBN-A", jpaBook.getIsbn());
        });

        // simulate click on form OK button -> expected to trigger the framework's property change execution
        run(()->{
            wktTester.assertComponent(bookIsbn.editInlinePromptFormOk(), IndicatingAjaxButton.class);

            wktTester.dumpComponentTree(comp->true);
            System.out.println(
                wktTester.getLastResponseAsString()
            );

            wktTester.executeAjaxEvent(bookIsbn.editInlinePromptFormOk(), "click");

            System.err.printf("bookAdapter state %s%n", bookAdapter.getEntityState());

            System.err.printf("--- verify %n");
            var jpaBook = (JdoBook)bookAdapter.getPojo();
            assertEquals("ISBN-XXXX", jpaBook.getIsbn());
        });

        // ... should yield a new Title containing 'Dune [ISBN-XXXX]'
        run(()->{
            wktTester.assertHeaderBrandText("Smoke Tests");
            wktTester.assertPageTitle("Dune [ISBN-XXXX]");
        });

        run(()->{
            // reset
            var jdoBook = repositoryService.allInstances(JdoBook.class).stream()
                    .filter(book->"Dune".equals(book.getName()))
                    .findFirst()
                    .orElseThrow();
            jdoBook.setIsbn("ISBN-A");
        });

    }

    @Test
    void loadBookPage_Dune_then_delete() {
        var pageParameters = call(()->{

            var jdoBook = repositoryService.allInstances(JdoBook.class).stream()
            .filter(book->"Dune".equals(book.getName()))
            .findFirst()
            .orElseThrow();

            return wktTester.createPageParameters(jdoBook);
        });

        // open Dune page and click on the Delete action
        run(()->{
            wktTester.startDomainObjectPage(pageParameters);
            wktTester.clickLink(BOOK_DELETE_ACTION_JDO);

            // then should render a standalone collection labeled 'Delete'
            var label = (Label)wktTester
                    .getComponentFromLastRenderedPage(STANDALONE_COLLECTION_LABEL);
            assertEquals("Delete", label.getDefaultModelObject());
        });
    }

    @Test
    void loadNonExistentBookPage_shouldRender_noSuchObjectError() {
        var pageParameters = PageParameterUtils.createPageParametersForBookmark(
                Bookmark.forLogicalTypeAndIdentifier(
                        LogicalType.eager(JdoBook.class, "testdomain.jdo.Book"),
                        "99"));

        // open book page for non existent OID '99'
        // should throw an (causeway) ObjectNotFoundException
        assertThrows(ObjectNotFoundException.class, ()->{
            run(()->{
                wktTester.startDomainObjectPage(pageParameters);
            });
        });

        // yet don't know how to verify an error page was rendered
//        run(()->{
//            wktTester.dumpComponentTree(comp->true);
//        });

    }

    @Test
    void loadNonExistentBookBookmark_shouldRender_BookmarkNotFoundException() {
        var pageParameters = PageParameterUtils.createPageParametersForBookmark(
                Bookmark.forLogicalTypeAndIdentifier(
                        LogicalType.eager(JdoBook.class, "simple.SimpleObject"),
                        "999"));
        assertThrows(BookmarkNotFoundException.class, ()->{
            run(()->{
                wktTester.startDomainObjectPage(pageParameters);
            });
        });
    }
}
