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

import org.apache.causeway.viewer.graphql.model.context.Context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

public abstract class GqlvAbstractCustom extends GqlvAbstract implements GqlvHolder {

    protected final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    @Getter(AccessLevel.PROTECTED)
    private GraphQLObjectType gqlObjectType;

    protected GqlvAbstractCustom(
            final GraphQLObjectType.Builder gqlObjectTypeBuilder,
            final Context context) {
        super(context);

        this.gqlObjectTypeBuilder = gqlObjectTypeBuilder;
    }

    protected final GraphQLFieldDefinition addChildField(GraphQLFieldDefinition childField) {
        if (this.gqlObjectType != null) {
            throw new IllegalStateException("GqlObjectType has already been created");
        }

        if (childField != null) {
            gqlObjectTypeBuilder.field(childField);
        }
        return childField;
    }

    protected GraphQLFieldDefinition buildObjectTypeAndSetFieldName(String fieldName) {
        val graphQLObjectType = buildObjectType();

        return setField(newFieldDefinition()
                .name(fieldName)
                .type(graphQLObjectType)
                .build());
    }

    protected final GraphQLObjectType buildObjectType() {
        this.gqlObjectType = gqlObjectTypeBuilder.build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(this.gqlObjectType);
        return this.gqlObjectType;

    }


    public final FieldCoordinates coordinatesFor(final GraphQLFieldDefinition field) {
        return FieldCoordinates.coordinates(gqlObjectType, field);
    }

}
