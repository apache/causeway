/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.htmx;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.viewer.webcomponents.htmx.HtmxObjectRoute;
import org.apache.causeway.viewer.webcomponents.htmx.HtmxRouteCodec;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ReferenceAppHtmxApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo-jpa")
class ReferenceAppHtmxApplication_IntegTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    private HttpClient httpClient;

    @BeforeEach
    void authenticate() throws Exception {
        final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        final HttpResponse<String> redirect = get("/wicket/");
        assertThat(redirect.statusCode()).isEqualTo(302);
        final URI signInUri = URI.create(redirect.headers().firstValue("location").orElseThrow());
        final HttpResponse<String> signIn = httpClient.send(
                HttpRequest.newBuilder(signInUri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(signIn.statusCode()).isEqualTo(200);

        final String action = match(signIn.body(), "<form[^>]+action=\\\"([^\\\"]+)");
        final String timezone = match(signIn.body(), "<option value=\\\"([^\\\"]+)\\\">UTC</option>");
        final String body = "username=" + encode("sven")
                + "&password=" + encode("pass")
                + "&timezone=" + encode(timezone)
                + "&rememberMeContainer%3ArememberMe=on";
        final URI submitUri = signInUri.resolve(action.replace("&amp;", "&"));
        final HttpResponse<String> submitted = httpClient.send(
                HttpRequest.newBuilder(submitUri)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(submitted.statusCode()).as(submitted.body()).isIn(200, 302);
        assertThat(submitted.body()).as(submitted.body()).doesNotContain("Invalid username", "Enter username");
        final HttpResponse<String> authenticatedHome = get("/wicket/");
        assertThat(authenticatedHome.statusCode()).as(authenticatedHome.body()).isIn(200, 302);
        assertThat(authenticatedHome.headers().firstValue("location").orElse("")).doesNotContain("/wicket/signin");
        assertThat(authenticatedHome.body()).doesNotContain("Enter username");
    }

    @Test
    void servesStrictHtmxShellGraphqlAndWicketComparison() throws Exception {
        final HttpResponse<String> shell = get("/htmx");
        assertThat(shell.statusCode()).isEqualTo(200);
        assertThat(shell.headers().firstValue("content-security-policy").orElse(""))
                .contains("default-src 'self'")
                .contains("style-src-attr 'none'")
                .doesNotContain("unsafe-inline");
        assertThat(shell.body())
                .contains("<causeway-menubars>")
                .contains("id=\"causeway-route\"")
                .contains("/causeway-htmx/causeway-htmx.mjs")
                .contains("Compare Wicket viewer");

        final JsonNode root = graphQL("{__typename}");
        assertNoGraphQLErrors(root);
        assertThat(root.at("/data/__typename").asText()).isEqualTo("SimpleAndRich");

        final HttpResponse<String> wicket = getWithoutRedirect("/wicket/");
        assertThat(wicket.statusCode()).isIn(200, 302);
        assertThat(wicket.headers().firstValue("location").orElse("")).doesNotContain("/wicket/signin");
        assertThat(wicket.body()).doesNotContain("Enter username");
    }

    @Test
    void exposesStructuralResourcesAndClassifiesKnownApplicationEntryIssues() throws Exception {
        final JsonNode application = graphQL("""
                { rich { application {
                    home { kind logicalTypeName object { __typename } }
                    menuBars { href mediaType formatVersion generation cacheControl }
                    issues { code message }
                } } }
                """);
        assertNoGraphQLErrors(application);
        final JsonNode home = application.at("/data/rich/application/home");
        final var issueCodes = application.at("/data/rich/application/issues").findValuesAsText("code");
        assertThat(issueCodes).contains("INVALID_ACTION_REFERENCE");
        if (home.isNull()) {
            assertThat(issueCodes).contains("HOME_UNAVAILABLE");
        } else {
            assertThat(home.path("logicalTypeName").asText()).isNotBlank();
        }

        final String menuHref = application.at("/data/rich/application/menuBars/href").asText();
        final HttpResponse<String> menu = get(menuHref);
        assertThat(menu.statusCode()).isEqualTo(200);
        assertThat(menu.headers().firstValue("cache-control").orElse("")).isEqualTo("private, no-store");
        assertThat(menu.body()).contains("<mb:menuBars", "<mb:primary", "<mb:secondary", "<mb:tertiary");
    }

    @Test
    void exposesRepresentativeFixtureIdentityAndEffectiveGrid() throws Exception {
        final JsonNode open = graphQL("""
                { rich { demo_JavaMathTypesMenu {
                    bigDecimals { invoke { results { _meta { id logicalTypeName title } } } }
                } } }
                """);
        assertNoGraphQLErrors(open);
        final String pageId = open.at("/data/rich/demo_JavaMathTypesMenu/bigDecimals/invoke/results/_meta/id").asText();
        assertThat(pageId).isNotBlank();

        final JsonNode decimals = graphQL("""
                { rich { demo_BigDecimals(object: {id: %s}) {
                    entities { get { ... on rich__demo_BigDecimalEntity { _meta { id logicalTypeName title grid } } } }
                } } }
                """.formatted(OBJECT_MAPPER.writeValueAsString(pageId)));
        assertNoGraphQLErrors(decimals);
        final JsonNode results = decimals.at("/data/rich/demo_BigDecimals/entities/get");
        assertThat(results.isArray()).as(decimals.toPrettyString()).isTrue();
        assertThat(results.size()).isGreaterThan(0);
        final JsonNode metadata = results.get(0).path("_meta");
        assertThat(metadata.path("logicalTypeName").asText()).isEqualTo("demo.BigDecimalEntity");
        assertThat(metadata.path("id").asText()).isNotBlank();

        final HttpResponse<String> grid = get(metadata.path("grid").asText());
        assertThat(grid.statusCode()).isEqualTo(200);
        assertThat(grid.body())
                .contains("<bs:grid")
                .contains("<cpt:property")
                .doesNotContain("<bs:metadataError>");
    }

    @Test
    void directServiceAndParameterizedObjectActionMutationsUseAdvertisedShapes() throws Exception {
        final JsonNode opened = graphQL("""
                mutation {
                  demo_ActionChoicesMenu__choices {
                    _meta { id logicalTypeName title }
                  }
                }
                """);
        assertNoGraphQLErrors(opened);
        final JsonNode metadata = opened.at("/data/demo_ActionChoicesMenu__choices/_meta");
        assertThat(metadata.path("logicalTypeName").asText()).isEqualTo("demo.ActionChoices");
        final String targetId = metadata.path("id").asText();
        assertThat(targetId).isNotBlank();

        final JsonNode prepared = graphQL("""
                query {
                  rich {
                    demo_ActionChoices(object: {id: %s}) {
                      selectTvCharacter {
                        params {
                          tvCharacter {
                            choices { _meta { id logicalTypeName title } }
                          }
                        }
                      }
                    }
                  }
                }
                """.formatted(OBJECT_MAPPER.writeValueAsString(targetId)));
        assertNoGraphQLErrors(prepared);
        final String choiceId = prepared.at(
                "/data/rich/demo_ActionChoices/selectTvCharacter/params/tvCharacter/choices/0/_meta/id").asText();
        assertThat(choiceId).isNotBlank();

        final JsonNode invoked = graphQL("""
                mutation {
                  demo_ActionChoices__selectTvCharacter(
                    _target: {id: %s},
                    tvCharacter: {id: %s}) {
                    _meta { id logicalTypeName title }
                  }
                }
                """.formatted(
                        OBJECT_MAPPER.writeValueAsString(targetId),
                        OBJECT_MAPPER.writeValueAsString(choiceId)));
        assertNoGraphQLErrors(invoked);
        assertThat(invoked.at("/data/demo_ActionChoices__selectTvCharacter/_meta/id").asText())
                .isNotBlank();
        assertThat(invoked.at("/data/demo_ActionChoices__selectTvCharacter/_meta/logicalTypeName").asText())
                .isEqualTo("demo.ActionChoices");
    }

    @Test
    void versionlessViewModelsAdvertiseIdentityAndSupportConcreteCollectionRows() throws Exception {
        final JsonNode versionedMetadata = graphQL("""
                { __type(name: "rich__demo_BigDecimalEntity__gqlv_meta") { fields { name } } }
                """);
        final JsonNode actionMetadata = graphQL("""
                { __type(name: "rich__demo_ActionSemanticsVm__gqlv_meta") { fields { name } } }
                """);
        final JsonNode rowMetadata = graphQL("""
                { __type(name: "rich__demo_CollectionLayoutPagedChildVm__gqlv_meta") { fields { name } } }
                """);
        assertNoGraphQLErrors(versionedMetadata);
        assertNoGraphQLErrors(actionMetadata);
        assertNoGraphQLErrors(rowMetadata);
        assertThat(versionedMetadata.at("/data/__type/fields").findValuesAsText("name"))
                .contains("id", "logicalTypeName", "title", "version");
        assertThat(actionMetadata.at("/data/__type/fields").findValuesAsText("name"))
                .contains("id", "logicalTypeName", "title")
                .doesNotContain("version");
        assertThat(rowMetadata.at("/data/__type/fields").findValuesAsText("name"))
                .contains("id", "logicalTypeName", "title")
                .doesNotContain("version");

        final JsonNode opened = graphQL("""
                { rich { demo_CollectionLayoutMenu {
                    paged { invoke { results { _meta { id logicalTypeName title } } } }
                } } }
                """);
        assertNoGraphQLErrors(opened);
        final String pageId = opened.at("/data/rich/demo_CollectionLayoutMenu/paged/invoke/results/_meta/id").asText();
        assertThat(pageId).isNotBlank();

        final JsonNode rows = graphQL("""
                { rich { demo_CollectionLayoutPagedPage(object: {id: %s}) {
                    children { get { _meta { id logicalTypeName title } value { get } } }
                } } }
                """.formatted(OBJECT_MAPPER.writeValueAsString(pageId)));
        assertNoGraphQLErrors(rows);
        final JsonNode firstRow = rows.at("/data/rich/demo_CollectionLayoutPagedPage/children/get/0");
        assertThat(firstRow.at("/_meta/id").asText()).isNotBlank();
        assertThat(firstRow.at("/_meta/logicalTypeName").asText())
                .isEqualTo("demo.CollectionLayoutPagedChildVm");
        assertThat(firstRow.at("/value/get").asText()).isNotBlank();
    }

    @Test
    void polymorphicCollectionsCompleteUnionMembershipAndAcceptFragments() throws Exception {
        final JsonNode unionType = graphQL("""
                { __type(name: "rich__demo_ValueHolder__gqlv_union") {
                    kind
                    possibleTypes { kind name }
                } }
                """);
        assertNoGraphQLErrors(unionType);
        final JsonNode possibleTypes = unionType.at("/data/__type/possibleTypes");
        assertThat(possibleTypes.isArray()).isTrue();
        assertThat(possibleTypes.size()).isEqualTo(28);
        assertThat(possibleTypes.findValuesAsText("name"))
                .contains("rich__demo_ActionChoicesFromEntity")
                .doesNotContain("rich__demo_CollectionTypeOfChildVm");
        final JsonNode opened = graphQL("""
                { rich { demo_ActionMenu {
                    choicesFrom { invoke { results { _meta { id logicalTypeName title } } } }
                } } }
                """);
        assertNoGraphQLErrors(opened);
        final String pageId = opened.at("/data/rich/demo_ActionMenu/choicesFrom/invoke/results/_meta/id").asText();
        assertThat(pageId).isNotBlank();

        final String target = "demo_ActionChoicesFromPage(object: {id: "
                + OBJECT_MAPPER.writeValueAsString(pageId) + "})";
        final JsonNode probe = graphQL("{ rich { " + target
                + " { objects { get { __typename } } } } }");
        assertNoGraphQLErrors(probe);
        assertThat(probe.at("/data/rich/demo_ActionChoicesFromPage/objects/get").findValuesAsText("__typename"))
                .containsOnly("rich__demo_ActionChoicesFromEntity");

        final JsonNode invalid = graphQL("{ rich { " + target
                + " { objects { get { _meta { id } } } } } }");
        assertThat(invalid.at("/errors/0/message").asText()).contains("_meta", "ValueHolder");

        final JsonNode projected = graphQL("""
                { rich { %s {
                    objects { get {
                        __typename
                        ... on rich__demo_ActionChoicesFromEntity {
                            _meta { id logicalTypeName title }
                            name { get }
                        }
                    } }
                } } }
                """.formatted(target));
        assertNoGraphQLErrors(projected);
        final JsonNode first = projected.at("/data/rich/demo_ActionChoicesFromPage/objects/get/0");
        assertThat(first.path("__typename").asText()).isEqualTo("rich__demo_ActionChoicesFromEntity");
        assertThat(first.at("/_meta/logicalTypeName").asText()).isEqualTo("demo.ActionChoicesFromEntity");
        assertThat(first.at("/_meta/id").asText()).isNotBlank();
        assertThat(first.at("/name/get").asText()).isNotBlank();
    }

    @Test
    void actionReferenceAutocompleteExposesBoundedServerWindowsAndLegacyCompatibility() throws Exception {
        final JsonNode opened = graphQL("""
                mutation {
                    demo_ActionAutoCompleteMenu__autoComplete {
                        _meta { id logicalTypeName title }
                    }
                }
                """);
        assertNoGraphQLErrors(opened);
        final String pageId = opened.at("/data/demo_ActionAutoCompleteMenu__autoComplete/_meta/id").asText();
        assertThat(pageId).isNotBlank();

        final String target = "demo_ActionAutoCompletePage(object: {id: "
                + OBJECT_MAPPER.writeValueAsString(pageId) + "})";
        final JsonNode described = graphQL("""
                { __type(name: "rich__demo_ActionAutoCompletePage__selectTvCharacter__tvCharacter__gqlv_action_parameter") {
                    fields { name args { name defaultValue } type { kind name ofType { kind name } } }
                } }
                """);
        assertNoGraphQLErrors(described);
        assertThat(described.at("/data/__type/fields").findValuesAsText("name"))
                .contains("autoComplete", "autoCompleteWindow");
        final JsonNode retainedLegacyType = graphQL("""
                { __type(name: "rich__demo_ValueHolder__name__gqlv_property") { name } }
                """);
        assertNoGraphQLErrors(retainedLegacyType);
        assertThat(retainedLegacyType.at("/data/__type/name").asText()).isNotBlank();

        final JsonNode first = graphQL("""
                { rich { %s { selectTvCharacter { params { tvCharacter {
                    autoCompleteWindow(search: "o", offset: 0, size: 5) {
                        items { _meta { id logicalTypeName title } }
                        offset requestedSize returnedCount totalCount maximumSize
                        hasPrevious hasNext ordering
                    }
                } } } } } }
                """.formatted(target));
        assertNoGraphQLErrors(first);
        final JsonNode firstWindow = first.at(
                "/data/rich/demo_ActionAutoCompletePage/selectTvCharacter/params/tvCharacter/autoCompleteWindow");
        assertThat(firstWindow.path("offset").asInt()).isZero();
        assertThat(firstWindow.path("requestedSize").asInt()).isEqualTo(5);
        assertThat(firstWindow.path("returnedCount").asInt()).isEqualTo(5);
        assertThat(firstWindow.path("totalCount").asInt()).isEqualTo(7);
        assertThat(firstWindow.path("maximumSize").asInt()).isEqualTo(5);
        assertThat(firstWindow.path("hasPrevious").asBoolean()).isFalse();
        assertThat(firstWindow.path("hasNext").asBoolean()).isTrue();
        assertThat(firstWindow.path("ordering").asText()).isEqualTo("APPLICATION");

        final JsonNode later = graphQL("""
                { rich { %s { selectTvCharacter { params { tvCharacter {
                    autoCompleteWindow(search: "o", offset: 5, size: 5) {
                        items { _meta { id logicalTypeName title } }
                        offset returnedCount totalCount hasPrevious hasNext
                    }
                } } } } } }
                """.formatted(target));
        assertNoGraphQLErrors(later);
        final JsonNode laterWindow = later.at(
                "/data/rich/demo_ActionAutoCompletePage/selectTvCharacter/params/tvCharacter/autoCompleteWindow");
        assertThat(laterWindow.path("offset").asInt()).isEqualTo(5);
        assertThat(laterWindow.path("returnedCount").asInt()).isGreaterThan(0);
        assertThat(laterWindow.path("totalCount").asInt()).isEqualTo(firstWindow.path("totalCount").asInt());
        assertThat(laterWindow.path("hasPrevious").asBoolean()).isTrue();
        assertThat(firstWindow.at("/items").findValuesAsText("id"))
                .doesNotContainAnyElementsOf(laterWindow.at("/items").findValuesAsText("id"));

        final JsonNode boundaries = graphQL("""
                { rich { %s { selectTvCharacter { params { tvCharacter {
                    defaulted: autoCompleteWindow(search: "o") {
                        offset requestedSize returnedCount totalCount
                    }
                    empty: autoCompleteWindow(search: "o", offset: 100, size: 5) {
                        items { _meta { id } } offset returnedCount totalCount hasPrevious hasNext
                    }
                } } } } } }
                """.formatted(target));
        assertNoGraphQLErrors(boundaries);
        final JsonNode defaulted = boundaries.at(
                "/data/rich/demo_ActionAutoCompletePage/selectTvCharacter/params/tvCharacter/defaulted");
        assertThat(defaulted.path("offset").asInt()).isZero();
        assertThat(defaulted.path("requestedSize").asInt()).isEqualTo(5);
        assertThat(defaulted.path("returnedCount").asInt()).isEqualTo(5);
        final JsonNode empty = boundaries.at(
                "/data/rich/demo_ActionAutoCompletePage/selectTvCharacter/params/tvCharacter/empty");
        assertThat(empty.path("items").size()).isZero();
        assertThat(empty.path("offset").asInt()).isEqualTo(100);
        assertThat(empty.path("returnedCount").asInt()).isZero();
        assertThat(empty.path("totalCount").asInt()).isEqualTo(firstWindow.path("totalCount").asInt());
        assertThat(empty.path("hasPrevious").asBoolean()).isTrue();
        assertThat(empty.path("hasNext").asBoolean()).isFalse();

        final JsonNode legacy = graphQL("""
                { rich { %s { selectTvCharacter { params { tvCharacter {
                    autoComplete(search: "o") { _meta { id logicalTypeName title } }
                } } } } } }
                """.formatted(target));
        assertNoGraphQLErrors(legacy);
        final JsonNode legacyItems = legacy.at(
                "/data/rich/demo_ActionAutoCompletePage/selectTvCharacter/params/tvCharacter/autoComplete");
        assertThat(legacyItems.size()).isEqualTo(firstWindow.path("totalCount").asInt());
        assertThat(Stream.concat(
                firstWindow.at("/items").findValuesAsText("id").stream(),
                laterWindow.at("/items").findValuesAsText("id").stream()).toList())
                .containsExactlyElementsOf(legacyItems.findValuesAsText("id"));

        for (String arguments : List.of(
                "offset: 0, size: 6",
                "offset: -1, size: 5",
                "offset: 0, size: 0")) {
            final JsonNode invalid = graphQL("""
                    { rich { %s { selectTvCharacter { params { tvCharacter {
                        autoCompleteWindow(search: "protected-search", %s) { totalCount }
                    } } } } } }
                    """.formatted(target, arguments));
            assertThat(invalid.at("/errors/0/message").asText())
                    .containsIgnoringCase("autocomplete window")
                    .doesNotContain("protected-search");
        }
    }

    @Test
    void compositeViewModelIdentityRoundTripsThroughTheBoundedCanonicalRoute() throws Exception {
        final JsonNode opened = graphQL("""
                mutation {
                    demo_CompositeValueTypeMenu__compositeValueTypes {
                        _meta { id logicalTypeName title }
                    }
                }
                """);
        assertNoGraphQLErrors(opened);
        final JsonNode metadata = opened.at(
                "/data/demo_CompositeValueTypeMenu__compositeValueTypes/_meta");
        final String identifier = metadata.path("id").asText();
        assertThat(metadata.path("logicalTypeName").asText()).isEqualTo("demo.CompositeValuesPage");
        assertThat(identifier.length()).isGreaterThan(1024).isLessThanOrEqualTo(4096);

        final HtmxRouteCodec codec = new HtmxRouteCodec("/htmx");
        final HtmxObjectRoute route = new HtmxObjectRoute("demo.CompositeValuesPage", identifier);
        final String canonicalPath = codec.objectPath(route);
        assertThat(canonicalPath.substring(canonicalPath.lastIndexOf('/') + 1).length())
                .isLessThanOrEqualTo(4096);
        assertThat(codec.parseObjectPath(canonicalPath)).isEqualTo(route);

        final HttpResponse<String> page = get(canonicalPath);
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body())
                .contains("data-route-state=\"loading\"")
                .contains("logical-type=\"demo.CompositeValuesPage\"")
                .doesNotContain("data-route-state=\"invalid-route\"");

        final JsonNode reconstructed = graphQL("""
                { rich { demo_CompositeValuesPage(object: {id: %s}) {
                    _meta { id logicalTypeName title }
                    complexNumber { get }
                } } }
                """.formatted(OBJECT_MAPPER.writeValueAsString(identifier)));
        assertNoGraphQLErrors(reconstructed);
        assertThat(reconstructed.at("/data/rich/demo_CompositeValuesPage/_meta/id").asText())
                .isEqualTo(identifier);
        assertThat(reconstructed.at("/data/rich/demo_CompositeValuesPage/complexNumber/get").isMissingNode())
                .isFalse();
    }

    @Test
    void exactDecimalPropertyMutationUsesAdvertisedStringAndRestoresFixture() throws Exception {
        final JsonNode open = graphQL("""
                { rich { demo_JavaMathTypesMenu {
                    bigDecimals { invoke { results { _meta { id } } } }
                } } }
                """);
        assertNoGraphQLErrors(open);
        final String pageId = open.at("/data/rich/demo_JavaMathTypesMenu/bigDecimals/invoke/results/_meta/id").asText();
        final JsonNode decimals = graphQL("""
                { rich { demo_BigDecimals(object: {id: %s}) {
                    entities { get { ... on rich__demo_BigDecimalEntity {
                        _meta { id }
                        readWriteProperty { get }
                    } } }
                } } }
                """.formatted(OBJECT_MAPPER.writeValueAsString(pageId)));
        assertNoGraphQLErrors(decimals);

        final JsonNode action = graphQL("""
                { rich { demo_BigDecimals(object: {id: %s}) {
                    openViewModel { invoke(initialValue: "9007199254740993.1200") {
                        results { ... on rich__demo_BigDecimalVm { readWriteProperty { get } } }
                    } }
                } } }
                """.formatted(OBJECT_MAPPER.writeValueAsString(pageId)));
        assertNoGraphQLErrors(action);
        assertThat(action.at("/data/rich/demo_BigDecimals/openViewModel/invoke/results/readWriteProperty/get").asText())
                .isEqualTo("9007199254740993.1200");

        final JsonNode entity = decimals.at("/data/rich/demo_BigDecimals/entities/get/0");
        final String entityId = entity.at("/_meta/id").asText();
        final String original = entity.at("/readWriteProperty/get").asText();
        assertThat(entityId).isNotBlank();
        assertThat(original).isNotBlank();

        final String mutation = "mutation { demo_BigDecimalEntity__readWriteProperty(_target: {id: "
                + OBJECT_MAPPER.writeValueAsString(entityId) + "}, readWriteProperty: %s) "
                + "{ readWriteProperty { get } } }";
        final String malformedValue = "PRIVATE_INVALID_DECIMAL_1.2.3";
        final JsonNode malformed = graphQL(mutation.formatted(OBJECT_MAPPER.writeValueAsString(malformedValue)));
        assertThat(malformed.at("/errors/0/message").asText())
                .contains("Invalid BigDecimal value")
                .doesNotContain(malformedValue, "NumberFormatException", "java.math");
        final JsonNode unchanged = graphQL("{ rich { demo_BigDecimalEntity(object: {id: "
                + OBJECT_MAPPER.writeValueAsString(entityId) + "}) { readWriteProperty { get } } } }");
        assertNoGraphQLErrors(unchanged);
        assertThat(unchanged.at("/data/rich/demo_BigDecimalEntity/readWriteProperty/get").asText())
                .isEqualTo(original);
        try {
            final JsonNode changed = graphQL(mutation.formatted("\"9007199254740993.1200\""));
            assertNoGraphQLErrors(changed);
            assertThat(changed.at("/data/demo_BigDecimalEntity__readWriteProperty/readWriteProperty/get").asText())
                    .isEqualTo("9007199254740993.1200");
        } finally {
            final JsonNode restored = graphQL(mutation.formatted(OBJECT_MAPPER.writeValueAsString(original)));
            assertNoGraphQLErrors(restored);
            assertThat(restored.at("/data/demo_BigDecimalEntity__readWriteProperty/readWriteProperty/get").asText())
                    .isEqualTo(original);
        }
    }

    @Test
    void nullableBooleanAndOffsetAndZonedTemporalActionsRoundTrip() throws Exception {
        final JsonNode booleans = graphQL("""
                {
                  rich {
                    demo_JavaLangWrapperTypesMenu {
                      booleans {
                        invoke {
                          results {
                            ... on rich__demo_WrapperBooleans {
                              entities { get { ... on rich__demo_WrapperBooleanEntity {
                                _meta { id }
                                readWriteOptionalProperty { get }
                              } } }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(booleans);
        final JsonNode booleanEntity = booleans.at(
                "/data/rich/demo_JavaLangWrapperTypesMenu/booleans/invoke/results/entities/get/0");
        final String booleanId = booleanEntity.at("/_meta/id").asText();
        final JsonNode originalBoolean = booleanEntity.at("/readWriteOptionalProperty/get");
        final String booleanMutation = "mutation { demo_WrapperBooleanEntity__readWriteOptionalProperty(_target: {id: "
                + OBJECT_MAPPER.writeValueAsString(booleanId) + "}, readWriteOptionalProperty: %s) "
                + "{ readWriteOptionalProperty { get } } }";
        try {
            final JsonNode falseValue = graphQL(booleanMutation.formatted("false"));
            assertNoGraphQLErrors(falseValue);
            assertThat(falseValue.at("/data/demo_WrapperBooleanEntity__readWriteOptionalProperty/readWriteOptionalProperty/get").asBoolean())
                    .isFalse();
            final JsonNode nullValue = graphQL(booleanMutation.formatted("null"));
            assertNoGraphQLErrors(nullValue);
            assertThat(nullValue.at("/data/demo_WrapperBooleanEntity__readWriteOptionalProperty/readWriteOptionalProperty/get").isNull())
                    .isTrue();
        } finally {
            final JsonNode restored = graphQL(booleanMutation.formatted(
                    originalBoolean.isNull() ? "null" : Boolean.toString(originalBoolean.asBoolean())));
            assertNoGraphQLErrors(restored);
        }

        final JsonNode offset = graphQL("""
                {
                  rich {
                    demo_JavaTimeTypesMenu {
                      offsetDateTimes {
                        invoke {
                          results {
                            ... on rich__demo_OffsetDateTimes {
                              openViewModel {
                                invoke(initialValue: "2026-08-23T10:15:30.123456789-04:00") {
                                  results {
                                    ... on rich__demo_OffsetDateTimeVm { readWriteProperty { get } }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        assertNoGraphQLErrors(offset);
        assertThat(offset.at("/data/rich/demo_JavaTimeTypesMenu/offsetDateTimes/invoke/results/openViewModel/invoke/results/readWriteProperty/get").asText())
                .contains("2026-08-23T10:15:30.123456789-04:00");

        final String zonedValue = "2026-11-01T01:30:00-04:00[America/New_York]";
        final JsonNode zoned = graphQL("""
                {
                  rich {
                    demo_JavaTimeTypesMenu {
                      zonedDateTimes {
                        invoke {
                          results {
                            ... on rich__demo_ZonedDateTimes {
                              openViewModel {
                                invoke(initialValue: %s) {
                                  results {
                                    ... on rich__demo_ZonedDateTimeVm { readWriteProperty { get } }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """.formatted(OBJECT_MAPPER.writeValueAsString(zonedValue)));
        assertNoGraphQLErrors(zoned);
        assertThat(ZonedDateTime.parse(zoned.at(
                "/data/rich/demo_JavaTimeTypesMenu/zonedDateTimes/invoke/results/openViewModel/invoke/results/readWriteProperty/get").asText()))
                .isEqualTo(ZonedDateTime.parse(zonedValue));
    }

    @Test
    void semanticTargetCatalogueReferencesAdvertisedRichRoots() throws Exception {
        final JsonNode rootType = graphQL("""
                { __type(name: "RICHSchema") { fields { name } } }
                """);
        assertNoGraphQLErrors(rootType);
        final var roots = rootType.at("/data/__type/fields").findValuesAsText("name");
        final JsonNode catalogue = OBJECT_MAPPER.readTree(
                getClass().getResourceAsStream("/org/apache/causeway/regressiontests/referenceapp/referenceapp-targets.json"));
        for (final JsonNode target : catalogue.path("targets")) {
            assertThat(roots).as(target.toString()).contains(target.path("rootField").asText());
        }
    }

    private JsonNode graphQL(final String query) throws Exception {
        final HttpRequest request = HttpRequest.newBuilder(uri("/graphql"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("sven:pass".getBytes(StandardCharsets.UTF_8)))
                .POST(HttpRequest.BodyPublishers.ofString(
                        OBJECT_MAPPER.writeValueAsString(Map.of("query", query)),
                        StandardCharsets.UTF_8))
                .build();
        final HttpResponse<String> response = client().send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        return OBJECT_MAPPER.readTree(response.body());
    }

    private static void assertNoGraphQLErrors(final JsonNode response) {
        assertThat(response.path("errors").isMissingNode() || response.path("errors").isEmpty())
                .as(response.toPrettyString())
                .isTrue();
    }

    private HttpResponse<String> get(final String path) throws Exception {
        return client().send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getWithoutRedirect(final String path) throws Exception {
        return client().send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpClient client() {
        return httpClient;
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String match(final String value, final String regex) {
        final var matcher = Pattern.compile(regex).matcher(value);
        assertThat(matcher.find()).as(regex).isTrue();
        return matcher.group(1);
    }

    private URI uri(final String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
