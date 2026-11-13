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

import java.util.stream.IntStream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.testdomain.jpa.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.rest.JpaRestEndpointService;
import org.apache.causeway.testing.unittestsupport.applib.annotations.DisabledIfRunningWithSurefire;
import org.apache.causeway.viewer.restfulobjects.viewer.CausewayModuleViewerRestfulObjectsViewer;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(
        classes = {JpaRestEndpointService.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"causeway.viewer.restfulobjects.base-path=/restful"})
@Import({
    Configuration_usingJpa.class,
    CausewayModuleViewerRestfulObjectsViewer.class
})
@DisabledIfRunningWithSurefire
@Slf4j
class RestServiceStressTest {

    private static final boolean USE_REQUEST_DEBUG_LOGGING = false;

    @LocalServerPort int port; // just for reference (not used)
    @Inject JpaRestEndpointService restService;

//    @BeforeAll
//    static void init() {
//        _Swt.enableSwing();
//    }

    //TODO[ISIS-3275] performance regression compared to v2: 26s vs 6s
    @Test
    void bookOfTheWeek_stressTest() {

        assertTrue(restService.getPort()>0);

        //_Swt.prompt("get ready for stress testing");

        final int clients = 16;
        final int iterations = 100;
        var label = String.format("Calling REST endpoint %d times", clients * iterations);

        _Timing.runVerbose(log, label, ()->{

            IntStream.range(0, clients)
            //.parallel()
            .mapToObj(i->restService.newClient(USE_REQUEST_DEBUG_LOGGING))
            .forEach(restClient->{
                IntStream.range(0, iterations)
                .forEach(iter->{
                    requestSingleBookOfTheWeek_viaRestEndpoint(restClient);
                });
            });

        });

        //_Swt.prompt("stress testing done");

    }

    void requestSingleBookOfTheWeek_viaRestEndpoint(final RestClient restClient) {
        var bookOfTheWeek = restService.getRecommendedBookOfTheWeekAsDto(restClient)
            .valueAsNonNullElseFail();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

}
