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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.calc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Order(35)
@ActiveProfiles("test")
public class EditableMementoViewModel_IntegTest extends Abstract_IntegTest {

    @Override
    @TestFactory
    public Iterable<DynamicTest> each() throws IOException, URISyntaxException {
        return super.each();
    }

    @Test
    void concreteSharedLogicalTypeCanBeSubmittedToAbstractParameter() throws Exception {
        var objectMapper = new ObjectMapper();
        var createResponse = executeGraphQl(objectMapper, """
                mutation {
                  create: university_calc_Calculator__createSharedViewModel {
                    ... on rich__university_calc_EditableMementoViewModel {
                      _meta { id }
                    }
                  }
                }
                """, Map.of());
        assertThat(createResponse.at("/errors").isMissingNode()).isTrue();
        var id = createResponse.at("/data/create/_meta/id").stringValue();
        assertThat(id).isNotBlank();

        var readResponse = executeGraphQl(objectMapper, """
                query($target: rich__university_calc_EditableMementoViewModel__gqlv_input!) {
                  rich {
                    university_calc_Calculator {
                      readSharedViewModel {
                        invoke(viewModel: $target) {
                          results
                        }
                      }
                    }
                  }
                }
                """, Map.of("target", Map.of(
                        "id", id,
                        "logicalTypeName", "university_calc_EditableMementoViewModel")));
        assertThat(readResponse.at("/errors").isMissingNode()).isTrue();
        assertThat(readResponse.at(
                "/data/rich/university_calc_Calculator/readSharedViewModel/invoke/results").intValue())
                .isEqualTo(42);
    }

    private JsonNode executeGraphQl(
            final ObjectMapper objectMapper,
            final String query,
            final Map<String, Object> variables) throws Exception {
        var requestBody = objectMapper.writeValueAsString(Map.of(
                "query", query,
                "variables", variables));
        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://0.0.0.0:%d/graphql", port)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        var response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body());
    }
}
