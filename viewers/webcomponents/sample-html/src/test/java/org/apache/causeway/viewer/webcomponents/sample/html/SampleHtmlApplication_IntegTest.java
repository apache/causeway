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
package org.apache.causeway.viewer.webcomponents.sample.html;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.viewer.webcomponents.sample.html.domain.SampleObject;
import org.apache.causeway.viewer.webcomponents.sample.html.domain.SampleRelatedObject;

@SpringBootTest(
        classes = SampleHtmlApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SampleHtmlApplication_IntegTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void servesSamplePageAndPackagedFoundationModule() throws Exception {
        final var page = get("/sample-html/index.html");
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.headers().firstValue("content-type").orElse(""))
                .contains("text/html");
        assertThat(page.body())
                .contains("data-testid=\"sample-app\"")
                .contains("data-testid=\"sample-object\"")
                .contains("data-testid=\"object-header\"")
                .contains("data-testid=\"property-name\"")
                .contains("data-testid=\"property-code\"")
                .contains("data-testid=\"property-secret\"")
                .contains("data-testid=\"property-summary\"")
                .contains("data-testid=\"property-capacity\"")
                .contains("data-testid=\"property-featured\"")
                .contains("data-testid=\"property-status\"")
                .contains("data-testid=\"property-notes\"")
                .contains("data-testid=\"property-related-object\"")
                .contains("data-testid=\"action-inspect\"")
                .contains("data-testid=\"action-archive\"")
                .contains("data-testid=\"action-hidden\"")
                .contains("data-testid=\"collection-related-objects\"")
                .contains("data-testid=\"collection-empty-related-objects\"")
                .contains("data-testid=\"column-related-name\"")
                .contains("data-testid=\"column-related-code\"")
                .contains("data-testid=\"sample-event\"")
                .contains("data-testid=\"collection-related-count\"")
                .contains("data-testid=\"section-object-summary\"")
                .contains("data-testid=\"section-properties\"")
                .contains("data-testid=\"section-actions\"")
                .contains("data-testid=\"section-collections\"")
                .contains("data-testid=\"section-events\"")
                .contains("data-testid=\"sample-coverage\"")
                .contains("Read-only component showcase")
                .contains("prefers-color-scheme: dark")
                .contains("await import('/causeway-webcomponents/index.mjs')")
                .doesNotContain(SampleObject.SAMPLE_SECRET);

        final var module = get("/causeway-webcomponents/index.mjs");
        assertThat(module.statusCode()).isEqualTo(200);
        assertThat(module.body())
                .contains("defineCausewayWebComponents")
                .contains("./register.mjs")
                .contains("./action-element.mjs")
                .contains("./collection-element.mjs")
                .contains("./value-renderers.mjs");
    }

    @Test
    void exposesTargetedRichSchemaIntrospectionAndDeterministicObject() throws Exception {
        final var introspection = graphQL("""
                query CausewaySampleDescribe {
                  __type(name: "rich__causeway_webcomponents_sample_SampleObject") {
                    name
                    fields {
                      name
                      type {
                        kind
                        name
                        ofType { kind name }
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(introspection);
        assertThat(introspection.at("/data/__type/name").asString())
                .isEqualTo("rich__causeway_webcomponents_sample_SampleObject");
        assertThat(introspection.at("/data/__type/fields").toString())
                .contains(
                        "_meta", "name", "code", "secret", "summary", "capacity", "featured",
                        "status", "notes", "relatedObject", "inspect", "archive", "hiddenAction",
                        "relatedObjects", "emptyRelatedObjects");

        final var statusIntrospection = graphQL("""
                query CausewaySampleStatusDescribe {
                  __type(name: "rich__causeway_webcomponents_sample_SampleObject__status__gqlv_property") {
                    fields {
                      name
                      type {
                        kind
                        name
                        ofType { kind name }
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(statusIntrospection);
        assertThat(statusIntrospection.at("/data/__type/fields").toString())
                .contains("get", "ENUM", "SampleStatus");

        final var collectionIntrospection = graphQL("""
                query CausewaySampleCollectionDescribe {
                  __type(name: "rich__causeway_webcomponents_sample_SampleObject__relatedObjects__gqlv_collection") {
                    fields {
                      name
                      type {
                        kind
                        name
                        ofType { kind name }
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(collectionIntrospection);
        assertThat(collectionIntrospection.at("/data/__type/fields").toString())
                .contains("hidden", "disabled", "get", "LIST", "SampleRelatedObject");

        final var objectRead = graphQL("""
                query CausewaySampleRead {
                  rich {
                    causeway_webcomponents_sample_SampleObject(object: {id: "s_sample-1"}) {
                      _meta { id logicalTypeName version title }
                      name { hidden disabled get }
                      code { hidden disabled get }
                      secret { hidden disabled get }
                      summary { hidden disabled datatype get }
                      capacity { hidden disabled datatype get }
                      featured { hidden disabled datatype get }
                      status { hidden disabled datatype get }
                      notes { hidden disabled datatype get }
                      relatedObject {
                        hidden
                        disabled
                        get { _meta { id logicalTypeName version title } }
                      }
                      inspect { hidden disabled }
                      archive { hidden disabled }
                      hiddenAction { hidden disabled }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(objectRead);
        final var object = objectRead.at("/data/rich/causeway_webcomponents_sample_SampleObject");
        assertThat(object.at("/_meta/id").asString()).isEqualTo(SampleObject.SAMPLE_BOOKMARK_ID);
        assertThat(object.at("/_meta/logicalTypeName").asString()).isEqualTo(SampleObject.LOGICAL_TYPE_NAME);
        assertThat(object.at("/_meta/version").asString()).isNotBlank();
        assertThat(object.at("/_meta/title").asString())
                .isEqualTo(SampleObject.SAMPLE_NAME + " [" + SampleObject.SAMPLE_CODE + "]");
        assertThat(object.at("/name/hidden").asBoolean()).isFalse();
        assertThat(object.at("/name/disabled").isNull()).isTrue();
        assertThat(object.at("/name/get").asString()).isEqualTo(SampleObject.SAMPLE_NAME);
        assertThat(object.at("/code/hidden").asBoolean()).isFalse();
        assertThat(object.at("/code/disabled").asString()).isEqualTo(SampleObject.CODE_DISABLED_REASON);
        assertThat(object.at("/code/get").asString()).isEqualTo(SampleObject.SAMPLE_CODE);
        assertThat(object.at("/secret/hidden").asBoolean()).isTrue();
        assertThat(object.at("/summary/hidden").asBoolean()).isFalse();
        assertThat(object.at("/summary/disabled").isNull()).isTrue();
        assertThat(object.at("/summary/get").asString()).isEqualTo(SampleObject.SAMPLE_SUMMARY);
        assertThat(object.at("/capacity/hidden").asBoolean()).isFalse();
        assertThat(object.at("/capacity/disabled").isNull()).isTrue();
        assertThat(object.at("/capacity/get").asInt()).isEqualTo(SampleObject.SAMPLE_CAPACITY);
        assertThat(object.at("/featured/hidden").asBoolean()).isFalse();
        assertThat(object.at("/featured/disabled").isNull()).isTrue();
        assertThat(object.at("/featured/get").asBoolean()).isEqualTo(SampleObject.SAMPLE_FEATURED);
        assertThat(object.at("/status/hidden").asBoolean()).isFalse();
        assertThat(object.at("/status/disabled").isNull()).isTrue();
        assertThat(object.at("/status/get").asString()).isEqualTo(SampleObject.SAMPLE_STATUS.name());
        assertThat(object.at("/notes/hidden").asBoolean()).isFalse();
        assertThat(object.at("/notes/disabled").isNull()).isTrue();
        assertThat(object.at("/notes/get").isNull()).isTrue();
        assertThat(object.at("/relatedObject/disabled").asString()).isNotBlank();
        assertThat(object.at("/relatedObject/get/_meta/id").asString())
                .isEqualTo(SampleRelatedObject.FIRST_BOOKMARK_ID);
        assertThat(object.at("/relatedObject/get/_meta/logicalTypeName").asString())
                .isEqualTo(SampleRelatedObject.LOGICAL_TYPE_NAME);
        assertThat(object.at("/inspect/hidden").asBoolean()).isFalse();
        assertThat(object.at("/inspect/disabled").isNull()).isTrue();
        assertThat(object.at("/archive/hidden").asBoolean()).isFalse();
        assertThat(object.at("/archive/disabled").asString())
                .isEqualTo(SampleObject.ARCHIVE_DISABLED_REASON);
        assertThat(object.at("/hiddenAction/hidden").asBoolean()).isTrue();

        final var collectionRead = graphQL("""
                query CausewaySampleCollectionsRead {
                  rich {
                    causeway_webcomponents_sample_SampleObject(object: {id: "s_sample-1"}) {
                      relatedObjects {
                        hidden
                        disabled
                        get {
                          _meta { id logicalTypeName version title }
                          name { hidden disabled datatype get }
                          code { hidden disabled datatype get }
                        }
                      }
                      emptyRelatedObjects {
                        hidden
                        disabled
                        get { _meta { id logicalTypeName version title } }
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(collectionRead);
        final var relatedObjects = collectionRead.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/relatedObjects");
        assertThat(relatedObjects.at("/hidden").asBoolean()).isFalse();
        assertThat(relatedObjects.at("/get").size()).isEqualTo(2);
        assertThat(relatedObjects.at("/get/0/_meta/id").asString())
                .isEqualTo(SampleRelatedObject.FIRST_BOOKMARK_ID);
        assertThat(relatedObjects.at("/get/0/name/get").asString())
                .isEqualTo(SampleRelatedObject.FIRST_NAME);
        assertThat(relatedObjects.at("/get/0/code/get").asString())
                .isEqualTo(SampleRelatedObject.FIRST_CODE);
        assertThat(relatedObjects.at("/get/1/_meta/id").asString())
                .isEqualTo(SampleRelatedObject.SECOND_BOOKMARK_ID);
        assertThat(relatedObjects.at("/get/1/name/get").asString())
                .isEqualTo(SampleRelatedObject.SECOND_NAME);
        assertThat(relatedObjects.at("/get/1/code/get").asString())
                .isEqualTo(SampleRelatedObject.SECOND_CODE);
        assertThat(collectionRead.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/emptyRelatedObjects/get").size())
                .isZero();
    }

    private HttpResponse<String> get(final String path) throws Exception {
        final var request = HttpRequest.newBuilder(uri(path)).GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode graphQL(final String query) throws Exception {
        final var requestBody = OBJECT_MAPPER.writeValueAsString(Map.of("query", query));
        final var request = HttpRequest.newBuilder(uri("/graphql"))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        final var response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return OBJECT_MAPPER.readTree(response.body());
    }

    private void assertNoGraphQLErrors(final JsonNode response) {
        assertThat(response.at("/errors").isMissingNode())
                .as(response.toPrettyString())
                .isTrue();
    }

    private URI uri(final String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
