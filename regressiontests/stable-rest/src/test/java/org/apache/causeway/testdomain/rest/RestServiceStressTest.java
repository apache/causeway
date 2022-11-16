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

import java.util.stream.IntStream;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.util.rest.RestEndpointService;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@SpringBootTest(
        classes = {RestEndpointService.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@Import({
    Configuration_usingJdo.class,
    CausewayModuleViewerRestfulObjectsJaxrsResteasy.class
})
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
@Log4j2
class RestServiceStressTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestEndpointService restService;

    @Test
    void bookOfTheWeek_stressTest() {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        final int clients = 16;
        final int iterations = 1000;
        val label = String.format("Calling REST endpoint %d times", clients * iterations);

        _Timing.runVerbose(log, label, ()->{

            IntStream.range(0, clients)
            .parallel()
            .mapToObj(i->{
                val restfulClient = restService.newClient(useRequestDebugLogging);
                return restfulClient;
            })
            .forEach(restfulClient->{

                IntStream.range(0, iterations)
                .forEach(iter->{
                    requestSingleBookOfTheWeek_viaRestEndpoint(restfulClient);
                });

            });

        });
    }

    void requestSingleBookOfTheWeek_viaRestEndpoint(final RestfulClient restfulClient) {

        val digest = restService.getRecommendedBookOfTheWeekDto(restfulClient)
                .ifFailure(Assertions::fail);

        val bookOfTheWeek = digest.getValue().orElseThrow();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

}
