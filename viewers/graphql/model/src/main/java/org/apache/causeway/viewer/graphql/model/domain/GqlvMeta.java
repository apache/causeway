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

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutFacet;
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
    private final GqlvMetaIconName metaIconName;
    private final GqlvMetaCssClass metaCssClass;
    private final GqlvMetaLayout metaLayout;
    private final GqlvMetaGrid metaGrid;
    private final GqlvMetaSaveAs metaSaveAs;

    public GqlvMeta(
            final Holder holder,
            final Context context
    ) {
        super(TypeNames.metaTypeNameFor(holder.getObjectSpecification()), context);
        this.holder = holder;

        addChildFieldFor(this.metaId = new GqlvMetaId(context));
        addChildFieldFor(this.metaLogicalTypeName = new GqlvMetaLogicalTypeName(context));
        addChildFieldFor(this.metaVersion = isEntity() ? new GqlvMetaVersion(context) : null);
        addChildFieldFor(this.metaTitle = new GqlvMetaTitle(context));
        addChildFieldFor(this.metaIconName = new GqlvMetaIconName(context));
        addChildFieldFor(this.metaCssClass = new GqlvMetaCssClass(context));
        addChildFieldFor(this.metaLayout = new GqlvMetaLayout(context));
        addChildFieldFor(this.metaGrid = new GqlvMetaGrid(context));
        addChildFieldFor(this.metaSaveAs = new GqlvMetaSaveAs(context));

        val fieldName = context.causewayConfiguration.getViewer().getGraphql().getMetaData().getFieldName();
        buildObjectTypeAndField(fieldName);
    }

    private boolean isEntity() {
        return holder.getObjectSpecification().getBeanSort() == BeanSort.ENTITY;
    }

    @Override
    protected void addDataFetchersForChildren() {
        metaId.addDataFetcher(this);
        metaLogicalTypeName.addDataFetcher(this);
        if (isEntity()) {
            metaVersion.addDataFetcher(this);
        }
        metaTitle.addDataFetcher(this);
        metaIconName.addDataFetcher(this);
        metaCssClass.addDataFetcher(this);
        metaLayout.addDataFetcher(this);
        metaGrid.addDataFetcher(this);
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

        public String iconName() {
            return managedObject()
                    .map(ManagedObject::getIconName)
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
            return managedObject()
                    .map(managedObject -> {
                        val facet = managedObject.getSpecification().getFacet(GridFacet.class);
                        return facet != null ? facet.getGrid(managedObject) : null;
                    })
                    .filter(obj -> Objects.nonNull(obj))
                    .map(JaxbUtils::toStringUtf8)
                    .map(x -> x.replaceAll("(\r\n)", "\n"))
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
