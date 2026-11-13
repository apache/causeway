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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@Order(63)
@ActiveProfiles("test")
public class LegacyUnsupportedOutputPolicy_IntegTest extends Abstract_IntegTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void legacyUnsupportedOutput(final DynamicPropertyRegistry registry) {
        registry.add(
                "causeway.viewer.graphql.values.unsupported-output-policy",
                CausewayConfiguration.Viewer.Graphql.Values.UnsupportedOutputPolicy.LEGACY_STRING::name);
    }

    @Test
    void legacyStringOutputIsExplicitAndDoesNotEnableInput() throws Exception {
        var output = executeGraphQl("""
                {
                  rich {
                    university_calc_Calculator {
                      sampleUnmappedValue { invoke { results } }
                    }
                  }
                }
                """, Map.of());
        assertThat(output.at("/data/rich/university_calc_Calculator/sampleUnmappedValue/invoke/results")
                .stringValue()).isEqualTo("NEVER_DISCLOSE_UNMAPPED_VALUE");

        var input = executeGraphQl("""
                query Unsupported($value: UnsupportedValue!) {
                  rich {
                    university_calc_Calculator {
                      echoUnmappedValue { invoke(value: $value) { results } }
                    }
                  }
                }
                """, Map.of("value", "PRIVATE_LEGACY_INPUT"));
        assertThat(input.at("/errors/0/message").stringValue())
                .contains("no reversible GraphQL input strategy")
                .doesNotContain("PRIVATE_LEGACY_INPUT");
    }

    private JsonNode executeGraphQl(
            final String query,
            final Map<String, ?> variables) throws Exception {
        var requestBody = objectMapper.writeValueAsString(Map.of(
                "query", query,
                "variables", variables));
        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://0.0.0.0:%d/graphql", port)))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        var response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body());
    }
}
