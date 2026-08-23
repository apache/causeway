/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceAppCapabilityInventoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReferenceAppCapabilityInventory inventory = new ReferenceAppCapabilityInventory(objectMapper);

    @Test
    void classifiesRepresentativeDomainAndValueShapesDeterministically() throws Exception {
        final JsonNode source = objectMapper.readTree("""
                {"data":{"__schema":{"types":[
                  {"kind":"OBJECT","name":"rich__demo_Sample","description":"password=secret stack trace","fields":[]},
                  {"kind":"OBJECT","name":"rich__demo_Sample__name__gqlv_property","fields":[{"name":"get","type":{"kind":"SCALAR","name":"String"}}]},
                  {"kind":"OBJECT","name":"rich__demo_Sample__amount__gqlv_property","fields":[{"name":"get","type":{"kind":"SCALAR","name":"BigDecimal"}}]},
                  {"kind":"OBJECT","name":"rich__demo_Sample__custom__gqlv_property","fields":[{"name":"get","type":{"kind":"SCALAR","name":"UnsupportedValue"}}]},
                  {"kind":"OBJECT","name":"rich__demo_Sample__items__gqlv_collection","fields":[{"name":"get","type":{"kind":"LIST","name":null,"ofType":{"kind":"OBJECT","name":"rich__demo_Item"}}}]},
                  {"kind":"OBJECT","name":"rich__demo_Sample__change__gqlv_action","fields":[]},
                  {"kind":"OBJECT","name":"rich__demo_Sample__change__value__gqlv_action_parameter","fields":[]},
                  {"kind":"ENUM","name":"rich__demoapp_Example__gqlv_enum","fields":[]},
                  {"kind":"SCALAR","name":"String"},
                  {"kind":"SCALAR","name":"BigDecimal"},
                  {"kind":"SCALAR","name":"UnsupportedValue"}
                ]}}}
                """);

        final JsonNode first = inventory.generate(source);
        final JsonNode second = inventory.generate(source);

        assertThat(first).isEqualTo(second);
        assertThat(first.path("itemCount").asInt()).isEqualTo(12);
        assertThat(classification(first, "rich__demo_Sample__name__gqlv_property")).isEqualTo("SUPPORTED");
        assertThat(classification(first, "rich__demo_Sample__amount__gqlv_property")).isEqualTo("VIEWER_DEFECT");
        assertThat(classification(first, "rich__demo_Sample__custom__gqlv_property")).isEqualTo("GRACEFUL_UNSUPPORTED");
        assertThat(classification(first, "rich__demo_Sample__change__value__gqlv_action_parameter")).isEqualTo("NOT_EXERCISED");
        assertThat(first.toString()).doesNotContain("secret", "stack trace");
        assertThat(first.path("classificationCounts").fieldNames())
                .toIterable()
                .containsExactly(
                        "SUPPORTED",
                        "GRACEFUL_UNSUPPORTED",
                        "GRAPHQL_GAP",
                        "VIEWER_DEFECT",
                        "VIEWER_SPECIFIC",
                        "NOT_EXERCISED");
    }

    @Test
    void conditionallyAdvertisedValueHolderMembersNormalizeToOneReviewedBaseline() throws Exception {
        final JsonNode withoutConditionalMembers = objectMapper.readTree("""
                {"data":{"__schema":{"types":[
                  {"kind":"OBJECT","name":"rich__demo_ValueHolder","fields":[]}
                ]}}}
                """);
        final JsonNode withConditionalMembers = objectMapper.readTree("""
                {"data":{"__schema":{"types":[
                  {"kind":"OBJECT","name":"rich__demo_ValueHolder","fields":[]},
                  {"kind":"OBJECT","name":"rich__demo_ValueHolder__blob__gqlv_property","fields":[{"name":"get","type":{"kind":"SCALAR","name":"BlobValue"}}]},
                  {"kind":"OBJECT","name":"rich__demo_ValueHolder__count__gqlv_property","fields":[{"name":"get","type":{"kind":"SCALAR","name":"Int"}}]},
                  {"kind":"OBJECT","name":"rich__demo_ValueHolder__incrementRedirectEvenIfSame__gqlv_action","fields":[]},
                  {"kind":"OBJECT","name":"rich__demo_ValueHolder__incrementRedirectOnlyIfDiffers__gqlv_action","fields":[]}
                ]}}}
                """);

        final JsonNode normalizedWithout = inventory.generate(withoutConditionalMembers);
        final JsonNode normalizedWith = inventory.generate(withConditionalMembers);

        assertThat(normalizedWithout).isEqualTo(normalizedWith);
        assertThat(normalizedWith.at("/classificationCounts/NOT_EXERCISED").asInt()).isEqualTo(5);
        assertThat(classification(normalizedWith, "rich__demo_ValueHolder__blob__gqlv_property"))
                .isEqualTo("NOT_EXERCISED");
    }

    @Test
    void additionsAndRemovalsChangeTheCanonicalHash() throws Exception {
        final JsonNode empty = objectMapper.readTree("{\"data\":{\"__schema\":{\"types\":[]}}}");
        final JsonNode oneType = objectMapper.readTree("""
                {"data":{"__schema":{"types":[{"kind":"OBJECT","name":"rich__demo_Sample","fields":[]}]}}}
                """);

        assertThat(inventory.generate(empty).path("itemsSha256").asText())
                .isNotEqualTo(inventory.generate(oneType).path("itemsSha256").asText());
    }

    @Test
    void duplicateDiscoveredItemsAreRejected() throws Exception {
        final JsonNode duplicate = objectMapper.readTree("""
                {"data":{"__schema":{"types":[
                  {"kind":"OBJECT","name":"rich__demo_Sample","fields":[]},
                  {"kind":"OBJECT","name":"rich__demo_Sample","fields":[]}
                ]}}}
                """);

        assertThatThrownBy(() -> inventory.generate(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate inventory item");
    }

    @Test
    void missingIntrospectionTypesAreRejected() throws Exception {
        assertThatThrownBy(() -> inventory.generate(objectMapper.readTree("{}")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("introspection response");
    }

    private static String classification(final JsonNode inventory, final String id) {
        for (final JsonNode item : inventory.path("items")) {
            if (id.equals(item.path("id").asText())) {
                return item.path("classification").asText();
            }
        }
        throw new AssertionError("Missing inventory item " + id);
    }
}
