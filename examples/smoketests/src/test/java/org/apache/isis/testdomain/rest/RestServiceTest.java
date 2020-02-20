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
package org.apache.isis.testdomain.rest;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoBook;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;

import lombok.val;

@SpringBootTest(
        classes = {RestEndpointService.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Import({
    Configuration_usingJdo.class,
    IsisModuleViewerRestfulObjectsJaxrsResteasy4.class
})
class RestServiceTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestEndpointService restService;

    @Test
    void bookOfTheWeek_viaRestEndpoint() {

        assertNotNull(restService.getPort());
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getRecommendedBookOfTheWeek(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val bookOfTheWeek = digest.getEntities().getSingletonOrFail();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());

    }
    
    @Test
    void addNewBook_viaRestEndpoint() throws JAXBException {

        assertNotNull(restService.getPort());
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val newBook = JdoBook.of("REST Book", "A sample REST book for testing.", 77., 
                "REST Author", "REST ISBN", "REST Publisher");
        
        val digest = restService.storeBook(restfulClient, newBook);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val storedBook = digest.getEntities().getSingletonOrFail();

        assertNotNull(storedBook);
        assertEquals("REST Book", storedBook.getName());

    }
    
    @Test
    void multipleBooks_viaRestEndpoint() throws JAXBException {

        assertNotNull(restService.getPort());
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false; 
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getMultipleBooks(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val multipleBooks = digest.getEntities();
        
        assertEquals(2, multipleBooks.size());
        
        for(val book : multipleBooks) {
            assertEquals("MultipleBooksTest", book.getName());    
        }

    }
    
    @Test
    void bookOfTheWeek_asDto_viaRestEndpoint() {

        assertNotNull(restService.getPort());
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getRecommendedBookOfTheWeekAsDto(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val bookOfTheWeek = digest.getEntities().getSingletonOrFail();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());

    }
    
    @Test
    void multipleBooks_asDto_viaRestEndpoint() throws JAXBException {

        assertNotNull(restService.getPort());
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false; 
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getMultipleBooksAsDto(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val multipleBooks = digest.getEntities();
        
        assertEquals(2, multipleBooks.size());
        
        for(val book : multipleBooks) {
            assertEquals("MultipleBooksTest", book.getName());    
        }

    }
    
    
    @Test
    void httpSessionInfo() {

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getHttpSessionInfo(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val httpSessionInfo = digest.getEntities().getSingletonOrFail();

        assertNotNull(httpSessionInfo);
        assertEquals("no http-session", httpSessionInfo);

    }
    
    

}
