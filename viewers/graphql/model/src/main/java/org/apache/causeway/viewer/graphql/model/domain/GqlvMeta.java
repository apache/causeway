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

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.causeway.core.metamodel.object.Bookmarkable;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.val;

public class GqlvMeta extends GqlvAbstractCustom {

    private final Holder holder;
    private final GqlvMetaId metaId;
    private final GqlvMetaLogicalTypeName metaLogicalTypeName;
    private final GqlvMetaVersion metaVersion;
    private final GqlvMetaTitle metaTitle;
    private final GqlvMetaIcon metaIcon;
    private final GqlvMetaCssClass metaCssClass;
    private final GqlvMetaLayout metaLayout;
    private final GqlvMetaGrid metaGrid;
    private final GqlvMetaSaveAs metaSaveAs;

    private final CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    public GqlvMeta(
            final Holder holder,
            final Context context
    ) {
        super(TypeNames.metaTypeNameFor(holder.getObjectSpecification()), context);
        this.holder = holder;

        this.graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

        if(isBuilt()) {
            this.metaId = null;
            this.metaLogicalTypeName = null;
            this.metaVersion = null;
            this.metaTitle = null;
            this.metaIcon = null;
            this.metaCssClass = null;
            this.metaLayout = null;
            this.metaGrid = null;
            this.metaSaveAs = null;
            return;
        }

        addChildFieldFor(this.metaId = new GqlvMetaId(context));
        addChildFieldFor(this.metaLogicalTypeName = new GqlvMetaLogicalTypeName(context));
        addChildFieldFor(this.metaVersion = isEntity() ? new GqlvMetaVersion(context) : null);
        addChildFieldFor(this.metaTitle = new GqlvMetaTitle(context));
        addChildFieldFor(this.metaCssClass = new GqlvMetaCssClass(context));
        addChildFieldFor(this.metaLayout = new GqlvMetaLayout(context));
        addChildFieldFor(this.metaSaveAs = new GqlvMetaSaveAs(context));

        addChildFieldFor(this.metaIcon = isResourceNotForbidden() ? new GqlvMetaIcon(context) : null);
        addChildFieldFor(this.metaGrid = isResourceNotForbidden() ? new GqlvMetaGrid(context) : null);

        val fieldName = graphqlConfiguration.getMetaData().getFieldName();
        buildObjectTypeAndField(fieldName, "Object metadata");
    }

    private boolean isResourceNotForbidden() {
        return graphqlConfiguration.getResources().getResponseType() != CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN;
    }

    private boolean isEntity() {
        return holder.getObjectSpecification().getBeanSort() == BeanSort.ENTITY;
    }

    @Override
    protected void addDataFetchersForChildren() {
        if (metaId == null) {
            // none of the fields will have been initialized
            return;
        }
        metaId.addDataFetcher(this);
        metaLogicalTypeName.addDataFetcher(this);
        if (isEntity()) {
            metaVersion.addDataFetcher(this);
        }
        metaTitle.addDataFetcher(this);
        metaCssClass.addDataFetcher(this);
        metaLayout.addDataFetcher(this);
        metaSaveAs.addDataFetcher(this);
        if (metaGrid != null) {
            metaGrid.addDataFetcher(this);
        }
        if (metaIcon != null) {
            metaIcon.addDataFetcher(this);
        }
    }

    @Override
    public Object fetchData(final DataFetchingEnvironment environment) {
        return context.bookmarkService.bookmarkFor(environment.getSource())
                .map(bookmark -> new Fetcher(bookmark, context.bookmarkService, context.objectManager, context.causewayConfiguration))
                .orElseThrow();
    }


    /**
     * Metadata for every domain object.
     */
    static class Fetcher {

        private final Bookmark bookmark;
        private final BookmarkService bookmarkService;
        private final ObjectManager objectManager;
        private final CausewayConfiguration causewayConfiguration;
        private final String graphqlPath;

        Fetcher(
                final Bookmark bookmark,
                final BookmarkService bookmarkService,
                final ObjectManager objectManager,
                final CausewayConfiguration causewayConfiguration
        ) {
            this.bookmark = bookmark;
            this.bookmarkService = bookmarkService;
            this.objectManager = objectManager;
            this.causewayConfiguration = causewayConfiguration;
            this.graphqlPath = causewayConfiguration.valueOf("spring.graphql.path").orElse("/graphql");
        }

        public String logicalTypeName(){
            return bookmark.getLogicalTypeName();
        }

        public String id(){
            return bookmark.getIdentifier();
        }

        public String version(){
            return managedObject()
                    .map(managedObject -> {
                        val domainPojo = managedObject.getPojo();
                        val entityFacet = managedObject.getSpecification().getFacet(EntityFacet.class);
                        if (entityFacet != null) {
                            val object = entityFacet.versionOf(domainPojo);
                            return object != null ? object.toString() : null;
                        } else {
                            return null;
                        }
                    }).orElse(null);
        }

        public Bookmark bookmark() {
            return bookmark;
        }

        public String title() {
            return managedObject()
                    .map(ManagedObject::getTitle)
                    .orElse(null);
        }

        public String cssClass() {
            return managedObject()
                    .map(managedObject -> {
                        val facet = managedObject.getSpecification().getFacet(CssClassFacet.class);
                        return facet != null ? facet.cssClass(managedObject) : null;
                    })
                    .orElse(null);
        }

        public String layout() {
            return managedObject()
                    .map(managedObject -> {
                        val facet = managedObject.getSpecification().getFacet(LayoutFacet.class);
                        return facet != null ? facet.layout(managedObject) : null;
                    })
                    .orElse(null);
        }

        public String grid() {
            return resource("grid");
        }

        public String icon() {
            return resource("icon");
        }

        private String resource(String resource) {
            return managedObject()
                    .flatMap(Bookmarkable::getBookmark
                    ).map(bookmark -> String.format(
                            "//%s/object/%s:%s/%s/%s",
                            graphqlPath, bookmark.getLogicalTypeName(), bookmark.getIdentifier(), causewayConfiguration.getViewer().getGraphql().getMetaData().getFieldName(), resource) )
                    .orElse(null);
        }

        private Optional<ManagedObject> managedObject() {
            return bookmarkService.lookup(bookmark)
                    .map(objectManager::adapt);
        }
    }

    public interface Holder
            extends ObjectSpecificationProvider {

    }
}
