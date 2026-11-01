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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.model.types;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.applib.value.OpenUrlStrategy;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.graphql.model.context.Context;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import lombok.experimental.UtilityClass;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;

@UtilityClass
public class ResourceValueTypes {

    public static final String BLOB_INPUT_TYPE = "BlobInput";
    public static final String CLOB_INPUT_TYPE = "ClobInput";
    public static final String BLOB_OUTPUT_TYPE = "BlobValue";
    public static final String CLOB_OUTPUT_TYPE = "ClobValue";
    public static final String LOCAL_RESOURCE_PATH_INPUT_TYPE = "LocalResourcePathInput";
    public static final String LOCAL_RESOURCE_PATH_OUTPUT_TYPE = "LocalResourcePathValue";
    public static final String OPEN_URL_STRATEGY_TYPE = "OpenUrlStrategy";

    public static boolean isResourceType(final Class<?> javaType) {
        return javaType == Blob.class || javaType == Clob.class;
    }

    public static boolean isLocalResourcePathType(final Class<?> javaType) {
        return javaType == LocalResourcePath.class;
    }

    public static GraphQLInputType inputTypeFor(final Class<?> javaType, final Context context) {
        if (!valueContentEnabled(context)) {
            return GraphQLValueScalars.UNSUPPORTED_INPUT;
        }
        return javaType == Blob.class
                ? blobInputType(context)
                : clobInputType(context);
    }

    public static GraphQLOutputType outputTypeFor(final Class<?> javaType, final Context context) {
        return javaType == Blob.class
                ? blobOutputType(context)
                : clobOutputType(context);
    }

    public static GraphQLInputType localResourcePathInputType(final Context context) {
        var existing = context.graphQLTypeRegistry.lookup(LOCAL_RESOURCE_PATH_INPUT_TYPE, GraphQLInputObjectType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        var type = GraphQLInputObjectType.newInputObject()
                .name(LOCAL_RESOURCE_PATH_INPUT_TYPE)
                .description("A Causeway local resource path and browser opening strategy.")
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("path")
                        .type(nonNull(Scalars.GraphQLString)))
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("openUrlStrategy")
                        .type(nonNull(openUrlStrategyType(context))))
                .build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        return type;
    }

    public static GraphQLOutputType localResourcePathOutputType(final Context context) {
        var existing = context.graphQLTypeRegistry.lookup(LOCAL_RESOURCE_PATH_OUTPUT_TYPE, GraphQLObjectType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        var type = GraphQLObjectType.newObject()
                .name(LOCAL_RESOURCE_PATH_OUTPUT_TYPE)
                .description("A Causeway local resource path and browser opening strategy.")
                .field(newFieldDefinition()
                        .name("path")
                        .type(nonNull(Scalars.GraphQLString)))
                .field(newFieldDefinition()
                        .name("openUrlStrategy")
                        .type(nonNull(openUrlStrategyType(context))))
                .build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        register(context, type.getName(), "path", source -> ((LocalResourcePath) source).path());
        register(context, type.getName(), "openUrlStrategy",
                source -> ((LocalResourcePath) source).openUrlStrategy().name());
        return type;
    }

    public static LocalResourcePath unmarshalLocalResourcePath(final Object graphValue) {
        if (!(graphValue instanceof Map<?, ?> input)) {
            throw new IllegalArgumentException("Expected structured LocalResourcePath input");
        }
        var path = requiredString(input, "path", 2083);
        var strategyName = requiredString(input, "openUrlStrategy", 64);
        final OpenUrlStrategy strategy;
        try {
            strategy = OpenUrlStrategy.valueOf(strategyName);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid LocalResourcePath open strategy");
        }
        return new LocalResourcePath(path, strategy);
    }

    public static Object unmarshal(final Class<?> javaType, final Object graphValue, final Context context) {
        if (!valueContentEnabled(context)) {
            throw new IllegalArgumentException("Resource input is forbidden by GraphQL value-content policy");
        }
        if (!(graphValue instanceof Map<?, ?> input)) {
            throw new IllegalArgumentException("Expected structured resource input");
        }
        var name = requiredString(input, "name", 255);
        var mimeType = requiredString(input, "mimeType", 255);
        requireValidMimeType(mimeType);
        var maxBytes = resources(context).inlineInputMaxBytes();

        if (javaType == Blob.class) {
            var base64 = requiredContent(input, "base64");
            if (base64.length() > encodedLengthLimit(maxBytes)) {
                throw new IllegalArgumentException("Resource input exceeds the configured inline byte limit");
            }
            final byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Blob input contains invalid base64 content");
            }
            requireWithinLimit(bytes.length, maxBytes);
            return new Blob(name, mimeType, bytes);
        }

        var chars = requiredContent(input, "chars");
        if (chars.length() > maxBytes) {
            throw new IllegalArgumentException("Resource input exceeds the configured inline byte limit");
        }
        var bytes = chars.getBytes(StandardCharsets.UTF_8);
        requireWithinLimit(bytes.length, maxBytes);
        return new Clob(name, mimeType, chars);
    }

    public static void validateFileAccept(final ObjectFeature feature, final Object resourceValue) {
        if (!(resourceValue instanceof Blob) && !(resourceValue instanceof Clob)) {
            return;
        }
        var accepted = fileAccept(feature);
        if (accepted.isEmpty()) {
            return;
        }
        var name = resourceValue instanceof Blob blob ? blob.name() : ((Clob) resourceValue).name();
        var mimeType = resourceValue instanceof Blob blob
                ? blob.mimeType().getBaseType()
                : ((Clob) resourceValue).mimeType().getBaseType();
        if (!matchesFileAccept(accepted.get(), name, mimeType)) {
            throw new IllegalArgumentException(
                    "Resource media type is not accepted for member '" + feature.getFeatureIdentifier() + "'");
        }
    }

    public static Optional<String> fileAccept(final ObjectFeature feature) {
        return Facets.fileAccept(feature)
                .filter(value -> !value.isBlank());
    }

    public static int byteLength(final Blob blob) {
        return blob.bytes().length;
    }

    public static int byteLength(final Clob clob) {
        return clob.chars().toString().getBytes(StandardCharsets.UTF_8).length;
    }

    public static String transferMode(final int byteLength, final Context context) {
        return valueContentEnabled(context) && byteLength <= resources(context).inlineOutputMaxBytes()
                ? "INLINE"
                : "METADATA_ONLY";
    }

    public static String inputMode(final Context context) {
        return valueContentEnabled(context) ? "STRUCTURED_INLINE" : "FORBIDDEN";
    }

    private static GraphQLEnumType openUrlStrategyType(final Context context) {
        var existing = context.graphQLTypeRegistry.lookup(OPEN_URL_STRATEGY_TYPE, GraphQLEnumType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        var builder = GraphQLEnumType.newEnum()
                .name(OPEN_URL_STRATEGY_TYPE)
                .description("Browser opening strategy for a Causeway local resource path.");
        for (var strategy : OpenUrlStrategy.values()) {
            builder.value(strategy.name());
        }
        var type = builder.build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        return type;
    }

    private static GraphQLInputObjectType blobInputType(final Context context) {
        return inputType(context, BLOB_INPUT_TYPE, "Bounded inline Causeway Blob input.", "base64");
    }

    private static GraphQLInputObjectType clobInputType(final Context context) {
        return inputType(context, CLOB_INPUT_TYPE, "Bounded inline Causeway Clob input.", "chars");
    }

    private static GraphQLInputObjectType inputType(
            final Context context,
            final String typeName,
            final String description,
            final String contentField) {
        var existing = context.graphQLTypeRegistry.lookup(typeName, GraphQLInputObjectType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        var type = GraphQLInputObjectType.newInputObject()
                .name(typeName)
                .description(description)
                .field(inputField("name", "Resource filename."))
                .field(inputField("mimeType", "IANA media type accepted by the target member."))
                .field(inputField(contentField, contentField.equals("base64")
                        ? "RFC 4648 base64 content, bounded after decoding."
                        : "UTF-8 character content, bounded by encoded byte length."))
                .build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        return type;
    }

    private static GraphQLInputObjectField inputField(final String name, final String description) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(name)
                .description(description)
                .type(nonNull(Scalars.GraphQLString))
                .build();
    }

    private static GraphQLObjectType blobOutputType(final Context context) {
        var existing = context.graphQLTypeRegistry.lookup(BLOB_OUTPUT_TYPE, GraphQLObjectType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        var builder = commonOutputBuilder(BLOB_OUTPUT_TYPE, "Metadata-first bounded Causeway Blob output.");
        if (valueContentEnabled(context)) {
            builder.field(newFieldDefinition()
                    .name("base64")
                    .description("RFC 4648 base64 content; null when the configured inline-output limit is exceeded.")
                    .type(Scalars.GraphQLString));
        }
        var type = builder.build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        registerCommonOutputFetchers(context, type.getName());
        register(context, type.getName(), "name", source -> ((Blob) source).name());
        register(context, type.getName(), "mimeType", source -> ((Blob) source).mimeType().getBaseType());
        register(context, type.getName(), "byteLength", source -> byteLength((Blob) source));
        if (valueContentEnabled(context)) {
            register(context, type.getName(), "base64", source -> {
                var blob = (Blob) source;
                return byteLength(blob) <= resources(context).inlineOutputMaxBytes()
                        ? Base64.getEncoder().encodeToString(blob.bytes())
                        : null;
            });
        }
        return type;
    }

    private static GraphQLObjectType clobOutputType(final Context context) {
        var existing = context.graphQLTypeRegistry.lookup(CLOB_OUTPUT_TYPE, GraphQLObjectType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        var builder = commonOutputBuilder(CLOB_OUTPUT_TYPE, "Metadata-first bounded Causeway Clob output.")
                .field(newFieldDefinition().name("characterLength").type(nonNull(Scalars.GraphQLInt)));
        if (valueContentEnabled(context)) {
            builder.field(newFieldDefinition()
                    .name("chars")
                    .description("Character content; null when the configured UTF-8 inline-output limit is exceeded.")
                    .type(Scalars.GraphQLString));
        }
        var type = builder.build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        registerCommonOutputFetchers(context, type.getName());
        register(context, type.getName(), "name", source -> ((Clob) source).name());
        register(context, type.getName(), "mimeType", source -> ((Clob) source).mimeType().getBaseType());
        register(context, type.getName(), "byteLength", source -> byteLength((Clob) source));
        register(context, type.getName(), "characterLength", source -> ((Clob) source).chars().length());
        if (valueContentEnabled(context)) {
            register(context, type.getName(), "chars", source -> {
                var clob = (Clob) source;
                return byteLength(clob) <= resources(context).inlineOutputMaxBytes()
                        ? clob.chars().toString()
                        : null;
            });
        }
        return type;
    }

    private static GraphQLObjectType.Builder commonOutputBuilder(
            final String name,
            final String description) {
        return GraphQLObjectType.newObject()
                .name(name)
                .description(description)
                .field(newFieldDefinition().name("name").type(nonNull(Scalars.GraphQLString)))
                .field(newFieldDefinition().name("mimeType").type(nonNull(Scalars.GraphQLString)))
                .field(newFieldDefinition().name("byteLength").type(nonNull(Scalars.GraphQLInt)))
                .field(newFieldDefinition().name("transferMode").type(nonNull(Scalars.GraphQLString)))
                .field(newFieldDefinition().name("inlineOutputMaxBytes").type(nonNull(Scalars.GraphQLInt)));
    }

    private static void registerCommonOutputFetchers(final Context context, final String typeName) {
        register(context, typeName, "transferMode", source -> source instanceof Blob blob
                ? transferMode(byteLength(blob), context)
                : transferMode(byteLength((Clob) source), context));
        register(context, typeName, "inlineOutputMaxBytes", source -> resources(context).inlineOutputMaxBytes());
    }

    private static void register(
            final Context context,
            final String typeName,
            final String fieldName,
            final FunctionWithSource function) {
        context.codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(typeName, fieldName),
                (DataFetcher<Object>) environment -> function.apply(environment.getSource()));
    }

    private static String requiredString(
            final Map<?, ?> input,
            final String field,
            final int maxCharacters) {
        var value = input.get(field);
        if (!(value instanceof String text)
                || text.isBlank()
                || text.length() > maxCharacters) {
            throw new IllegalArgumentException("Resource input field '" + field + "' is missing or invalid");
        }
        return text;
    }

    private static String requiredContent(final Map<?, ?> input, final String field) {
        var value = input.get(field);
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Resource input field '" + field + "' is missing or invalid");
        }
        return text;
    }

    private static int encodedLengthLimit(final int maxBytes) {
        var encodedLimit = ((long) maxBytes + 2L) / 3L * 4L + 4L;
        return (int) Math.min(Integer.MAX_VALUE, encodedLimit);
    }

    private static void requireWithinLimit(final int actualBytes, final int maxBytes) {
        if (actualBytes > maxBytes) {
            throw new IllegalArgumentException("Resource input exceeds the configured inline byte limit");
        }
    }

    private static void requireValidMimeType(final String mimeType) {
        if (!mimeType.matches("[A-Za-z0-9!#$&^_.+\\-]+/[A-Za-z0-9!#$&^_.+\\-]+")) {
            throw new IllegalArgumentException("Resource input media type is invalid");
        }
    }

    private static boolean matchesFileAccept(
            final String accepted,
            final String name,
            final String mimeType) {
        var lowerName = name.toLowerCase(Locale.ROOT);
        var lowerMimeType = mimeType.toLowerCase(Locale.ROOT);
        for (String candidate : accepted.toLowerCase(Locale.ROOT).split("[,|\\s]+")) {
            if (candidate.isBlank()) {
                continue;
            }
            if (candidate.startsWith(".") && lowerName.endsWith(candidate)) {
                return true;
            }
            if (candidate.endsWith("/*")
                    && lowerMimeType.startsWith(candidate.substring(0, candidate.length() - 1))) {
                return true;
            }
            if (candidate.equals(lowerMimeType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean valueContentEnabled(final Context context) {
        return resources(context).effectiveValueContentResponseType()
                != CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN;
    }

    private static CausewayConfiguration.Viewer.Graphql.Resources resources(final Context context) {
        return context.causewayConfiguration.viewer().graphql().resources();
    }

    @FunctionalInterface
    private interface FunctionWithSource {
        Object apply(Object source);
    }
}
