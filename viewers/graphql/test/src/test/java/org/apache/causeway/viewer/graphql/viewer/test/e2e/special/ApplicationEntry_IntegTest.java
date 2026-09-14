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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

@Order(66)
@ActiveProfiles("test")
public class ApplicationEntry_IntegTest extends Abstract_IntegTest {

    @Override
    @TestFactory
    public Iterable<DynamicTest> each() throws IOException, URISyntaxException {
        return super.each();
    }

    @Test
    void menuBarsResourceIsAuthorizedFilteredAndNoStore() throws Exception {
        var response = submitFileNamed("ApplicationEntry_IntegTest.each.application_entry._.gql");
        var root = new ObjectMapper().readTree(response);
        assertThat(root.at("/errors").isMissingNode()).isTrue();
        var menuBars = root.at("/data/rich/application/menuBars");
        var href = menuBars.get("href").stringValue();
        assertThat(href).isEqualTo("/graphql/application/menu-bars");

        var resource = get(href, null);
        assertThat(resource.statusCode()).isEqualTo(200);
        assertThat(resource.headers().firstValue("Content-Type").orElseThrow())
                .startsWith("application/xml");
        assertThat(resource.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");
        assertThat(resource.headers().firstValue("ETag").orElseThrow())
                .isEqualTo('"' + menuBars.get("generation").stringValue() + '"');
        assertThat(resource.headers().firstValue("X-Content-Type-Options").orElseThrow())
                .isEqualTo("nosniff");

        var xml = new String(resource.body(), StandardCharsets.UTF_8);
        assertThat(xml)
                .contains("<mb:primary")
                .contains("<mb:secondary")
                .contains("<mb:tertiary")
                .contains("objectType=\"university.dept.Departments\" id=\"findAllDepartments\"")
                .contains("objectType=\"university.admin.AdminMenu\" id=\"otherAdminAction\"")
                .doesNotContain("missingAction")
                .doesNotContain("Invalid Fixture Entry")
                .doesNotContain("id=\"adminAction\"");

        var localizedResource = get(href, "fr-FR");
        assertThat(localizedResource.statusCode()).isEqualTo(200);
        assertThat(localizedResource.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");

        var schema = graphQlSourceForCauseway.schema();
        var richRoot = (GraphQLObjectType) schema.getType("RICHSchema");
        assertThat(richRoot.getFieldDefinition("application")).isNotNull();
        var departmentsService = (GraphQLObjectType) schema.getType("rich__university_dept_Departments");
        var actionType = (GraphQLNamedType) GraphQLTypeUtil.unwrapAll(
                departmentsService.getFieldDefinition("findAllDepartments").getType());
        var establishedAction = (GraphQLObjectType) schema.getType(actionType.getName());
        assertThat(establishedAction.getFieldDefinition("invoke")).isNotNull();
    }

    private HttpResponse<byte[]> get(
            final String path,
            final String acceptLanguage) throws IOException, InterruptedException {
        var uri = URI.create(String.format("http://0.0.0.0:%d", port)).resolve(path);
        var requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30));
        if (acceptLanguage != null) {
            requestBuilder.header("Accept-Language", acceptLanguage);
        }
        var request = requestBuilder.GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
