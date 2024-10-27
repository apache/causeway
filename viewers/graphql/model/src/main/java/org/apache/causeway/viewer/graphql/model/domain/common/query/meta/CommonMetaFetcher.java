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

import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.causeway.core.metamodel.object.Bookmarkable;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

/**
 * Metadata for every domain object.
 */
public class CommonMetaFetcher {

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;
    private final CausewayConfiguration causewayConfiguration;
    private final String graphqlPath;

    public CommonMetaFetcher(
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

    public String logicalTypeName() {
        return bookmark.getLogicalTypeName();
    }

    public String id() {
        return bookmark.getIdentifier();
    }

    public String version() {
        return managedObject()
                .map(managedObject -> {
                    var domainPojo = managedObject.getPojo();
                    var entityFacet = managedObject.getSpecification().getFacet(EntityFacet.class);
                    if (entityFacet != null) {
                        var object = entityFacet.versionOf(domainPojo);
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
                    var facet = managedObject.getSpecification().getFacet(CssClassFacet.class);
                    return facet != null ? facet.cssClass(managedObject) : null;
                })
                .orElse(null);
    }

    public String layout() {
        return managedObject()
                .map(managedObject -> {
                    var facet = managedObject.getSpecification().getFacet(LayoutFacet.class);
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
                        graphqlPath, bookmark.getLogicalTypeName(), bookmark.getIdentifier(), causewayConfiguration.getViewer().getGraphql().getMetaData().getFieldName(), resource))
                .orElse(null);
    }

    private Optional<ManagedObject> managedObject() {
        return bookmarkService.lookup(bookmark)
                .map(objectManager::adapt);
    }
}
