/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.htmx;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.regressiontests.referenceapp.inventory.ReferenceAppCapabilityInventory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ReferenceAppHtmxApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo-jpa")
class ReferenceAppCapabilityInventory_IntegTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void capabilityInventoryMatchesReviewedBaseline() throws Exception {
        final JsonNode introspection = graphQL("""
                { __schema { types {
                    kind
                    name
                    fields(includeDeprecated: true) {
                        name
                        type { kind name ofType { kind name ofType { kind name } } }
                        args { name type { kind name ofType { kind name } } }
                    }
                    inputFields { name type { kind name ofType { kind name } } }
                    enumValues(includeDeprecated: true) { name }
                } } }
                """);
        assertThat(introspection.path("errors").isMissingNode() || introspection.path("errors").isEmpty())
                .as(introspection.toPrettyString()).isTrue();

        final JsonNode actual = new ReferenceAppCapabilityInventory(OBJECT_MAPPER).generate(introspection);
        assertJourneyTargetsExist(introspection);
        assertThat(introspection.at("/data/__schema/types").size()).isGreaterThan(9_000);
        assertThat(actual.path("itemCount").asInt()).isGreaterThan(4_000);
        assertThat(actual.at("/classificationCounts/VIEWER_DEFECT").asInt()).isZero();
        assertThat(actual.at("/classificationCounts/GRACEFUL_UNSUPPORTED").asInt()).isGreaterThan(0);
        assertThat(actual.at("/classificationCounts/VIEWER_SPECIFIC").asInt()).isGreaterThan(0);
        assertThat(actual.at("/classificationCounts/NOT_EXERCISED").asInt()).isGreaterThan(0);

        final Path baseline = Path.of("src/test/resources/referenceapp-capability-inventory.json");
        if (Boolean.getBoolean("referenceapp.inventory.update")) {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(baseline.toFile(), actual);
        }
        final JsonNode expected = OBJECT_MAPPER.readTree(Files.readString(baseline));
        assertThat(actual.path("itemsSha256").asText())
                .as("capability inventory hash; actual counts=%s expected counts=%s",
                        actual.path("classificationCounts"), expected.path("classificationCounts"))
                .isEqualTo(expected.path("itemsSha256").asText());
        assertThat(actual).isEqualTo(expected);
    }

    private void assertJourneyTargetsExist(final JsonNode introspection) throws Exception {
        final Map<String, Set<String>> fieldsByType = new HashMap<>();
        for (final JsonNode type : introspection.at("/data/__schema/types")) {
            final Set<String> fields = new HashSet<>();
            for (final JsonNode field : type.path("fields")) {
                fields.add(field.path("name").asText());
            }
            fieldsByType.put(type.path("name").asText(), fields);
        }

        final JsonNode catalogue;
        try (var input = getClass().getResourceAsStream(
                "/org/apache/causeway/regressiontests/referenceapp/referenceapp-targets.json")) {
            assertThat(input).isNotNull();
            catalogue = OBJECT_MAPPER.readTree(input);
        }
        assertThat(catalogue.path("schemaVersion").asInt()).isEqualTo(1);
        assertThat(catalogue.path("upstreamRevision").asText()).isNotBlank();
        final Set<String> families = new HashSet<>();
        for (final JsonNode target : catalogue.path("targets")) {
            assertThat(families.add(target.path("family").asText())).as(target.toString()).isTrue();
            final String typeName = "rich__" + target.path("rootField").asText();
            assertThat(fieldsByType).as(target.toString()).containsKey(typeName);
            assertThat(fieldsByType.get(typeName)).as(target.toString())
                    .contains(target.path("member").asText());
        }
        assertThat(families).contains(
                "action-choices", "action-autocomplete", "reference-autocomplete-window",
                "action-defaults", "action-validation",
                "action-parameterless", "action-parameterized", "action-scalar-outcome",
                "action-object-outcome", "action-cancel-stale-concurrency",
                "versionless-preparation", "versionless-action-result", "versionless-collection-row",
                "property-text", "property-multiline", "property-hidden", "property-disabled",
                "property-editing", "property-validation", "property-cancel-stale",
                "collection-empty-populated", "collection-paging-columns",
                "collection-polymorphism", "polymorphic-union-valid", "polymorphic-union-unreadable",
                "collection-associated-actions", "collection-stale-partial",
                "boolean-nullable", "numeric-decimal", "numeric-integer", "temporal-local-date",
                "temporal-offset-date-time", "temporal-zoned-date-time", "enum",
                "reference-autocomplete", "url", "protected-password", "blob", "clob", "custom-value",
                "opaque-route");
    }

    private JsonNode graphQL(final String query) throws Exception {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        OBJECT_MAPPER.writeValueAsString(Map.of("query", query)),
                        StandardCharsets.UTF_8))
                .build();
        final HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        return OBJECT_MAPPER.readTree(response.body());
    }
}
