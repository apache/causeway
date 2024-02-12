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

import java.util.Objects;
import java.util.Optional;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.val;

public class GqlvMeta extends GqlvAbstractCustom {

    static GraphQLFieldDefinition id = newFieldDefinition().name("id").type(nonNull(Scalars.GraphQLString)).build();
    static GraphQLFieldDefinition logicalTypeName = newFieldDefinition().name("logicalTypeName").type(nonNull(Scalars.GraphQLString)).build();
    static GraphQLFieldDefinition version = newFieldDefinition().name("version").type(Scalars.GraphQLString).build();

    private final Holder holder;

    public GqlvMeta(
            final Holder holder,
            final Context context
    ) {
        super(newObject().name(TypeNames.metaTypeNameFor(holder.getObjectSpecification())), context);

        this.holder = holder;


        addChildField(id);
        addChildField(logicalTypeName);
        if (this.holder.getObjectSpecification().getBeanSort() == BeanSort.ENTITY) {
            addChildField(version);
        }

        val fieldName = context.causewayConfiguration.getViewer().getGraphql().getMetaData().getFieldName();
        buildObjectTypeAndField(fieldName);
    }


    public void addDataFetchers() {

        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                (DataFetcher<Object>) environment ->
                    context.bookmarkService.bookmarkFor(environment.getSource())
                        .map(bookmark -> new Fetcher(bookmark, context.bookmarkService, context.objectManager))
                        .orElseThrow());

        context.codeRegistryBuilder.dataFetcher(
                coordinates(getGqlObjectType(), logicalTypeName),
                (DataFetcher<Object>) environment -> environment.<Fetcher>getSource().logicalTypeName());

        context.codeRegistryBuilder.dataFetcher(
                coordinates(getGqlObjectType(), id),
                (DataFetcher<Object>) environment -> environment.<Fetcher>getSource().id());

        if (holder.getObjectSpecification().getBeanSort() == BeanSort.ENTITY) {
            context.codeRegistryBuilder.dataFetcher(
                    coordinates(getGqlObjectType(), version),
                    (DataFetcher<Object>) environment -> environment.<Fetcher>getSource().version());
        }
    }


    /**
     * Metadata for every domain object.
     */
    static class Fetcher {

        private final Bookmark bookmark;
        private final BookmarkService bookmarkService;
        private final ObjectManager objectManager;

        Fetcher(
                final Bookmark bookmark,
                final BookmarkService bookmarkService,
                final ObjectManager objectManager) {
            this.bookmark = bookmark;
            this.bookmarkService = bookmarkService;
            this.objectManager = objectManager;
        }

        public String logicalTypeName(){
            return bookmark.getLogicalTypeName();
        }

        public String id(){
            return bookmark.getIdentifier();
        }

        public String version(){
            Object domainObject = bookmarkService.lookup(bookmark).orElse(null);
            if (domainObject == null) {
                return null;
            }
            EntityFacet entityFacet = objectManager.adapt(domainObject).getSpecification().getFacet(EntityFacet.class);
            return Optional.ofNullable(entityFacet)
                    .map(x -> x.versionOf(domainObject))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .orElse(null);
        }

    }

    public interface Holder
            extends GqlvHolder,
            ObjectSpecificationProvider {

    }
}
