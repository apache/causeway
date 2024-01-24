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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import lombok.Getter;
import lombok.val;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

/**
 * Exposes a domain object (view model or entity) via the GQL viewer.
 */
public class GqlvDomainObject implements GqlvActionHolder, GqlvPropertyHolder, GqlvCollectionHolder, GqlvMutationsHolder, GqlvMetaHolder {

    @Getter private final ObjectSpecification objectSpecification;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final BookmarkService bookmarkService;

    private final GqlvMeta meta;
    private final GqlvMutations mutations;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    private final SortedMap<String, GqlvProperty> properties = new TreeMap<>();
    private final SortedMap<String, GqlvCollection> collections = new TreeMap<>();
    private final SortedMap<String, GqlvCollectionSimple> collectionSimples = new TreeMap<>();
    private final Map<String, GqlvAction> safeActions = new TreeMap<>();

    private GraphQLObjectType gqlObjectType;

    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    public GqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager) {
        this.objectSpecification = objectSpecification;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.bookmarkService = bookmarkService;

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.objectTypeNameFor(objectSpecification));

        this.meta = new GqlvMeta(this, codeRegistryBuilder, bookmarkService, objectManager);
        this.mutations = new GqlvMutations(this, codeRegistryBuilder, bookmarkService);


        // input object type
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(TypeNames.inputTypeNameFor(objectSpecification));
        inputTypeBuilder
                .field(newInputObjectField()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLID))
                        .build());
        gqlInputObjectType = inputTypeBuilder.build();
    }


    public void addMembers() {
        objectSpecification.streamProperties(MixedIn.INCLUDED).forEach(this::addProperty);
        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(this::addCollection);

        val anyActions = new AtomicBoolean(false);
        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction -> {
                    anyActions.set(true);
                    addAction(objectAction);
                });

        mutations.buildObjectTypeAndFieldIfRequired();

        anyActions.get();
    }

    private void addProperty(final OneToOneAssociation otoa) {
        GqlvProperty property = new GqlvProperty(this, otoa, codeRegistryBuilder, bookmarkService);
        if (property.hasFieldDefinition()) {
            String propertyId = property.getId();
            if (!properties.containsKey(propertyId)) {
                properties.put(propertyId, property);
            }
        }
    }

    private void addCollection(OneToManyAssociation otom) {
        GqlvCollection collection = new GqlvCollection(this, otom, codeRegistryBuilder, bookmarkService);
        if (collection.hasFieldDefinition()) {
            String collectionId = collection.getId();
            if (!collections.containsKey(collectionId)) {
                collections.put(collectionId, collection);
            }
        }
//        GqlvCollectionSimple collection = new GqlvCollectionSimple(this, otom, codeRegistryBuilder);
//        if (collection.hasFieldDefinition()) {
//            String collectionId = collection.getId();
//            if (!collectionSimples.containsKey(collectionId)) {
//                collectionSimples.put(collectionId, collection);
//            }
//        }
    }

    private void addAction(final ObjectAction objectAction) {
        if (objectAction.getSemantics().isSafeInNature()) {
            String actionId = objectAction.getId();
            if (!safeActions.containsKey(actionId)) {
                safeActions.put(actionId, new GqlvAction(this, objectAction, codeRegistryBuilder, bookmarkService));
            }
        } else {
            mutations.addAction(objectAction);
        }
    }


    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }


    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {

        gqlObjectType = gqlObjectTypeBuilder.name(TypeNames.objectTypeNameFor(objectSpecification)).build();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlObjectType);

        meta.registerTypesInto(graphQLTypeRegistry);

        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(getGqlInputObjectType());

        mutations.registerTypesInto(graphQLTypeRegistry);

    }

    public void addDataFetchers() {
        meta.addDataFetchers();
        properties.forEach((id, property) -> property.addDataFetcher());
        collections.forEach((id, collection) -> collection.addDataFetcher());
//        collectionSimples.forEach((id, collection) -> collection.addDataFetcher());
        safeActions.forEach((id, action) -> action.addDataFetcher());
        mutations.addDataFetchers();
    }


    @Override
    public FieldCoordinates coordinatesFor(final GraphQLFieldDefinition fieldDefinition) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", objectSpecification.getLogicalTypeName()));
        }
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }


    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}
