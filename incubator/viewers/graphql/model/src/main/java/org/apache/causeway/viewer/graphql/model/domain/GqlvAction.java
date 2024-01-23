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
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.*;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Log4j2
public class GqlvAction extends GqlvMember<ObjectAction, GqlvActionHolder>
        implements GqlvActionInvokeHolder, GqlvMemberHiddenHolder, GqlvMemberDisabledHolder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final GqlvMemberHidden hidden;
    private final GqlvMemberDisabled disabled;
    private final GqlvActionInvoke invoke;
    private final BookmarkService bookmarkService;

    public GqlvAction(
            final GqlvActionHolder holder,
            final ObjectAction objectAction,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService
            ) {
        super(holder, objectAction, codeRegistryBuilder);

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.actionTypeNameFor(objectAction, holder.getObjectSpecification()));
        this.bookmarkService = bookmarkService;

        this.hidden = new GqlvMemberHidden(this, codeRegistryBuilder);
        this.disabled = new GqlvMemberDisabled(this, codeRegistryBuilder);
        this.invoke = new GqlvActionInvoke(this, codeRegistryBuilder);

        this.gqlObjectType = gqlObjectTypeBuilder.build();

        val field = newFieldDefinition()
                        .name(objectAction.getId())
                        .type(gqlObjectTypeBuilder)
                        .build();

        holder.addField(field);

        setField(field);
    }

    @Override
    public ObjectAction getObjectAction() {
        return getObjectMember();
    }

    @Override
    public void addField(GraphQLFieldDefinition fieldDefinition) {
        gqlObjectTypeBuilder.field(fieldDefinition);
    }

    public void addDataFetcher() {

        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                new Fetcher());

        hidden.addDataFetcher();
        disabled.addDataFetcher();
        invoke.addDataFetcher();
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
