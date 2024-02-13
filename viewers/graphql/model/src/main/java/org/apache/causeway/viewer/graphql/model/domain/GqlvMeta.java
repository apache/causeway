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
import graphql.schema.DataFetchingEnvironment;
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
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.val;

public class GqlvMeta extends GqlvAbstractCustom {

    private final Holder holder;
    private final GqlvMetaId metaId;
    private final GqlvMetaLogicalTypeName metaLogicalTypeName;
    private final GqlvMetaVersion metaVersion;
    private final GqlvMetaSaveAs metaSaveAs;

    public GqlvMeta(
            final Holder holder,
            final Context context
    ) {
        super(TypeNames.metaTypeNameFor(holder.getObjectSpecification()), context);
        this.holder = holder;

        metaId = new GqlvMetaId(context);
        addChildField(metaId.getField());

        metaLogicalTypeName = new GqlvMetaLogicalTypeName(context);
        addChildField(metaLogicalTypeName.getField());

        if (holder.getObjectSpecification().getBeanSort() == BeanSort.ENTITY) {
            metaVersion = new GqlvMetaVersion(context);
            addChildField(metaVersion.getField());
        } else {
            metaVersion = null;
        }

        metaSaveAs = new GqlvMetaSaveAs(context);
        addChildField(metaSaveAs.getField());

        val fieldName = context.causewayConfiguration.getViewer().getGraphql().getMetaData().getFieldName();
        buildObjectTypeAndField(fieldName);
    }

    @Override
    protected void addDataFetchersForChildren() {
        metaId.addDataFetcher(this);
        metaLogicalTypeName.addDataFetcher(this);
        if (holder.getObjectSpecification().getBeanSort() == BeanSort.ENTITY) {
            metaVersion.addDataFetcher(this);
        }
        metaSaveAs.addDataFetcher(this);
    }

    @Override
    public Object fetchData(final DataFetchingEnvironment environment) {
        return context.bookmarkService.bookmarkFor(environment.getSource())
                .map(bookmark -> new Fetcher(bookmark, context.bookmarkService, context.objectManager))
                .orElseThrow();
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

        public Bookmark bookmark() {
            return bookmark;
        }

    }

    public interface Holder
            extends ObjectSpecificationProvider {

    }
}
