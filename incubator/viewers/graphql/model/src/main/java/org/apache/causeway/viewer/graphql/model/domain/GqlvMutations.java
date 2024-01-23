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
package org.apache.causeway.viewer.graphql.model.domain;

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvMutations implements GqlvActionHolder {

    private final GqlvMutationsHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final BookmarkService bookmarkService;

    /**
     * Used to build {@link #objectTypeIfAny}.
     */
    final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    /**
     * Built lazily using {@link #buildObjectTypeAndFieldIfRequired()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLObjectType> objectTypeIfAny;

    /**
     * Built lazily using {@link #buildObjectTypeAndFieldIfRequired()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLFieldDefinition> fieldIfAny;

    private final SortedMap<String, GqlvAction> actions = new TreeMap<>();

    public GqlvMutations(
            final GqlvMutationsHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.bookmarkService = bookmarkService;

        gqlObjectTypeBuilder = newObject().name(TypeNames.mutationsTypeNameFor(this.holder.getObjectSpecification()));
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    public void addAction(final ObjectAction objectAction) {
        String actionId = objectAction.getId();
        if (!actions.containsKey(actionId)) {
            actions.put(actionId, new GqlvAction(this, objectAction, codeRegistryBuilder, bookmarkService));
        }
    }


    boolean hasActions() {
        return !actions.isEmpty();
    }


    public Optional<GraphQLObjectType> buildObjectTypeAndFieldIfRequired() {
        //noinspection OptionalAssignedToNull
        if (objectTypeIfAny != null) {
            throw new IllegalArgumentException("Gql mutations type and field has already been built for " + holder.getObjectSpecification().getLogicalTypeName());
        }

        if (hasActions()) {

            // create the type
            GraphQLObjectType mutationsType = gqlObjectTypeBuilder.build();
            this.objectTypeIfAny = Optional.of(mutationsType);

            // create the field
            GraphQLFieldDefinition mutationsField = newFieldDefinition()
                    .name("_gql_mutations")
                    .type(mutationsType)
                    .build();
            fieldIfAny = Optional.of(mutationsField);

            // register the field into the owning type
            holder.addField(mutationsField);

        } else {
            fieldIfAny = Optional.empty();
            objectTypeIfAny = Optional.empty();
        }
        return objectTypeIfAny;
    }

    public void addDataFetchers() {
        if (fieldIfAny.isPresent()) {
            codeRegistryBuilder.dataFetcher(
                    holder.coordinatesFor(fieldIfAny.get()),
                    (DataFetcher<Object>) environment ->
                        bookmarkService.bookmarkFor(environment.getSource())
                            .map(bookmark -> new BookmarkedPojo(bookmark, bookmarkService))
                            .orElseThrow());

            actions.forEach((id, gqlvAction) -> gqlvAction.addDataFetcher());
        }
    }

    @Override
    public void addField(GraphQLFieldDefinition fieldDefinition) {
        gqlObjectTypeBuilder.field(fieldDefinition);
    }

    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {
        objectTypeIfAny.ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(objectTypeIfAny.orElseThrow(), fieldDefinition);
    }


}
