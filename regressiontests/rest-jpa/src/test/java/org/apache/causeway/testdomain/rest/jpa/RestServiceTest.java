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
package org.apache.causeway.testdomain.rest.jpa;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.client.RestClient;

import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEventSemantics;
import org.apache.causeway.testdomain.jpa.JpaInventoryJaxbVm;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.RegressionTestWithJpaFixtures;
import org.apache.causeway.testdomain.jpa.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.jpa.rest.JpaRestEndpointService;
import org.apache.causeway.viewer.restfulobjects.viewer.CausewayModuleViewerRestfulObjectsViewer;

@SpringBootTest(
        classes = {
                JpaRestEndpointService.class,
                CalendarEventSemantics.class // register semantics for testing
                },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"causeway.viewer.restfulobjects.base-path=/restful"}
)
@Import({
    Configuration_usingJpa.class,
    CausewayModuleViewerRestfulObjectsViewer.class
})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestServiceTest extends RegressionTestWithJpaFixtures {

    private static final boolean USE_REQUEST_DEBUG_LOGGING = true;

    @LocalServerPort int port; // just for reference (not used)
    @Inject JpaRestEndpointService restService;

    private RestClient restClient;

    @BeforeEach
    void checkPrereq() {
        assertTrue(restService.getPort()>0);
        this.restClient = restService.newClient(USE_REQUEST_DEBUG_LOGGING);
    }

    @Test @Order(1)
    void httpSessionInfo() {
        var httpSessionInfo = restService.getHttpSessionInfo(restClient)
                .valueAsNonNullElseFail();

        // NB: this works only because we excluded wicket viewer from the app.
        assertEquals("no http-session", httpSessionInfo);
    }

    @Test @Order(2)
    void bookOfTheWeek_viaRestEndpoint() {
        var bookOfTheWeek = restService.getRecommendedBookOfTheWeek(restClient)
                .valueAsNonNullElseFail();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

    @Test @Order(3)
    void addNewBook_viaRestEndpoint() throws JAXBException {
        var newBook = JpaBook.of("REST Book", "A sample REST book for testing.", 77.,
                "REST Author", "REST ISBN", "REST Publisher");

        var entity = restService.storeBook(restClient, newBook)
                .ifFailureFail();

        var storedBook = entity.valueAsNonNullElseFail();

        assertNotNull(storedBook);
        assertEquals("REST Book", storedBook.getName());
    }

    @Test @Order(4)
    void multipleBooks_viaRestEndpoint() throws JAXBException {
        var entity = restService.getMultipleBooks(restClient)
                .ifFailureFail();

        var expectedBookTitles = JpaTestFixtures.expectedBookTitles();

        var multipleBooks = entity.valueAsNonNullElseFail()
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());
    }

    @Test @Order(5)
    void bookOfTheWeek_asDto_viaRestEndpoint() {
        var entity = restService.getRecommendedBookOfTheWeekAsDto(restClient)
                .ifFailureFail();

        var bookOfTheWeek = entity.valueAsNonNullElseFail();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

    @Test @Order(6)
    void multipleBooks_asDto_viaRestEndpoint() throws JAXBException {
        var entity = restService.getMultipleBooksAsDto(restClient)
                .ifFailureFail();

        var multipleBooks = entity.valueAsNonNullElseFail();

        assertEquals(2, multipleBooks.size());

        for(var book : multipleBooks) {
            assertEquals("MultipleBooksAsDtoTest", book.getName());
        }
    }

    @Test @Order(7)
    void inventoryAsJaxbVm_viaRestEndpoint() {
        final JpaInventoryJaxbVm inventoryAsJaxbVm = restService.getInventoryAsJaxbVm(restClient)
                .valueAsNonNullElseFail();

        assertNotNull(inventoryAsJaxbVm);
        assertEquals("Bookstore", inventoryAsJaxbVm.getName());
    }

    @Test @Order(8)
    void listBooks_fromInventoryAsJaxbVm_viaRestEndpoint() {
        var books = restService.getBooksFromInventoryAsJaxbVm(restClient)
                .valueAsNonNullElseFail();

        var expectedBookTitles = JpaTestFixtures.expectedBookTitles();

        var multipleBooks = books
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());
    }

    @Test @Order(9)
    void calendarEvent_echo_viaRestEndpoint() {
        var calSemantics = new CalendarEventSemantics();
        var calSample = calSemantics.getExamples().getElseFail(0);
        /* calSemantics.decompose(calSample).toJson() ...
         * {
         * "elements":[
         *     {"long":1652452215000,"type":"long","name":"epochMillis"},
         *     {"string":"Business","type":"string","name":"calendarName"},
         *     {"string":"Weekly Meetup","type":"string","name":"title"},
         *     {"string":"Calendar Notes","type":"string","name":"notes"}
         *     ],
         * "type":"org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent",
         * "cardinality":4
         * }
         */
        var entity = restService.echoCalendarEvent(restClient, calSample)
                .ifFailure(Assertions::fail);

        var calSampleEchoed = entity.valueAsNonNullElseFail();
        assertEquals(calSample, calSampleEchoed);
    }

}
