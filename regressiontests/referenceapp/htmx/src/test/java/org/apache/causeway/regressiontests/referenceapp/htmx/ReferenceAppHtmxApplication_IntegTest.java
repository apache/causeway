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
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

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
