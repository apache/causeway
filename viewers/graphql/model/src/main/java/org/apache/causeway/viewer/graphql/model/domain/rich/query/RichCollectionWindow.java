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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.exceptions.InvalidCollectionWindowException;

public class RichCollectionWindow extends Element {

    private static final String ORDERING_TYPE_NAME = "rich__gqlv_collection_window_ordering";

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
                .build());
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment environment) {
        var offset = environment.<Integer>getArgument("offset");
        var size = environment.<Integer>getArgument("size");
        validate(offset, size);

        var association = memberInteractor.getObjectMember();
        var managedObject = RichCollectionAccess.visibleSource(environment, association, context);
        if (managedObject == null) {
            return null;
        }

        var resultManagedObject = association.get(managedObject);
        var rows = materialize(resultManagedObject != null ? resultManagedObject.getPojo() : null);
        var comparator = association.getElementComparator();
        if (comparator.isPresent()) {
            var elementType = association.getElementType();
            var elementComparator = comparator.get();
            rows.sort((left, right) -> elementComparator.compare(
                    ManagedObject.adaptSingular(elementType, left),
                    ManagedObject.adaptSingular(elementType, right)));
        }

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
        result.put("ordering", comparator.isPresent()
                ? CollectionWindowOrdering.CONFIGURED
                : CollectionWindowOrdering.ENCOUNTER);
        return Map.copyOf(result);
    }

    private void validate(final Integer offset, final Integer size) {
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
    }

    private static List<Object> materialize(final Object collection) {
        if (collection == null) {
            return new ArrayList<>();
        }
        if (!(collection instanceof Iterable<?> iterable)) {
            throw new IllegalStateException("Collection association did not return an iterable value.");
        }
        var rows = new ArrayList<>();
        iterable.forEach(rows::add);
        return rows;
    }

    private static GraphQLEnumType orderingType(final Context context) {
        return context.graphQLTypeRegistry.lookup(ORDERING_TYPE_NAME, GraphQLEnumType.class)
                .orElseGet(() -> {
                    var orderingType = newEnum()
                            .name(ORDERING_TYPE_NAME)
                            .description("How rows were ordered before selecting a collection window.")
                            .value(CollectionWindowOrdering.CONFIGURED.name(), CollectionWindowOrdering.CONFIGURED,
                                    "A Causeway configured comparator was applied.")
                            .value(CollectionWindowOrdering.ENCOUNTER.name(), CollectionWindowOrdering.ENCOUNTER,
                                    "The materialized collection encounter order was retained without a cross-request stability guarantee.")
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(orderingType);
                    return orderingType;
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
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
                    return type;
                });
    }
}
