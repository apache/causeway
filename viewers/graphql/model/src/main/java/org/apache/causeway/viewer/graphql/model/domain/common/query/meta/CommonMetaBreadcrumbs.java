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
package org.apache.causeway.viewer.graphql.model.domain.common.query.meta;

import java.util.List;
import java.util.Map;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class CommonMetaBreadcrumbs extends Element {

    static final String ENTRY_TYPE_NAME = "RichNavigableBreadcrumb";

    public CommonMetaBreadcrumbs(final Context context) {
        super(context);
        setField(newFieldDefinition()
                .name("breadcrumbs")
                .description("Navigable ancestors ordered from root to immediate parent.")
                .type(GraphQLList.list(GraphQLNonNull.nonNull(entryType(context))))
                .build());
    }

    private static GraphQLObjectType entryType(final Context context) {
        final var existing = context.graphQLTypeRegistry.lookup(ENTRY_TYPE_NAME, GraphQLObjectType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        final var type = GraphQLObjectType.newObject()
                .name(ENTRY_TYPE_NAME)
                .description("Bookmark identity and title for one navigable ancestor.")
                .field(newFieldDefinition()
                        .name("logicalTypeName")
                        .type(GraphQLNonNull.nonNull(Scalars.GraphQLString)))
                .field(newFieldDefinition()
                        .name("id")
                        .type(GraphQLNonNull.nonNull(Scalars.GraphQLString)))
                .field(newFieldDefinition()
                        .name("title")
                        .type(GraphQLNonNull.nonNull(Scalars.GraphQLString)))
                .build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        return context.graphQLTypeRegistry.lookup(ENTRY_TYPE_NAME, GraphQLObjectType.class).orElse(type);
    }

    @Override
    protected List<Map<String, String>> fetchData(final DataFetchingEnvironment environment) {
        return environment.<CommonMetaFetcher>getSource().breadcrumbs();
    }
}
