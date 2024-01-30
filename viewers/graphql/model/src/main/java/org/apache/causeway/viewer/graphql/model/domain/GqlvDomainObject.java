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

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;

import static org.apache.causeway.core.config.CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT;

import lombok.Getter;
import lombok.val;

/**
 * Exposes a domain object (view model or entity) via the GQL viewer.
 */
public class GqlvDomainObject implements GqlvAction.Holder, GqlvProperty.Holder, GqlvCollection.Holder, GqlvMeta.Holder {

    @Getter private final ObjectSpecification objectSpecification;
    private final Context context;

    private final GqlvMeta meta;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    private final SortedMap<String, GqlvProperty> properties = new TreeMap<>();
    private final SortedMap<String, GqlvCollection> collections = new TreeMap<>();
    private final Map<String, GqlvAction> actions = new TreeMap<>();

    private GraphQLObjectType gqlObjectType;

    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    public GqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final Context context,
            final ObjectManager objectManager,
            final GraphQLTypeRegistry graphQLTypeRegistry) {

        this.objectSpecification = objectSpecification;
        this.context = context;

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.objectTypeNameFor(objectSpecification));

        this.meta = new GqlvMeta(this, context, objectManager);

        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(TypeNames.inputTypeNameFor(objectSpecification));
        inputTypeBuilder
                .field(newInputObjectField()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLID))
                        .build());
        gqlInputObjectType = inputTypeBuilder.build();

        addMembers();
    }

    public void addTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {
        gqlObjectType = gqlObjectTypeBuilder.build();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlObjectType);
        meta.registerTypesInto(graphQLTypeRegistry);
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlInputObjectType);
    }


    private void addMembers() {

        objectSpecification.streamProperties(MixedIn.INCLUDED).forEach(this::addProperty);
        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(this::addCollection);

        val variant = context.causewayConfiguration.getViewer().getGraphql().getApiVariant();

        objectSpecification.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                .filter(x -> x.getSemantics().isSafeInNature() ||
                             variant == QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT)
                .forEach(objectAction -> {
                    actions.put(objectAction.getId(), new GqlvAction(this, objectAction, context));
                });
    }

    @SuppressWarnings("unused")
    private ActionScope determineActionScope() {
        return context.causewaySystemEnvironment.getDeploymentType().isProduction()
                ? ActionScope.PRODUCTION
                : ActionScope.PROTOTYPE;
    }

    private void addProperty(final OneToOneAssociation otoa) {
        properties.put(otoa.getId(), new GqlvProperty(this, otoa, context));
    }

    private void addCollection(OneToManyAssociation otom) {
        GqlvCollection collection = new GqlvCollection(this, otom, context);
        if (collection.hasFieldDefinition()) {
            collections.put(otom.getId(), collection);
        }
    }


    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }


    public void addDataFetchers() {
        meta.addDataFetchers();
        properties.forEach((id, property) -> property.addDataFetcher());
        collections.forEach((id, collection) -> collection.addDataFetcher());
        actions.forEach((id, action) -> action.addDataFetcher());
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
