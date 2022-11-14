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

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.jdo.JdoInventoryJaxbVm;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.util.rest.RestEndpointService;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;

import lombok.val;

@SpringBootTest(
        classes = {RestEndpointService.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@Import({
    Configuration_usingJdo.class,
    CausewayModuleViewerRestfulObjectsJaxrsResteasy.class
})
class RestServiceTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestEndpointService restService;

    @Test
    void httpSessionInfo() {

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getHttpSessionInfo(restfulClient)
                .ifFailure(Assertions::fail);

        val httpSessionInfo = digest.getValue().orElseThrow();

        assertNotNull(httpSessionInfo);

        // NB: this works only because we excluded wicket viewer from the app.
        assertEquals("no http-session", httpSessionInfo);

    }

    @Test
    void bookOfTheWeek_viaRestEndpoint() {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getRecommendedBookOfTheWeek(restfulClient)
                .ifFailure(Assertions::fail);

        val bookOfTheWeek = digest.getValue().orElseThrow();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());

    }

    @Test
    void addNewBook_viaRestEndpoint() throws JAXBException {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val newBook = JdoBook.of("REST Book", "A sample REST book for testing.", 77.,
                "REST Author", "REST ISBN", "REST Publisher");

        val digest = restService.storeBook(restfulClient, newBook)
                .ifFailure(Assertions::fail);

        val storedBook = digest.getValue().orElseThrow();

        assertNotNull(storedBook);
        assertEquals("REST Book", storedBook.getName());

    }

    @Test
    void multipleBooks_viaRestEndpoint() throws JAXBException {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getMultipleBooks(restfulClient)
                .ifFailure(Assertions::fail);

        val expectedBookTitles = JdoTestFixtures.expectedBookTitles();

        val multipleBooks = digest.getValue().orElseThrow()
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());
    }

    @Test
    void bookOfTheWeek_asDto_viaRestEndpoint() {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getRecommendedBookOfTheWeekAsDto(restfulClient)
                .ifFailure(Assertions::fail);

        val bookOfTheWeek = digest.getValue().orElseThrow();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());

    }

    @Test
    void multipleBooks_asDto_viaRestEndpoint() throws JAXBException {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getMultipleBooksAsDto(restfulClient)
                .ifFailure(Assertions::fail);

        val multipleBooks = digest.getValue().orElseThrow();

        assertEquals(2, multipleBooks.size());

        for(val book : multipleBooks) {
            assertEquals("MultipleBooksAsDtoTest", book.getName());
        }

    }

    @Test
    void inventoryAsJaxbVm_viaRestEndpoint() {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getInventoryAsJaxbVm(restfulClient)
                .ifFailure(Assertions::fail);

        final JdoInventoryJaxbVm inventoryAsJaxbVm = digest.getValue().orElseThrow();

        assertNotNull(inventoryAsJaxbVm);
        assertEquals("Bookstore", inventoryAsJaxbVm.getName());

    }

    @Test
    void listBooks_fromInventoryAsJaxbVm_viaRestEndpoint() {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getBooksFromInventoryAsJaxbVm(restfulClient)
                .ifFailure(Assertions::fail);

        val books = digest.getValue().orElseThrow();

        val expectedBookTitles = JdoTestFixtures.expectedBookTitles();

        val multipleBooks = books
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());

    }


}
