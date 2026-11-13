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

import graphql.schema.GraphQLObjectType;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

@Order(67)
@ActiveProfiles("test")
public class ApplicationEntryForbiddenResource_IntegTest extends Abstract_IntegTest {

    @DynamicPropertySource
    static void resourcePolicy(final DynamicPropertyRegistry registry) {
        registry.add(
                "causeway.viewer.graphql.resources.structural-metadata-response-type",
                CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN::name);
    }

    @Test
    void omitsMenuCapabilityAndRejectsDereference() throws Exception {
        var schema = graphQlSourceForCauseway.schema();
        var applicationType = (GraphQLObjectType) schema.getType("rich__gqlv_application_entry");
        assertThat(applicationType.getFieldDefinition("menuBars")).isNull();
        assertThat(applicationType.getFieldDefinition("home")).isNotNull();

        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(
                        "http://0.0.0.0:%d/graphql/application/menu-bars",
                        port)))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        var response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).isEmpty();
        assertThat(response.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");
    }
}
