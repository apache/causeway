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

import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvProperty extends GqlvAssociation<OneToOneAssociation, GqlvPropertyHolder> implements GqlvPropertyGetHolder, GqlvMemberHiddenHolder, GqlvMemberDisabledHolder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final GqlvMemberHidden hidden;
    private final GqlvMemberDisabled disabled;
    private final GqlvPropertyGet get;
    private final BookmarkService bookmarkService;

    public GqlvProperty(
            final GqlvPropertyHolder domainObject,
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService
    ) {
        super(domainObject, oneToOneAssociation, codeRegistryBuilder);

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.propertyTypeNameFor(holder.getObjectSpecification(), oneToOneAssociation));
        this.bookmarkService = bookmarkService;

        this.hidden = new GqlvMemberHidden(this, codeRegistryBuilder);
        this.disabled = new GqlvMemberDisabled(this, codeRegistryBuilder);
        this.get = new GqlvPropertyGet(this, codeRegistryBuilder, specificationLoader);

        this.gqlObjectType = gqlObjectTypeBuilder.build();

        setField(
            holder.addField(
                newFieldDefinition()
                    .name(oneToOneAssociation.getId())
                    .type(gqlObjectTypeBuilder)
                    .build()
            )
        );
    }


    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectAssociation();
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                new GqlvProperty.Fetcher());

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        get.addDataFetcher();
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


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }
}
