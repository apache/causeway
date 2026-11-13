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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.viewer.webcomponents.sample.html.domain.SampleMenu;
import org.apache.causeway.viewer.webcomponents.sample.html.domain.SampleObject;
import org.apache.causeway.viewer.webcomponents.sample.html.domain.SampleRelatedObject;

@SpringBootTest(
        classes = SampleHtmlApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SampleHtmlApplication_IntegTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void initializesGraphQLSourceOnceForConcurrentFirstRequests() throws Exception {
        final var start = new CountDownLatch(1);
        final var executor = Executors.newFixedThreadPool(6);
        try {
            final var requests = IntStream.range(0, 6)
                    .mapToObj(index -> executor.submit(() -> {
                        start.await();
                        return graphQL("query CausewayConcurrentStartup" + index + " { __typename }");
                    }))
                    .toList();
            start.countDown();
            for (var request : requests) {
                assertNoGraphQLErrors(request.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void servesSamplePageAndPackagedFoundationModule() throws Exception {
        final var page = get("/sample-html/index.html");
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.headers().firstValue("content-type").orElse(""))
                .contains("text/html");
        assertThat(page.body())
                .contains("data-testid=\"sample-app\"")
                .contains("data-testid=\"sample-object\"")
                .contains("data-testid=\"section-menubars\"")
                .contains("data-testid=\"menubars-composite\"")
                .contains("data-testid=\"menubar-primary-standalone\"")
                .contains("data-testid=\"menubar-secondary-standalone\"")
                .contains("data-testid=\"menubar-tertiary-standalone\"")
                .contains("data-testid=\"menubars-state\"")
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
                .contains("data-testid=\"action-update-details\"")
                .contains("data-testid=\"action-find-related\"")
                .contains("data-testid=\"action-clear-notes\"")
                .contains("data-testid=\"action-archive\"")
                .contains("data-testid=\"action-hidden\"")
                .contains("data-testid=\"collection-related-objects\"")
                .contains("data-testid=\"collection-empty-related-objects\"")
                .contains("data-testid=\"column-related-name\"")
                .contains("data-testid=\"column-related-code\"")
                .contains("data-testid=\"sample-event\"")
                .contains("data-testid=\"sample-interaction-event\"")
                .contains("data-testid=\"action-interaction-controller\"")
                .contains("data-testid=\"collection-related-count\"")
                .contains("data-testid=\"section-object-summary\"")
                .contains("data-testid=\"section-properties\"")
                .contains("data-testid=\"section-actions\"")
                .contains("data-testid=\"section-collections\"")
                .contains("data-testid=\"section-events\"")
                .contains("data-testid=\"sample-coverage\"")
                .contains("data-testid=\"section-composite-object\"")
                .contains("data-testid=\"composite-object-context\"")
                .contains("data-testid=\"composite-object\"")
                .contains("<cw-object editable")
                .contains("Component interaction showcase")
                .contains("Qualified presentation")
                .contains("?component-toolkit=native")
                .contains("?menu-state=empty")
                .contains("?menu-state=unsupported")
                .contains("?fail-field=basic")
                .contains("?fail-action=true")
                .contains("?fail-menubar=true")
                .contains("dataset.causewayPresentation = componentToolkit")
                .contains("dataset.causewayActionButtons = componentToolkit")
                .contains("dataset.causewayApplicationMenubar = forcedMenuState ? 'native' : componentToolkit")
                .contains("/causeway-webcomponents/component-styles.css")
                .contains("/causeway-webcomponents/theme.css")
                .contains("await import('/causeway-webcomponents/index.mjs')")
                .doesNotContain(SampleObject.SAMPLE_SECRET);

        final var structuralStyles = get("/causeway-webcomponents/component-styles.css");
        assertThat(structuralStyles.statusCode()).isEqualTo(200);
        assertThat(structuralStyles.body()).contains(".causeway-menubar-bar-disclosure");

        final var theme = get("/causeway-webcomponents/theme.css");
        assertThat(theme.statusCode()).isEqualTo(200);
        assertThat(theme.body())
                .contains("prefers-color-scheme: dark")
                .contains("prefers-reduced-motion: reduce")
                .contains("forced-colors: active");

        final var module = get("/causeway-webcomponents/index.mjs");
        assertThat(module.statusCode()).isEqualTo(200);
        assertThat(module.body())
                .contains("defineCausewayWebComponents")
                .contains("./register.mjs")
                .contains("./action-element.mjs")
                .contains("./collection-element.mjs")
                .contains("./editor-registry.mjs")
                .contains("./interaction-controller-element.mjs")
                .contains("./interaction-operations.mjs")
                .contains("./menu-context-controller.mjs")
                .contains("./menu-layout.mjs")
                .contains("./menubar-element.mjs")
                .contains("./menubar-projection.mjs")
                .contains("./menubar-qualification.mjs")
                .contains("./menubar-widget.mjs")
                .contains("./menubars-element.mjs")
                .contains("./object-element.mjs")
                .contains("./object-layout.mjs")
                .contains("./value-renderers.mjs");

        final var actionAsset = get("/causeway-webcomponents/vaadin-actions/vaadin-actions.js");
        assertThat(actionAsset.statusCode()).isEqualTo(200);
        assertThat(actionAsset.body()).contains("vaadin-button");

        final var actionPolicy = get("/causeway-webcomponents/vaadin-actions/csp-policy.json");
        assertThat(actionPolicy.statusCode()).isEqualTo(200);
        assertThat(actionPolicy.body())
                .contains("styleHashes")
                .contains("sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=");

        final var menubarAsset = get("/causeway-webcomponents/vaadin-menubar/vaadin-menubar.js");
        assertThat(menubarAsset.statusCode()).isEqualTo(200);
        assertThat(menubarAsset.body()).contains("vaadin-menu-bar");

        final var menubarPolicy = get("/causeway-webcomponents/vaadin-menubar/csp-policy.json");
        assertThat(menubarPolicy.statusCode()).isEqualTo(200);
        assertThat(menubarPolicy.body())
                .contains("styleHashes")
                .contains("sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=");
    }

    @Test
    void exposesAuthorizedEffectiveGridForCompositeObject() throws Exception {
        final var response = get("/graphql/object/causeway.webcomponents.sample.SampleObject:s_sample-1/_meta/grid");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type").orElse(""))
                .contains("application/xml");
        assertThat(response.headers().firstValue("cache-control").orElse(""))
                .isEqualTo("private, no-store");
        assertThat(response.body())
                .contains("<bs:grid")
                .contains("<cpt:domainObject")
                .contains("unreferencedProperties=\"true\"")
                .contains("unreferencedCollections=\"true\"")
                .doesNotContain(SampleObject.SAMPLE_SECRET);
    }

    @Test
    void exposesAuthorizedEffectiveMenuBarsAndServiceActions() throws Exception {
        final var application = graphQL("""
                query CausewaySampleApplicationEntry {
                  rich {
                    application {
                      menuBars {
                        href
                        mediaType
                        formatVersion
                        generation
                        cacheControl
                      }
                      issues { code message }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(application);
        assertThat(application.at("/data/rich/application/menuBars/href").asString())
                .isEqualTo("/graphql/application/menu-bars");
        assertThat(application.at("/data/rich/application/menuBars/cacheControl").asString())
                .isEqualTo("private, no-store");
        assertThat(application.at("/data/rich/application/issues").toString())
                .contains("INVALID_ACTION_REFERENCE")
                .doesNotContain("missingAction");

        final var menuBars = get(application.at("/data/rich/application/menuBars/href").asString());
        assertThat(menuBars.statusCode()).isEqualTo(200);
        assertThat(menuBars.headers().firstValue("content-type").orElse(""))
                .contains("application/xml");
        assertThat(menuBars.headers().firstValue("cache-control").orElse(""))
                .isEqualTo("private, no-store");
        assertThat(menuBars.body())
                .contains("<mb:primary")
                .contains("<mb:secondary")
                .contains("<mb:tertiary")
                .contains("objectType=\"" + SampleMenu.LOGICAL_TYPE_NAME + "\"")
                .contains("id=\"welcomeMessage\"")
                .contains("id=\"greet\"")
                .contains("id=\"clearSampleNotes\"")
                .contains("id=\"disabledAction\"")
                .doesNotContain("hiddenAction")
                .doesNotContain("missingAction")
                .doesNotContain(SampleObject.SAMPLE_SECRET);

        final var service = graphQL("""
                query CausewaySampleServiceActions {
                  rich {
                    causeway_webcomponents_sample_SampleMenu {
                      welcomeMessage {
                        hidden
                        disabled
                        invoke { results }
                      }
                      greet {
                        hidden
                        disabled
                        params {
                          name {
                            hidden
                            disabled
                            default
                            choices
                            datatype
                          }
                        }
                        validate(name: "")
                      }
                      disabledAction { hidden disabled }
                      hiddenAction { hidden }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(service);
        assertThat(service.at("/data/rich/causeway_webcomponents_sample_SampleMenu/welcomeMessage/invoke/results").asString())
                .isEqualTo("Welcome to Causeway web components.");
        assertThat(service.at("/data/rich/causeway_webcomponents_sample_SampleMenu/greet/params/name/default").asString())
                .isEqualTo("Ada");
        assertThat(service.at("/data/rich/causeway_webcomponents_sample_SampleMenu/greet/params/name/choices").toString())
                .contains("Ada", "Grace", "Linus");
        assertThat(service.at("/data/rich/causeway_webcomponents_sample_SampleMenu/greet/validate").asString())
                .contains(SampleMenu.GREETING_VALIDATION_REASON)
                .contains("'Name' is mandatory");
        assertThat(service.at("/data/rich/causeway_webcomponents_sample_SampleMenu/disabledAction/disabled").asString())
                .isEqualTo(SampleMenu.DISABLED_REASON);
        assertThat(service.at("/data/rich/causeway_webcomponents_sample_SampleMenu/hiddenAction/hidden").asBoolean())
                .isTrue();

        final var mutation = graphQL("""
                mutation CausewaySampleClearNotes {
                  causeway_webcomponents_sample_SampleMenu__clearSampleNotes
                }
                """);
        assertNoGraphQLErrors(mutation);
        assertThat(mutation.at("/data/causeway_webcomponents_sample_SampleMenu__clearSampleNotes").isNull())
                .isTrue();
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
                        "status", "notes", "relatedObject", "inspect", "updateDetails", "findRelated",
                        "clearNotes", "archive", "hiddenAction", "relatedObjects", "emptyRelatedObjects");

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

    @Test
    void exposesDeterministicPropertyAndActionInteractions() throws Exception {
        final var capacityInteraction = graphQL("""
                query CausewaySampleCapacityInteraction {
                  rich {
                    causeway_webcomponents_sample_SampleObject(object: {id: "s_sample-1"}) {
                      capacity {
                        validateInvalid: validate(capacity: 0)
                        validateValid: validate(capacity: 31)
                      }
                      status { choices }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(capacityInteraction);
        assertThat(capacityInteraction.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/capacity/validateInvalid").asString())
                .isEqualTo(SampleObject.CAPACITY_VALIDATION_REASON);
        assertThat(capacityInteraction.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/capacity/validateValid").isNull())
                .isTrue();
        assertThat(capacityInteraction.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/status/choices").toString())
                .contains("ACTIVE", "PAUSED");

        final var capacityUpdate = graphQL("""
                mutation CausewaySampleCapacityUpdate {
                  causeway_webcomponents_sample_SampleObject__capacity(
                    _target: {id: "s_sample-1"}, capacity: 31) {
                    _meta { id version }
                    capacity { get }
                  }
                }
                """);
        assertNoGraphQLErrors(capacityUpdate);
        assertThat(capacityUpdate.at(
                "/data/causeway_webcomponents_sample_SampleObject__capacity/capacity/get").asInt())
                .isEqualTo(31);

        final var capacityRestore = graphQL("""
                mutation CausewaySampleCapacityRestore {
                  causeway_webcomponents_sample_SampleObject__capacity(
                    _target: {id: "s_sample-1"}, capacity: 24) {
                    capacity { get }
                  }
                }
                """);
        assertNoGraphQLErrors(capacityRestore);
        assertThat(capacityRestore.at(
                "/data/causeway_webcomponents_sample_SampleObject__capacity/capacity/get").asInt())
                .isEqualTo(SampleObject.SAMPLE_CAPACITY);

        final var safeActions = graphQL("""
                query CausewaySampleSafeActions {
                  rich {
                    causeway_webcomponents_sample_SampleObject(object: {id: "s_sample-1"}) {
                      inspect { invoke { results } }
                      findRelated {
                        params { search { autoComplete(search: "Frame") } }
                        invoke(search: "Framework") {
                          results { _meta { id logicalTypeName title } }
                        }
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(safeActions);
        assertThat(safeActions.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/inspect/invoke/results").asString())
                .isEqualTo(SampleObject.SAMPLE_NAME + " [" + SampleObject.SAMPLE_CODE + "]");
        assertThat(safeActions.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/findRelated/params/search/autoComplete").toString())
                .contains(SampleRelatedObject.SECOND_NAME);
        assertThat(safeActions.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/findRelated/invoke/results").size())
                .isEqualTo(1);

        final var actionNegotiation = graphQL("""
                query CausewaySampleUpdateDetailsNegotiation {
                  rich {
                    causeway_webcomponents_sample_SampleObject(object: {id: "s_sample-1"}) {
                      updateDetails {
                        params {
                          summary { hidden disabled default validity datatype }
                          status { hidden disabled default choices validity datatype }
                        }
                        invalid: validate(summary: "short", status: ACTIVE)
                        valid: validate(summary: "A sufficiently detailed summary", status: PAUSED)
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(actionNegotiation);
        final var updateDetails = actionNegotiation.at(
                "/data/rich/causeway_webcomponents_sample_SampleObject/updateDetails");
        assertThat(updateDetails.at("/params/summary/default").asString()).isEqualTo(SampleObject.SAMPLE_SUMMARY);
        assertThat(updateDetails.at("/params/status/choices").toString()).contains("ACTIVE", "PAUSED");
        assertThat(updateDetails.at("/invalid").asString()).isEqualTo(SampleObject.ACTION_VALIDATION_REASON);
        assertThat(updateDetails.at("/valid").isNull()).isTrue();

        final var updateDetailsMutation = graphQL("""
                mutation CausewaySampleUpdateDetails {
                  causeway_webcomponents_sample_SampleObject__updateDetails(
                    _target: {id: "s_sample-1"},
                    summary: "A deterministic interaction mutation result.",
                    status: PAUSED) {
                    _meta { id logicalTypeName version title }
                    summary { get }
                    status { get }
                  }
                }
                """);
        assertNoGraphQLErrors(updateDetailsMutation);
        final var updated = updateDetailsMutation.at(
                "/data/causeway_webcomponents_sample_SampleObject__updateDetails");
        assertThat(updated.at("/_meta/id").asString()).isEqualTo(SampleObject.SAMPLE_BOOKMARK_ID);
        assertThat(updated.at("/status/get").asString()).isEqualTo("PAUSED");

        final var restore = graphQL("""
                mutation CausewaySampleInteractionRestore {
                  summary: causeway_webcomponents_sample_SampleObject__summary(
                    _target: {id: "s_sample-1"},
                    summary: "A deterministic reference page composed entirely from semantic Causeway web components.") {
                    summary { get }
                  }
                  status: causeway_webcomponents_sample_SampleObject__status(
                    _target: {id: "s_sample-1"}, status: ACTIVE) {
                    status { get }
                  }
                  clearNotes: causeway_webcomponents_sample_SampleObject__clearNotes(
                    _target: {id: "s_sample-1"})
                }
                """);
        assertNoGraphQLErrors(restore);
        assertThat(restore.at("/data/summary/summary/get").asString()).isEqualTo(SampleObject.SAMPLE_SUMMARY);
        assertThat(restore.at("/data/status/status/get").asString()).isEqualTo(SampleObject.SAMPLE_STATUS.name());
        assertThat(restore.at("/data/clearNotes").isNull()).isTrue();
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
