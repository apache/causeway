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
import java.util.stream.IntStream;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.object.MmSortUtils;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.exceptions.InvalidCollectionWindowException;

public class RichCollectionWindow extends Element {

    private static final String ORDERING_TYPE_NAME = "rich__gqlv_collection_window_ordering";
    private static final String SORT_DIRECTION_TYPE_NAME = "rich__gqlv_collection_window_sort_direction";
    private static final int MAX_SEARCH_LENGTH = 256;
    private static final int MAX_SORT_MEMBER_LENGTH = 128;

    private final MemberInteractor<OneToManyAssociation> memberInteractor;
    private final int defaultWindowSize;
    private final int maxWindowSize;

    public RichCollectionWindow(
            final MemberInteractor<OneToManyAssociation> memberInteractor,
            final Context context) {
        super(context);
        this.memberInteractor = memberInteractor;
        this.defaultWindowSize = graphqlConfiguration.collections().defaultWindowSize();
        this.maxWindowSize = graphqlConfiguration.collections().maxWindowSize();

        var association = memberInteractor.getObjectMember();
        var rowsType = context.typeMapper.listTypeForElementTypeOf(association, memberInteractor.getSchemaType());
        if (rowsType == null) {
            setField(null);
            return;
        }

        var orderingType = orderingType(context);
        var sortDirectionType = sortDirectionType(context);
        var windowType = windowType(context, memberInteractor, rowsType, orderingType);
        setField(newFieldDefinition()
                .name("window")
                .description("Returns a bounded zero-based collection window; the configured maximum size is "
                        + maxWindowSize + ".")
                .type(windowType)
                .argument(GraphQLArgument.newArgument()
                        .name("offset")
                        .description("Zero-based row offset.")
                        .type(nonNull(Scalars.GraphQLInt))
                        .defaultValueProgrammatic(0))
                .argument(GraphQLArgument.newArgument()
                        .name("size")
                        .description("Positive row count, not exceeding the configured maximum of "
                                + maxWindowSize + ".")
                        .type(nonNull(Scalars.GraphQLInt))
                        .defaultValueProgrammatic(defaultWindowSize))
                .argument(GraphQLArgument.newArgument()
                        .name("sortBy")
                        .description("Optional accepted Causeway table-column member id.")
                        .type(Scalars.GraphQLString))
                .argument(GraphQLArgument.newArgument()
                        .name("sortDirection")
                        .description("Direction for an optional sort member.")
                        .type(nonNull(sortDirectionType))
                        .defaultValueProgrammatic(MmSortUtils.SortDirection.ASCENDING))
                .argument(GraphQLArgument.newArgument()
                        .name("search")
                        .description("Optional CollectionFilterService quick-search text, bounded to "
                                + MAX_SEARCH_LENGTH + " characters.")
                        .type(Scalars.GraphQLString))
                .build());
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment environment) {
        var offset = environment.<Integer>getArgument("offset");
        var size = environment.<Integer>getArgument("size");
        var sortBy = normalize(environment.<String>getArgument("sortBy"));
        var sortDirection = environment.<MmSortUtils.SortDirection>getArgument("sortDirection");
        var search = normalize(environment.<String>getArgument("search"));
        validate(offset, size, sortBy, sortDirection, search);

        var association = memberInteractor.getObjectMember();
        var managedObject = RichCollectionAccess.visibleSource(environment, association, context);
        if (managedObject == null) {
            return null;
        }

        var table = ManagedCollection.of(managedObject, association, Where.ANYWHERE).createDataTableModel();
        var columns = table.dataColumnsObservable().getValue();
        var sortableMembers = columns.stream()
                .filter(column -> column.associationMetaModel().getSpecialization().leftIfAny() != null)
                .map(column -> column.columnId())
                .toList();
        if (sortBy != null) {
            var columnIndex = IntStream.range(0, columns.size())
                    .filter(index -> sortBy.equals(columns.getElseFail(index).columnId()))
                    .findFirst()
                    .orElseThrow(() -> invalid("Collection window sort member is not an accepted table column."));
            if (!sortableMembers.contains(sortBy)) {
                throw invalid("Collection window sort member is not sortable.");
            }
            table.columnSortBindable().setValue(new DataTableInteractive.ColumnSort(columnIndex, sortDirection));
        }
        if (search != null) {
            if (!table.isSearchSupported()) {
                throw invalid("Collection window search is not supported for this element type.");
            }
            table.searchArgumentBindable().setValue(search);
        }
        var rows = table.dataRowsFilteredAndSortedObservable().getValue().stream()
                .map(row -> row.rowElement().getPojo())
                .toList();

        var totalCount = rows.size();
        var fromIndex = Math.min(offset, totalCount);
        var toIndex = (int) Math.min(totalCount, (long) offset + size);
        var selectedRows = List.copyOf(rows.subList(fromIndex, toIndex));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", selectedRows);
        result.put("offset", offset);
        result.put("requestedSize", size);
        result.put("returnedCount", selectedRows.size());
        result.put("totalCount", totalCount);
        result.put("maximumSize", maxWindowSize);
        result.put("hasPrevious", offset > 0 && totalCount > 0);
        result.put("hasNext", (long) offset + selectedRows.size() < totalCount);
        result.put("ordering", sortBy != null
                ? CollectionWindowOrdering.REQUESTED
                : association.getElementComparator().isPresent()
                        ? CollectionWindowOrdering.CONFIGURED
                        : CollectionWindowOrdering.ENCOUNTER);
        result.put("sortableMembers", sortableMembers);
        result.put("searchSupported", table.isSearchSupported());
        if (table.isSearchSupported() && !table.getSearchPromptPlaceholderText().isBlank()) {
            result.put("searchPrompt", table.getSearchPromptPlaceholderText());
        }
        return Map.copyOf(result);
    }

    private void validate(
            final Integer offset,
            final Integer size,
            final String sortBy,
            final MmSortUtils.SortDirection sortDirection,
            final String search) {
        if (offset == null || offset < 0) {
            throw new InvalidCollectionWindowException(
                    "Collection window offset must be zero or greater.");
        }
        if (size == null || size < 1) {
            throw new InvalidCollectionWindowException(
                    "Collection window size must be greater than zero.");
        }
        if (size > maxWindowSize) {
            throw new InvalidCollectionWindowException(
                    "Collection window size exceeds the configured maximum of " + maxWindowSize + ".");
        }
        if (sortBy != null && sortBy.length() > MAX_SORT_MEMBER_LENGTH) {
            throw invalid("Collection window sort member exceeds the supported length.");
        }
        if (sortBy != null && sortDirection == null) {
            throw invalid("Collection window sort direction is required with a sort member.");
        }
        if (search != null && search.length() > MAX_SEARCH_LENGTH) {
            throw invalid("Collection window search exceeds the supported length of " + MAX_SEARCH_LENGTH + ".");
        }
    }

    private static String normalize(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static InvalidCollectionWindowException invalid(final String message) {
        return new InvalidCollectionWindowException(message);
    }

    private static GraphQLEnumType orderingType(final Context context) {
        return context.graphQLTypeRegistry.lookup(ORDERING_TYPE_NAME, GraphQLEnumType.class)
                .orElseGet(() -> {
                    var orderingType = newEnum()
                            .name(ORDERING_TYPE_NAME)
                            .description("How rows were ordered before selecting a collection window.")
                            .value(CollectionWindowOrdering.CONFIGURED.name(), CollectionWindowOrdering.CONFIGURED,
                                    "A Causeway configured comparator was applied.")
                            .value(CollectionWindowOrdering.REQUESTED.name(), CollectionWindowOrdering.REQUESTED,
                                    "A requested accepted Causeway table-column sort was applied.")
                            .value(CollectionWindowOrdering.ENCOUNTER.name(), CollectionWindowOrdering.ENCOUNTER,
                                    "The materialized collection encounter order was retained without a cross-request stability guarantee.")
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(orderingType);
                    return orderingType;
                });
    }

    private static GraphQLEnumType sortDirectionType(final Context context) {
        return context.graphQLTypeRegistry.lookup(SORT_DIRECTION_TYPE_NAME, GraphQLEnumType.class)
                .orElseGet(() -> {
                    var directionType = newEnum()
                            .name(SORT_DIRECTION_TYPE_NAME)
                            .description("Direction for a requested collection-window sort.")
                            .value(MmSortUtils.SortDirection.ASCENDING.name(), MmSortUtils.SortDirection.ASCENDING)
                            .value(MmSortUtils.SortDirection.DESCENDING.name(), MmSortUtils.SortDirection.DESCENDING)
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(directionType);
                    return directionType;
                });
    }

    private static GraphQLObjectType windowType(
            final Context context,
            final MemberInteractor<OneToManyAssociation> memberInteractor,
            final GraphQLOutputType rowsType,
            final GraphQLEnumType orderingType) {
        var association = memberInteractor.getObjectMember();
        var typeName = TypeNames.collectionWindowTypeNameFor(
                memberInteractor.getObjectSpecification(),
                association,
                memberInteractor.getSchemaType());
        return context.graphQLTypeRegistry.lookup(typeName, GraphQLObjectType.class)
                .orElseGet(() -> {
                    var type = newObject()
                            .name(typeName)
                            .description("A bounded execution-time view of a Causeway collection association.")
                            .field(newFieldDefinition().name("rows").type(rowsType))
                            .field(newFieldDefinition().name("offset").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("requestedSize").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("returnedCount").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("totalCount").type(Scalars.GraphQLInt))
                            .field(newFieldDefinition().name("maximumSize").type(nonNull(Scalars.GraphQLInt)))
                            .field(newFieldDefinition().name("hasPrevious").type(nonNull(Scalars.GraphQLBoolean)))
                            .field(newFieldDefinition().name("hasNext").type(nonNull(Scalars.GraphQLBoolean)))
                            .field(newFieldDefinition().name("ordering").type(nonNull(orderingType)))
                            .field(newFieldDefinition().name("sortableMembers")
                                    .type(nonNull(list(nonNull(Scalars.GraphQLString)))))
                            .field(newFieldDefinition().name("searchSupported")
                                    .type(nonNull(Scalars.GraphQLBoolean)))
                            .field(newFieldDefinition().name("searchPrompt").type(Scalars.GraphQLString))
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
                    return type;
                });
    }
}
