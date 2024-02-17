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

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.springframework.lang.Nullable;

import org.apache.causeway.viewer.graphql.model.context.Context;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class GqlvAbstractCustom extends GqlvAbstract implements Parent {

    protected final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final String typeName;

    @Getter(AccessLevel.PROTECTED)
    private GraphQLObjectType gqlObjectType;

    protected GqlvAbstractCustom(
            final String typeName,
            final Context context) {
        super(context);
        this.typeName = typeName;
        Optional<GraphQLObjectType> typeIfAny =
                context.graphQLTypeRegistry.lookup(typeName, GraphQLObjectType.class);
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

    protected final void addChildFieldFor(@Nullable GqlvAbstract hasField) {
        if (isBuilt()) {
            // the type was built already
            return;
        }
        if (hasField == null) {
            return;
        }
        addChildField(hasField.getField());
    }

    void addChildField(GraphQLFieldDefinition childField) {
        if (isBuilt()) {
            // the type was built already
            return;
        }
        if (childField != null) {
            gqlObjectTypeBuilder.field(childField);
        }
    }

    protected void buildObjectTypeAndField(String fieldName) {
        if (!isBuilt()) {
            buildObjectType();
        }

        setField(newField(fieldName));
    }

    public GraphQLFieldDefinition newField(String fieldName) {
        return newFieldDefinition()
                .name(fieldName)
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

}
