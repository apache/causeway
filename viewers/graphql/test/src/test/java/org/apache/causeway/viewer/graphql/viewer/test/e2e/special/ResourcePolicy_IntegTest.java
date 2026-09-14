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

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

@Order(61)
@ActiveProfiles("test")
public class ResourcePolicy_IntegTest extends Abstract_IntegTest {

    @DynamicPropertySource
    static void resourcePolicies(final DynamicPropertyRegistry registry) {
        registry.add(
                "causeway.viewer.graphql.resources.structural-metadata-response-type",
                CausewayConfiguration.Viewer.Graphql.ResponseType.DIRECT::name);
        registry.add(
                "causeway.viewer.graphql.resources.value-content-response-type",
                CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN::name);
    }

    @Test
    void separatesStructuralMetadataFromValueContent() throws Exception {
        var objectMapper = new ObjectMapper();
        var graphQlResponse = executeGraphQl(objectMapper, """
                {
                  rich {
                    university_dept_Staff {
                      findStaffMemberByName {
                        invoke(name: "Gerry Jones") {
                          results {
                            _meta { id grid }
                            photo { get { name mimeType } }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        assertThat(graphQlResponse.at("/errors").isMissingNode()).isTrue();
        var resultPath = "/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results";
        var id = graphQlResponse.at(resultPath + "/_meta/id").stringValue();
        var gridUrl = graphQlResponse.at(resultPath + "/_meta/grid").stringValue();
        assertThat(gridUrl).startsWith("/graphql/object/");
        assertThat(graphQlResponse.at(resultPath + "/photo/get/name").stringValue())
                .isEqualTo("StaffMember-photo-Bar.pdf");
        assertThat(graphQlResponse.at(resultPath + "/photo/get/mimeType").stringValue())
                .isEqualTo("application/pdf");

        var schema = graphQlSourceForCauseway.schema();
        var richLobType = (GraphQLObjectType) schema.getType(
                "rich__university_dept_StaffMember__photo__gqlv_property_lob");
        var simpleLobType = (GraphQLObjectType) schema.getType(
                "simple__university_dept_StaffMember__photo__gqlv_member");
        assertThat(richLobType.getFieldDefinition("bytes")).isNull();
        assertThat(simpleLobType.getFieldDefinition("bytes")).isNull();

        var photoProperty = (GraphQLObjectType) schema.getType(
                "rich__university_dept_StaffMember__photo__gqlv_property");
        assertThat(GraphQLTypeUtil.simplePrint(
                photoProperty.getFieldDefinition("set").getArgument("photo").getType()))
                .isEqualTo("UnsupportedValue");

        var echoBlobAction = (GraphQLObjectType) schema.getType(
                "rich__university_calc_Calculator__echoBlob__gqlv_action");
        assertThat(GraphQLTypeUtil.simplePrint(
                echoBlobAction.getFieldDefinition("invoke").getArgument("value").getType()))
                .isEqualTo("UnsupportedValue!");
        var parameterMetadata = executeGraphQl(objectMapper, """
                {
                  rich {
                    university_calc_Calculator {
                      echoBlob { params { value { resourceInputMode } } }
                    }
                  }
                }
                """);
        assertThat(parameterMetadata.at(
                "/data/rich/university_calc_Calculator/echoBlob/params/value/resourceInputMode").stringValue())
                .isEqualTo("FORBIDDEN");

        var applicationType = (GraphQLObjectType) schema.getType("rich__gqlv_application_entry");
        assertThat(applicationType.getFieldDefinition("menuBars")).isNotNull();

        var menuBarsResponse = get("/graphql/application/menu-bars");
        assertThat(menuBarsResponse.statusCode()).isEqualTo(200);
        assertThat(menuBarsResponse.headers().firstValue("Content-Type").orElseThrow())
                .startsWith("application/xml");
        assertThat(menuBarsResponse.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");
        assertThat(menuBarsResponse.headers().firstValue("Content-Disposition")).isEmpty();

        var gridResponse = get(gridUrl);
        assertThat(gridResponse.statusCode()).isEqualTo(200);
        assertThat(gridResponse.headers().firstValue("Content-Type").orElseThrow())
                .startsWith("application/xml");
        assertThat(gridResponse.headers().firstValue("Content-Length").orElseThrow())
                .isEqualTo(Integer.toString(gridResponse.body().length));
        assertThat(gridResponse.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");
        assertThat(gridResponse.headers().firstValue("Content-Disposition")).isEmpty();

        var blobResponse = get(String.format(
                "/graphql/object/university.dept.StaffMember:%s/photo/blobBytes",
                id));
        assertThat(blobResponse.statusCode()).isEqualTo(403);
        assertThat(blobResponse.body()).isEmpty();
        assertThat(blobResponse.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");
    }

    private JsonNode executeGraphQl(
            final ObjectMapper objectMapper,
            final String query) throws Exception {
        var requestBody = objectMapper.writeValueAsString(Map.of("query", query));
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

    private HttpResponse<byte[]> get(final String path) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://0.0.0.0:%d%s", port, path)))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofByteArray());
    }
}
