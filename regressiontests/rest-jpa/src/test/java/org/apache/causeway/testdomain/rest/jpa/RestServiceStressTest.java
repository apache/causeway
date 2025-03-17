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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.commons.internal.debug.swt._Swt;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.jpa.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.rest.JpaRestEndpointService;
import org.apache.causeway.testing.unittestsupport.applib.annotations.DisabledIfRunningWithSurefire;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;

import lombok.extern.log4j.Log4j2;

@SpringBootTest(
        classes = {JpaRestEndpointService.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@Import({
    Configuration_usingJpa.class,
    CausewayModuleViewerRestfulObjectsJaxrsResteasy.class
})
@DisabledIfRunningWithSurefire
@Log4j2
class RestServiceStressTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject JpaRestEndpointService restService;

    @BeforeAll
    static void init() {
        _Swt.enableSwing();
    }

    //TODO[ISIS-3275] performance regression compared to v2: 26s vs 6s
    @Test
    void bookOfTheWeek_stressTest() {

        assertTrue(restService.getPort()>0);

        _Swt.prompt("get ready for stress testing");

        var useRequestDebugLogging = false;
        final int clients = 16;
        final int iterations = 1000;
        var label = String.format("Calling REST endpoint %d times", clients * iterations);

        _Timing.runVerbose(log, label, ()->{

            IntStream.range(0, clients)
            .parallel()
            .mapToObj(i->{
                var restfulClient = restService.newClient(useRequestDebugLogging);
                return restfulClient;
            })
            .forEach(restfulClient->{

                IntStream.range(0, iterations)
                .forEach(iter->{
                    requestSingleBookOfTheWeek_viaRestEndpoint(restfulClient);
                });

            });

        });

        _Swt.prompt("stress testing done");

    }

    void requestSingleBookOfTheWeek_viaRestEndpoint(final RestfulClient restfulClient) {

        var digest = restService.getRecommendedBookOfTheWeekDto(restfulClient)
                .ifFailure(Assertions::fail);

        var bookOfTheWeek = digest.getValue().orElseThrow();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

}
