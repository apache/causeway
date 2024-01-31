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

import java.util.LinkedHashMap;
import java.util.Map;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.context.Context;

import static org.apache.causeway.core.config.CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT;

import lombok.Getter;
import lombok.val;

/**
 * Exposes a domain service (view model or entity) via the GQL viewer.
 */
public class GqlvDomainService implements GqlvAction.Holder {

    @Getter private final ObjectSpecification objectSpecification;
    @Getter private final Object servicePojo;
    private final Context context;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    String getLogicalTypeName() {
        return objectSpecification.getLogicalTypeName();
    }

    private final Map<String, GqlvAction> actions = new LinkedHashMap<>();

    /**
     * Will be <code>null</code> if there are no actions.
     */
    private GraphQLObjectType gqlObjectType;

    public GqlvDomainService(
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final Context context) {
        this.objectSpecification = objectSpecification;
        this.servicePojo = servicePojo;
        this.context = context;

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.objectTypeNameFor(objectSpecification));

        addActions();

        if (hasActions()) {
            gqlObjectType = gqlObjectTypeBuilder.build();
            addDataFetchers();
        }
    }

    public boolean hasActions() {
        return !actions.isEmpty();
    }

    private void addActions() {

        objectSpecification.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                .forEach(this::addAction);
    }

    private void addAction(final ObjectAction objectAction) {
        actions.put(objectAction.getId(), new GqlvAction(this, objectAction, context));
    }


    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    void addDataFetchers() {
        actions.forEach((id, gqlva) -> gqlva.addDataFetcher());
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return coordinates(gqlObjectType, fieldDefinition);
    }

    public GraphQLFieldDefinition createTopLevelQueryField() {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return newFieldDefinition()
                .name(TypeNames.objectTypeNameFor(objectSpecification))
                .type(gqlObjectType)
                .build();
    }

    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}
