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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.exceptions.InvalidAutoCompleteWindowException;

abstract class RichAutoCompleteWindow extends Element {

    static final String SEARCH_ARGUMENT = "search";
    static final String OFFSET_ARGUMENT = "offset";
    static final String SIZE_ARGUMENT = "size";

    private static final String ORDERING_TYPE_NAME = "rich__gqlv_autocomplete_window_ordering";

    private final int defaultWindowSize;
    private final int maxWindowSize;

    RichAutoCompleteWindow(
            final Context context,
            final String resultTypeName,
            final GraphQLOutputType itemType,
            final Consumer<GraphQLFieldDefinition.Builder> dependentArguments) {
        super(context);
        this.defaultWindowSize = graphqlConfiguration.autocomplete().defaultWindowSize();
        this.maxWindowSize = graphqlConfiguration.autocomplete().maxWindowSize();

        var resultType = resultType(context, resultTypeName, itemType, orderingType(context));
        var fieldBuilder = newFieldDefinition()
                .name("autoCompleteWindow")
                .description("Returns a bounded response window over one authoritative autocomplete execution; "
                        + "the configured maximum size is " + maxWindowSize + ".")
                .type(resultType);
        dependentArguments.accept(fieldBuilder);
        fieldBuilder
                .argument(GraphQLArgument.newArgument()
                        .name(SEARCH_ARGUMENT)
                        .type(nonNull(context.typeMapper.outputTypeFor(String.class))))
                .argument(GraphQLArgument.newArgument()
                        .name(OFFSET_ARGUMENT)
                        .type(nonNull(Scalars.GraphQLInt))
                        .defaultValueProgrammatic(0))
                .argument(GraphQLArgument.newArgument()
                        .name(SIZE_ARGUMENT)
                        .type(nonNull(Scalars.GraphQLInt))
                        .defaultValueProgrammatic(defaultWindowSize));
        setField(fieldBuilder.build());
    }

    @Override
    protected final Object fetchData(final DataFetchingEnvironment environment) {
        var offset = environment.<Integer>getArgument(OFFSET_ARGUMENT);
        var size = environment.<Integer>getArgument(SIZE_ARGUMENT);
        validate(offset, size);

        var items = autocompleteItems(environment);
        var safeItems = items == null ? List.of() : List.copyOf(items);
        var totalCount = safeItems.size();
        var fromIndex = Math.min(offset, totalCount);
        var toIndex = (int) Math.min(totalCount, (long) offset + size);
        var selectedItems = List.copyOf(safeItems.subList(fromIndex, toIndex));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", selectedItems);
        result.put("offset", offset);
        result.put("requestedSize", size);
        result.put("returnedCount", selectedItems.size());
        result.put("totalCount", totalCount);
        result.put("maximumSize", maxWindowSize);
        result.put("hasPrevious", offset > 0 && totalCount > 0);
        result.put("hasNext", (long) offset + selectedItems.size() < totalCount);
        result.put("ordering", AutoCompleteWindowOrdering.APPLICATION);
        return Map.copyOf(result);
    }

    protected abstract List<Object> autocompleteItems(DataFetchingEnvironment environment);

    private void validate(final Integer offset, final Integer size) {
        if (offset == null || offset < 0) {
            throw new InvalidAutoCompleteWindowException(
                    "Autocomplete window offset must be zero or greater.");
        }
        if (size == null || size < 1) {
            throw new InvalidAutoCompleteWindowException(
                    "Autocomplete window size must be greater than zero.");
        }
        if (size > maxWindowSize) {
            throw new InvalidAutoCompleteWindowException(
                    "Autocomplete window size exceeds the configured maximum of " + maxWindowSize + ".");
        }
    }

    private static GraphQLEnumType orderingType(final Context context) {
        return context.graphQLTypeRegistry.lookup(ORDERING_TYPE_NAME, GraphQLEnumType.class)
                .orElseGet(() -> {
                    var type = newEnum()
                            .name(ORDERING_TYPE_NAME)
                            .description("How autocomplete items were ordered before selecting a response window.")
                            .value(AutoCompleteWindowOrdering.APPLICATION.name(), AutoCompleteWindowOrdering.APPLICATION,
                                    "The authoritative application autocomplete encounter order for this execution.")
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
                    return type;
                });
    }

    private static GraphQLObjectType resultType(
            final Context context,
            final String resultTypeName,
            final GraphQLOutputType itemType,
            final GraphQLEnumType orderingType) {
        return context.graphQLTypeRegistry.lookup(resultTypeName, GraphQLObjectType.class)
                .orElseGet(() -> {
                    var type = newObject()
                            .name(resultTypeName)
                            .description("A bounded response window over one Causeway autocomplete execution.")
                            .field(newFieldDefinition().name("items").type(list(itemType)))
                            .field(newFieldDefinition().name("offset").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("requestedSize").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("returnedCount").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("totalCount").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("maximumSize").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("hasPrevious").type(nonNull(Scalars.GraphQLBoolean)))
                            .field(newFieldDefinition().name("hasNext").type(nonNull(Scalars.GraphQLBoolean)))
                            .field(newFieldDefinition().name("ordering").type(nonNull(orderingType)))
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
                    return type;
                });
    }
}
