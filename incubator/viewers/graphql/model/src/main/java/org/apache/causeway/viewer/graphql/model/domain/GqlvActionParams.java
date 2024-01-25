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

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Log4j2
public class GqlvActionParams {

    private final GqlvActionParamsHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final BookmarkService bookmarkService;

    /**
     * Populated iff {@link #hasParams()}
     */
    private final GraphQLFieldDefinition field;

    private final Map<String, ObjectActionParameter> params = new LinkedHashMap<>();

    public GqlvActionParams(
            final GqlvActionParamsHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService
            ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.gqlObjectTypeBuilder = newObject().name(TypeNames.actionParamsTypeNameFor(holder.getHolder().getObjectSpecification(), holder.getObjectAction()));
        this.bookmarkService = bookmarkService;

        this.gqlObjectType = gqlObjectTypeBuilder.build();

        this.field = hasParams() ? holder.addField(newFieldDefinition()
                .name("params")
                .type(gqlObjectTypeBuilder)
                .build()) : null;
    }

    public boolean hasParams() {
        return !params.isEmpty();
    }

    void addParam(ObjectActionParameter objectActionParameter) {
        // TODO
    }


    // @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                new Fetcher());

    }

    private class Fetcher implements DataFetcher<Object> {
        @Override
        public Object get(DataFetchingEnvironment dataFetchingEnvironment) {

            val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

            return bookmarkService.bookmarkFor(sourcePojo)
                    .map(bookmark -> new BookmarkedPojo(bookmark, bookmarkService))
                    .orElseThrow();
        }
    }

    //@Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

}
