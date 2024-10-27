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

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import org.springframework.lang.Nullable;

import org.apache.causeway.viewer.graphql.model.context.Context;

import lombok.Getter;

/**
 * A custom type that has children.
 */
public abstract class ElementCustom
        extends Element
        implements Parent {

    private final String typeName;

    /**
     * If set, then {@link #getGqlObjectType()}  will not be set.
     *
     * <p>
     *     Represents the case when we are currently still in the process of building a custom
     *     {@link #getGqlObjectType()}.
     * </p>
     */
    protected final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    /**
     * If set, then the builder will not be set.
     *
     * <p>
     *     Represents the case when this custom type was previously created and has been found, so doesn't need to be
     *     re-built.
     * </p>
     */
    @Getter private GraphQLObjectType gqlObjectType;

    protected ElementCustom(
            final String typeName,
            final Context context) {
        super(context);
        this.typeName = typeName;

        var typeIfAny = context.graphQLTypeRegistry.lookup(typeName, GraphQLObjectType.class);
        if(typeIfAny.isPresent()) {
            this.gqlObjectType = typeIfAny.get();
            this.gqlObjectTypeBuilder = null;
        } else {
            this.gqlObjectTypeBuilder = newObject().name(typeName);
        }
    }

    public boolean isBuilt() {
        return gqlObjectType != null;
    }

    protected final <T extends Element> T addChildFieldFor(@Nullable T hasField) {
        if (isBuilt()) {
            // the type was built already
            return hasField;
        }
        if (hasField == null) {
            return hasField;
        }
        addChildField(hasField.getField());
        return hasField;
    }

    protected void addChildField(GraphQLFieldDefinition childField) {
        if (isBuilt()) {
            // the type was built already
            return;
        }
        if (childField != null) {
            gqlObjectTypeBuilder.field(childField);
        }
    }

    protected void buildObjectTypeAndField(
            final String fieldName,
            final String description) {
        if (!isBuilt()) {
            buildObjectType();
        }

        setField(newField(fieldName, description));
    }

    public GraphQLFieldDefinition newField(
            final String fieldName,
            final String description) {
        return newFieldDefinition()
                .name(fieldName)
                .description(description)
                .type(getGqlObjectType())
                .build();
    }

    protected final GraphQLObjectType buildObjectType() {
        if (!isBuilt()) {
            this.gqlObjectType = gqlObjectTypeBuilder.build();
            context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(this.gqlObjectType);
        }
        return this.gqlObjectType;
    }

    /**
     * Implemented by top-level queries only.
     */
    public void addDataFetchers() {
    }

    public final FieldCoordinates coordinatesFor(final GraphQLFieldDefinition field) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(
                    String.format("GQL Object Type for '%s' not yet built", typeName));
        }
        return FieldCoordinates.coordinates(gqlObjectType, field);
    }

    public final FieldCoordinates coordinatesFor(final String fieldName) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(
                    String.format("GQL Object Type for '%s' not yet built", typeName));
        }
        return FieldCoordinates.coordinates(gqlObjectType, fieldName);
    }

    protected boolean inApiScope(ObjectAction act) {
        if (graphqlConfiguration.getApiScope() == CausewayConfiguration.Viewer.Graphql.ApiScope.ALL) {
            return true;
        }
        var returnType = act.getElementType();
        return returnType.isViewModelOrValueOrVoid() &&
                act.getParameterTypes().stream().allMatch(ObjectSpecification::isViewModelOrValue);
    }

    protected boolean inApiScope(final ObjectAssociation objAssoc) {
        if (graphqlConfiguration.getApiScope() == CausewayConfiguration.Viewer.Graphql.ApiScope.ALL) {
            return true;
        }
        return objAssoc.getElementType().isViewModelOrValue();
    }

}
