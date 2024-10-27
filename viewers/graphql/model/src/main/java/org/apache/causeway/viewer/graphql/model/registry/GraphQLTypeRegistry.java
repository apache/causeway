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
package org.apache.causeway.viewer.graphql.model.registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLEnumValueDefinition.newEnumValueDefinition;

import org.springframework.stereotype.Component;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class GraphQLTypeRegistry {

    private final Provider<Context> contextProvider;

    Set<GraphQLType> graphQLTypes = new HashSet<>();

    public Set<GraphQLType> getGraphQLTypes() {
        return Collections.unmodifiableSet(graphQLTypes);
    }

    void addTypeIfNotAlreadyPresent(
            final GraphQLObjectType typeToAdd,
            final String logicalTypeName){

        if (isPresent(typeToAdd, GraphQLObjectType.class)){
            // For now we just log and skip
            log.info("GraphQLObjectType for {} already present", logicalTypeName);
            return;
        }
        graphQLTypes.add(typeToAdd);
    }

    public GraphQLEnumType addEnumTypeIfNotAlreadyPresent(
            final Class<?> typeToAdd,
            final SchemaType schemaType) {
        var objectSpec = contextProvider.get().specificationLoader.loadSpecification(typeToAdd);
        var typeName = TypeNames.enumTypeNameFor(objectSpec, schemaType);
        var enumTypeIfAny = lookup(typeName, GraphQLEnumType.class);

        if (enumTypeIfAny.isPresent()) {
            return enumTypeIfAny.get();
        }

        var enumTypeToAdd = (Class<? extends Enum<?>>) typeToAdd;
        var enumType = newEnum()
                .name(typeName)
                .values(Stream.of(enumTypeToAdd.getEnumConstants())
                        .map(enumValue -> newEnumValueDefinition()
                                .name(enumValue.name())
                                .value(enumValue)
                                .build()).collect(Collectors.toList())
                )
                .build();
        add(enumType);
        return enumType;
    }

    public void addTypeIfNotAlreadyPresent(final GraphQLType typeToAdd) {

        if (typeToAdd instanceof GraphQLEnumType) {
            addTypeIfNotAlreadyPresent((GraphQLEnumType) typeToAdd);
            return;
        }

        if (typeToAdd instanceof GraphQLObjectType) {
            addTypeIfNotAlreadyPresent((GraphQLObjectType) typeToAdd);
            return;
        }

        if (typeToAdd instanceof GraphQLInputObjectType) {
            addTypeIfNotAlreadyPresent((GraphQLInputObjectType) typeToAdd);
            return;
        }

        // TODO: none of these types yet handled
        // GraphQLTypeReference
        // GraphQLScalarType
        // GraphQLCompositeType
        // GraphQLUnionType
        // GraphQLEnumType
        // GraphQLInterfaceType
        // GraphQLList
        // GraphQLNonNull
        log.warn("GraphQLType {} not yet implemented", typeToAdd.getClass().getName());
    }

    void addTypeIfNotAlreadyPresent(final GraphQLEnumType typeToAdd){
        if (isPresent(typeToAdd, GraphQLEnumType.class)){
            // For now we just log and skip
            log.debug("GraphQLEnumType for {} already present", typeToAdd.getName());
            return;
        }
        add(typeToAdd);
    }

    void addTypeIfNotAlreadyPresent(final GraphQLObjectType typeToAdd){
        if (isPresent(typeToAdd, GraphQLObjectType.class)){
            // For now we just log and skip
            log.debug("GraphQLObjectType for {} already present", typeToAdd.getName());
            return;
        }
        add(typeToAdd);
    }

    void addTypeIfNotAlreadyPresent(final GraphQLInputObjectType typeToAdd) {
        if (isPresent(typeToAdd, GraphQLInputObjectType.class)){
            // For now we just log and skip
            log.debug("GraphQLInputObjectType for {} already present", typeToAdd.getName());
            return;
        }
        add(typeToAdd);
    }

    private boolean isPresent(
            final GraphQLNamedType typeToAdd,
            final Class<? extends GraphQLNamedType> cls) {
        return graphQLTypes.stream()
                .filter(o -> o.getClass().isAssignableFrom(cls))
                .map(cls::cast)
                .anyMatch(ot -> ot.getName().equals(typeToAdd.getName()));
    }

    public <T extends GraphQLNamedType> Optional<T> lookup(
            final String typeName,
            final Class<T> cls) {
        return graphQLTypes.stream()
                .filter(o -> o.getClass().isAssignableFrom(cls))
                .map(cls::cast)
                .filter(ot -> ot.getName().equals(typeName))
                .findFirst();
    }

    private void add(GraphQLType typeToAdd) {
        graphQLTypes.add(typeToAdd);
    }

}
