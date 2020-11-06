/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.testdomain.rest;

import org.apache.isis.testdomain.conf.Configuration_headlessButSecure;
import org.apache.isis.testdomain.rospec.Configuration_usingRoSpec;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = RestEndpointService.class,
        properties = {
                "sven = pass, admin_role",
                "admin_role = *"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        Configuration_headlessButSecure.class,
        Configuration_usingRoSpec.class,
        IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
})
class CorsFilterTest {

    @LocalServerPort
    private int port;

    @Test
    public void ensureCorsFilterIsInChain() {
        final String url = "http://localhost:" + port + "/restful/user";
        final TestRestTemplate testRestTemplate = new TestRestTemplate();
        final ResponseEntity<String> response = testRestTemplate
                //TODO sven/pass not enforced due to usage of IsisModuleCoreSecurity _headlessButSecure
                .withBasicAuth("user", "passwd")
                .getForEntity(url, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertTrue(response.getBody().length() > 0);
    }

}

