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
package org.apache.causeway.testdomain.rest;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEventSemantics;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.JpaInventoryJaxbVm;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.RegressionTestWithJpaFixtures;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.util.rest.RestEndpointService;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;

@SpringBootTest(
        classes = {
                RestEndpointService.class,
                CalendarEventSemantics.class // register semantics for testing
                },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@Import({
    Configuration_usingJpa.class,
    CausewayModuleViewerRestfulObjectsJaxrsResteasy.class
})
class RestServiceTest extends RegressionTestWithJpaFixtures {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestEndpointService restService;

    private RestfulClient restfulClient;

    @BeforeEach
    void checkPrereq() {
        assertTrue(restService.getPort()>0);
        var useRequestDebugLogging = true;
        this.restfulClient = restService.newClient(useRequestDebugLogging);
    }

    @Test
    void httpSessionInfo() {
        var digest = restService.getHttpSessionInfo(restfulClient)
                .ifFailureFail();

        var httpSessionInfo = digest.getValue().orElseThrow();

        assertNotNull(httpSessionInfo);

        // NB: this works only because we excluded wicket viewer from the app.
        assertEquals("no http-session", httpSessionInfo);
    }

    @Test
    void bookOfTheWeek_viaRestEndpoint() {
        var digest = restService.getRecommendedBookOfTheWeek(restfulClient)
                .ifFailureFail();

        var bookOfTheWeek = digest.getValue().orElseThrow();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

    @Test
    void addNewBook_viaRestEndpoint() throws JAXBException {
        var newBook = JpaBook.of("REST Book", "A sample REST book for testing.", 77.,
                "REST Author", "REST ISBN", "REST Publisher");

        var digest = restService.storeBook(restfulClient, newBook)
                .ifFailureFail();

        var storedBook = digest.getValue().orElseThrow();

        assertNotNull(storedBook);
        assertEquals("REST Book", storedBook.getName());
    }

    //TODO[causeway-regressiontests-CAUSEWAY-3866] requires test migration
    @Disabled
    @Test
    void multipleBooks_viaRestEndpoint() throws JAXBException {
        var digest = restService.getMultipleBooks(restfulClient)
                .ifFailureFail();

        var expectedBookTitles = JpaTestFixtures.expectedBookTitles();

        var multipleBooks = digest.getValue().orElseThrow()
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());
    }

    @Test
    void bookOfTheWeek_asDto_viaRestEndpoint() {
        var digest = restService.getRecommendedBookOfTheWeekAsDto(restfulClient)
                .ifFailureFail();

        var bookOfTheWeek = digest.getValue().orElseThrow();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

    @Test
    void multipleBooks_asDto_viaRestEndpoint() throws JAXBException {
        var digest = restService.getMultipleBooksAsDto(restfulClient)
                .ifFailureFail();

        var multipleBooks = digest.getValue().orElseThrow();

        assertEquals(2, multipleBooks.size());

        for(var book : multipleBooks) {
            assertEquals("MultipleBooksTest", book.getName());
        }
    }

    //TODO[causeway-regressiontests-CAUSEWAY-3866] requires test migration
    @Disabled
    @Test
    void inventoryAsJaxbVm_viaRestEndpoint() {
        var digest = restService.getInventoryAsJaxbVm(restfulClient)
                .ifFailureFail();

        final JpaInventoryJaxbVm inventoryAsJaxbVm = digest.getValue().orElseThrow();

        assertNotNull(inventoryAsJaxbVm);
        assertEquals("Bookstore", inventoryAsJaxbVm.getName());
    }

    //TODO[causeway-regressiontests-CAUSEWAY-3866] requires test migration
    @Disabled
    @Test
    void listBooks_fromInventoryAsJaxbVm_viaRestEndpoint() {
        var digest = restService.getBooksFromInventoryAsJaxbVm(restfulClient)
                .ifFailure(Assertions::fail);

        var books = digest.getValue().orElseThrow();

        var expectedBookTitles = JpaTestFixtures.expectedBookTitles();

        var multipleBooks = books
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());
    }

    //TODO[causeway-regressiontests-CAUSEWAY-3866] requires test migration
    @Disabled
    @Test
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
        var digest = restService.echoCalendarEvent(restfulClient, calSample)
                .ifFailure(Assertions::fail);

        var calSampleEchoed = digest.getValue().orElseThrow();
        assertEquals(calSample, calSampleEchoed);
    }

}
