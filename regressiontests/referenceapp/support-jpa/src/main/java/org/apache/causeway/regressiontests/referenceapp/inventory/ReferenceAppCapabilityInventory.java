/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.inventory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ReferenceAppCapabilityInventory {

    public static final int SCHEMA_VERSION = 1;
    public static final String UPSTREAM_REVISION = "29b43bfe4f77d525fb345394e5a52bd7d85a91ba";

    private static final Set<String> EXACT_NUMERIC_TYPES = Set.of("BigDecimal", "BigInteger", "Long");
    private static final Set<String> SUPPORTED_SCALARS = Set.of(
            "Boolean", "Byte", "Char", "Float", "ID", "Int", "Locale", "Short", "String",
            "LocalDate", "LocalDateTime", "LocalTime", "UUID", "Url");
    private static final Set<String> UNSUPPORTED_TEMPORAL_TYPES = Set.of(
            "DateTime", "LegacyDateTime", "OffsetDateTime", "OffsetTime", "ZonedDateTime");
    private static final Set<String> RESOURCE_TYPES = Set.of(
            "BlobValue", "ClobValue", "LocalResourcePathInput", "LocalResourcePathValue");

    private final ObjectMapper objectMapper;

    public ReferenceAppCapabilityInventory(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode generate(final JsonNode introspectionResponse) {
        final JsonNode types = introspectionResponse.at("/data/__schema/types");
        if (!types.isArray()) {
            throw new IllegalArgumentException("GraphQL introspection response has no /data/__schema/types array");
        }

        final List<Item> items = new ArrayList<>();
        for (final JsonNode type : types) {
            final String name = type.path("name").asText("");
            if (isReferenceDomainType(name)) {
                addDomainType(items, type, name);
            }
        }
        addValueShapeItems(items, types);
        normalizeConditionalValueHolderMembers(items, types);
        items.add(new Item(
                "viewer-specific:wicket-custom-panels",
                "VIEWER_EXTENSION",
                null,
                Classification.VIEWER_SPECIFIC,
                "Copied domain excludes private Wicket panel implementations; semantic fallbacks remain in scope."));
        items.add(new Item(
                "not-exercised:destructive-action-exhaustion",
                "EXECUTION_POLICY",
                null,
                Classification.NOT_EXERCISED,
                "Every action is inventoried, but destructive execution uses reviewed representative disposable journeys."));
        items.sort(Comparator.comparing(Item::id));
        final Set<String> itemIds = new HashSet<>();
        for (final Item item : items) {
            if (!itemIds.add(item.id())) {
                throw new IllegalArgumentException("Duplicate inventory item: " + item.id());
            }
        }

        final Map<Classification, Integer> classifications = new LinkedHashMap<>();
        for (final Classification classification : Classification.values()) {
            classifications.put(classification, 0);
        }
        for (final Item item : items) {
            classifications.compute(item.classification(), (key, value) -> value + 1);
        }

        final ArrayNode itemNodes = objectMapper.createArrayNode();
        for (final Item item : items) {
            final ObjectNode node = itemNodes.addObject();
            node.put("id", item.id());
            node.put("kind", item.kind());
            if (item.valueType() != null) {
                node.put("valueType", item.valueType());
            }
            node.put("classification", item.classification().name());
            node.put("reason", item.reason());
        }

        final ObjectNode root = objectMapper.createObjectNode();
        root.put("schemaVersion", SCHEMA_VERSION);
        root.put("upstreamRevision", UPSTREAM_REVISION);
        root.put("scope", "rich demo base objects, semantic member wrappers, action parameters, enums, and advertised standard value shapes");
        root.put("itemCount", items.size());
        final ObjectNode counts = root.putObject("classificationCounts");
        classifications.forEach((classification, count) -> counts.put(classification.name(), count));
        root.put("itemsSha256", sha256(itemNodes.toString()));
        root.set("items", itemNodes);
        return root;
    }

    private void addDomainType(final List<Item> items, final JsonNode type, final String name) {
        final String kind = type.path("kind").asText("UNKNOWN");
        if (name.endsWith("__gqlv_property")) {
            final String valueType = fieldNamedType(type, "get");
            items.add(classifyProperty(name, valueType));
        } else if (name.endsWith("__gqlv_collection")) {
            items.add(new Item(name, "COLLECTION", fieldNamedType(type, "get"), Classification.SUPPORTED,
                    "Generic collections use bounded public windows and semantic row contexts."));
        } else if (name.endsWith("__gqlv_action")) {
            items.add(new Item(name, "ACTION", null, Classification.SUPPORTED,
                    "Generic action state, preparation, validation, invocation, and outcomes are supported."));
        } else if (name.endsWith("__gqlv_action_parameter")) {
            items.add(new Item(name, "ACTION_PARAMETER", null, Classification.NOT_EXERCISED,
                    "Parameter shape is inventoried; runtime preparation and representative journeys determine value support."));
        } else if (!name.contains("__gqlv_")) {
            final Classification classification = "ENUM".equals(kind) ? Classification.SUPPORTED : Classification.SUPPORTED;
            items.add(new Item(name, "ENUM".equals(kind) ? "ENUM" : "OBJECT", null, classification,
                    "The public rich type is discoverable through the generic semantic object or enum contract."));
        }
    }

    private Item classifyProperty(final String name, final String valueType) {
        if (valueType == null || valueType.isBlank()) {
            return new Item(name, "PROPERTY", null, Classification.GRAPHQL_GAP,
                    "The advertised property has no discoverable get value type.");
        }
        if ("UnsupportedValue".equals(valueType)) {
            return new Item(name, "PROPERTY", valueType, Classification.GRACEFUL_UNSUPPORTED,
                    "GraphQL explicitly advertises an unsupported value and the viewer renders a bounded unsupported state.");
        }
        if (EXACT_NUMERIC_TYPES.contains(valueType)) {
            return new Item(name, "PROPERTY", valueType, Classification.VIEWER_DEFECT,
                    "The current standard editor coerces this exact numeric type through JavaScript Number.");
        }
        if (UNSUPPORTED_TEMPORAL_TYPES.contains(valueType)) {
            return new Item(name, "PROPERTY", valueType, Classification.VIEWER_DEFECT,
                    "The public scalar is advertised but the standard editor does not preserve its temporal semantics.");
        }
        if (valueType.startsWith("LocalResourcePath")) {
            return new Item(name, "PROPERTY", valueType, Classification.GRACEFUL_UNSUPPORTED,
                    "Resource metadata is public but the standard semantic editor has no resource-input control.");
        }
        if (RESOURCE_TYPES.contains(valueType) || SUPPORTED_SCALARS.contains(valueType)
                || valueType.startsWith("rich__") || valueType.startsWith("[") || valueType.startsWith("simple__")) {
            return new Item(name, "PROPERTY", valueType, Classification.SUPPORTED,
                    "The value shape has an existing scalar, enum, reference, collection, or resource presentation contract.");
        }
        return new Item(name, "PROPERTY", valueType, Classification.GRACEFUL_UNSUPPORTED,
                "The value shape is discoverable but requires a reviewed renderer or reversible editor strategy.");
    }

    private void normalizeConditionalValueHolderMembers(final List<Item> items, final JsonNode types) {
        boolean valueHolderAdvertised = false;
        for (final JsonNode type : types) {
            if ("rich__demo_ValueHolder".equals(type.path("name").asText())) {
                valueHolderAdvertised = true;
                break;
            }
        }
        if (!valueHolderAdvertised) {
            return;
        }

        final Set<String> conditionalIds = Set.of(
                "rich__demo_ValueHolder__blob__gqlv_property",
                "rich__demo_ValueHolder__count__gqlv_property",
                "rich__demo_ValueHolder__incrementRedirectEvenIfSame__gqlv_action",
                "rich__demo_ValueHolder__incrementRedirectOnlyIfDiffers__gqlv_action");
        items.removeIf(item -> conditionalIds.contains(item.id()));
        final String reason = "The effective metamodel conditionally advertises this abstract ValueHolder member; "
                + "the reviewed inventory retains it explicitly without claiming deterministic runtime support.";
        items.add(new Item(
                "rich__demo_ValueHolder__blob__gqlv_property",
                "PROPERTY",
                "BlobValue",
                Classification.NOT_EXERCISED,
                reason));
        items.add(new Item(
                "rich__demo_ValueHolder__count__gqlv_property",
                "PROPERTY",
                "Int",
                Classification.NOT_EXERCISED,
                reason));
        items.add(new Item(
                "rich__demo_ValueHolder__incrementRedirectEvenIfSame__gqlv_action",
                "ACTION",
                null,
                Classification.NOT_EXERCISED,
                reason));
        items.add(new Item(
                "rich__demo_ValueHolder__incrementRedirectOnlyIfDiffers__gqlv_action",
                "ACTION",
                null,
                Classification.NOT_EXERCISED,
                reason));
    }

    private void addValueShapeItems(final List<Item> items, final JsonNode types) {
        for (final JsonNode type : types) {
            if (!"SCALAR".equals(type.path("kind").asText())) {
                continue;
            }
            final String name = type.path("name").asText("");
            if (SUPPORTED_SCALARS.contains(name)) {
                items.add(new Item("value-shape:" + name, "VALUE_SHAPE", name, Classification.SUPPORTED,
                        "The scalar has an established semantic value contract."));
            } else if (EXACT_NUMERIC_TYPES.contains(name) || UNSUPPORTED_TEMPORAL_TYPES.contains(name)) {
                items.add(new Item("value-shape:" + name, "VALUE_SHAPE", name, Classification.VIEWER_DEFECT,
                        "The scalar is public but the current standard editor is not reversibly correct."));
            } else if (RESOURCE_TYPES.contains(name)) {
                items.add(new Item("value-shape:" + name, "VALUE_SHAPE", name, Classification.GRACEFUL_UNSUPPORTED,
                        "Resource output is bounded while resource input remains explicitly unsupported."));
            } else if ("UnsupportedValue".equals(name)) {
                items.add(new Item("value-shape:" + name, "VALUE_SHAPE", name, Classification.GRACEFUL_UNSUPPORTED,
                        "The schema explicitly marks values without a public reversible strategy."));
            }
        }
    }

    private static boolean isReferenceDomainType(final String name) {
        return name.startsWith("rich__demo_") || name.startsWith("rich__demoapp_");
    }

    private static String fieldNamedType(final JsonNode type, final String fieldName) {
        for (final JsonNode field : type.path("fields")) {
            if (fieldName.equals(field.path("name").asText())) {
                JsonNode ref = field.path("type");
                boolean list = false;
                while (!ref.isMissingNode() && !ref.isNull()) {
                    if ("LIST".equals(ref.path("kind").asText())) {
                        list = true;
                    }
                    if (!ref.path("name").isNull() && !ref.path("name").asText().isBlank()) {
                        return list ? "[" + ref.path("name").asText() + "]" : ref.path("name").asText();
                    }
                    ref = ref.path("ofType");
                }
            }
        }
        return null;
    }

    private static String sha256(final String value) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            final StringBuilder result = new StringBuilder(digest.length * 2);
            for (final byte element : digest) {
                result.append("%02x".formatted(element & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public enum Classification {
        SUPPORTED,
        GRACEFUL_UNSUPPORTED,
        GRAPHQL_GAP,
        VIEWER_DEFECT,
        VIEWER_SPECIFIC,
        NOT_EXERCISED
    }

    private record Item(String id, String kind, String valueType, Classification classification, String reason) {
    }
}
