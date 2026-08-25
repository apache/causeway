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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import java.util.Objects;
import java.util.function.Supplier;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;

final class RichScalarMetadataField extends Element {

    private final Supplier<@Nullable Object> valueSupplier;

    RichScalarMetadataField(
            final Context context,
            final String name,
            final GraphQLOutputType outputType,
            final Supplier<@Nullable Object> valueSupplier) {
        this(context, name, null, outputType, valueSupplier);
    }

    RichScalarMetadataField(
            final Context context,
            final String name,
            final @Nullable String description,
            final GraphQLOutputType outputType,
            final Supplier<@Nullable Object> valueSupplier) {
        super(context);
        this.valueSupplier = Objects.requireNonNull(valueSupplier);
        setField(GraphQLFieldDefinition.newFieldDefinition()
                .name(name)
                .description(description)
                .type(outputType)
                .build());
    }

    static @Nullable Integer finiteMaxLength(final ObjectFeature feature) {
        return optionalIntOrNull(Facets.maxLengthExplicit(feature));
    }

    static @Nullable String pattern(final ObjectFeature feature) {
        return Facets.regularExpressionPattern(feature).orElse(null);
    }

    static @Nullable Integer patternFlags(final ObjectFeature feature) {
        return optionalIntOrNull(Facets.regularExpressionPatternFlags(feature));
    }

    static @Nullable Integer multiLine(final ObjectFeature feature) {
        return optionalIntOrNull(Facets.multilineNumberOfLinesExplicit(feature));
    }

    static @Nullable Integer typicalLength(final ObjectFeature feature) {
        return optionalIntOrNull(Facets.typicalLength(feature));
    }

    private static @Nullable Integer optionalIntOrNull(final java.util.OptionalInt value) {
        return value.isPresent() ? value.getAsInt() : null;
    }

    @Override
    protected @Nullable Object fetchData(final DataFetchingEnvironment environment) {
        return valueSupplier.get();
    }
}
