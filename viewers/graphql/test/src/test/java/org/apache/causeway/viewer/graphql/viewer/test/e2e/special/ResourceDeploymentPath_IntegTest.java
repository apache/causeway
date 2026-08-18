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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.special;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

@Order(62)
@ActiveProfiles("test")
public class ResourceDeploymentPath_IntegTest extends Abstract_IntegTest {

    @DynamicPropertySource
    static void deploymentPaths(final DynamicPropertyRegistry registry) {
        registry.add("server.servlet.context-path", () -> "/causeway");
        registry.add("spring.graphql.http.path", () -> "/api/query");
        registry.add("causeway.viewer.graphql.resources.external-path-prefix", () -> "/public");
        registry.add(
                "causeway.viewer.graphql.resources.structural-metadata-response-type",
                CausewayConfiguration.Viewer.Graphql.ResponseType.DIRECT::name);
    }

    @Test
    void retainsServletProxyAndNonDefaultGraphQlPaths() throws Exception {
        var objectMapper = new ObjectMapper();
        var requestBody = objectMapper.writeValueAsString(Map.of("query", """
                {
                  rich {
                    university_dept_Staff {
                      findStaffMemberByName {
                        invoke(name: "Gerry Jones") {
                          results { _meta { grid } }
                        }
                      }
                    }
                  }
                }
                """));
        var graphQlRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format(
                        "http://0.0.0.0:%d/causeway/api/query",
                        port)))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        var graphQlResponse = HttpClient.newHttpClient().send(
                graphQlRequest,
                HttpResponse.BodyHandlers.ofString());
        assertThat(graphQlResponse.statusCode()).isEqualTo(200);
        var body = objectMapper.readTree(graphQlResponse.body());
        assertThat(body.at("/errors").isMissingNode()).isTrue();
        var gridPath = body.at(
                "/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results/_meta/grid")
                .stringValue();
        assertThat(gridPath)
                .matches("/public/causeway/api/query/object/university.dept.StaffMember:(\\d+)/_meta/grid")
                .doesNotStartWith("//");

        var internalPath = gridPath.substring("/public".length());
        var resourceRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://0.0.0.0:%d%s", port, internalPath)))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        var resourceResponse = HttpClient.newHttpClient().send(
                resourceRequest,
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(resourceResponse.statusCode()).isEqualTo(200);
        assertThat(resourceResponse.body()).isNotEmpty();
    }
}
