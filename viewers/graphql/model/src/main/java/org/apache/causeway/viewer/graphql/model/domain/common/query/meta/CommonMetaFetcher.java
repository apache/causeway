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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutPrefixFacet;
import org.apache.causeway.core.metamodel.object.Bookmarkable;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.viewer.graphql.model.domain.common.query.ResourcePath;

/**
 * Metadata for every domain object.
 */
public class CommonMetaFetcher {

    static final int MAXIMUM_BREADCRUMB_ANCESTORS = 32;

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;
    private final CausewayConfiguration causewayConfiguration;
    private final ResourcePath resourcePath;

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
        this.resourcePath = ResourcePath.from(causewayConfiguration);
    }

    public String logicalTypeName() {
        return bookmark.logicalTypeName();
    }

    public String id() {
        return bookmark.identifier();
    }

    public String version() {
        return managedObject()
                .map(managedObject -> {
                    var domainPojo = managedObject.getPojo();
                    var entityFacet = managedObject.objSpec().lookupFacet(EntityFacet.class).orElse(null);
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

    public List<Map<String, String>> breadcrumbs() {
        return managedObject()
                .map(current -> traverseBreadcrumbs(
                        bookmark,
                        current.getPojo(),
                        pojo -> objectManager.adapt(pojo).objSpec().getNavigableParent(pojo),
                        bookmarkService::bookmarkFor,
                        pojo -> objectManager.adapt(pojo).getTitle(),
                        this::breadcrumbIcon))
                .orElseGet(List::of);
    }

    static List<Map<String, String>> traverseBreadcrumbs(
            final Bookmark currentBookmark,
            final Object currentPojo,
            final Function<Object, Object> parentResolver,
            final Function<Object, Optional<Bookmark>> bookmarkResolver,
            final Function<Object, String> titleResolver,
            final Function<Bookmark, String> iconResolver) {
        final var seen = new HashSet<Bookmark>();
        seen.add(currentBookmark);
        final var ancestors = new ArrayList<Map<String, String>>();
        var pojo = currentPojo;
        while (true) {
            final Object parentPojo;
            try {
                parentPojo = parentResolver.apply(pojo);
            } catch (RuntimeException ex) {
                throw breadcrumbFailure("GRAPHQL_BREADCRUMB_PARENT_FAILED", "Navigable parent evaluation failed.");
            }
            if (parentPojo == null) {
                break;
            }
            final Optional<Bookmark> parentBookmark;
            try {
                parentBookmark = bookmarkResolver.apply(parentPojo);
            } catch (RuntimeException ex) {
                throw breadcrumbFailure("GRAPHQL_BREADCRUMB_IDENTITY_FAILED", "Navigable parent identity resolution failed.");
            }
            if (parentBookmark.isEmpty()) {
                break;
            }
            if (!seen.add(parentBookmark.get())) {
                throw breadcrumbFailure("GRAPHQL_BREADCRUMB_CYCLE", "Navigable parent hierarchy contains a cycle.");
            }
            if (ancestors.size() >= MAXIMUM_BREADCRUMB_ANCESTORS) {
                throw breadcrumbFailure(
                        "GRAPHQL_BREADCRUMB_DEPTH_EXCEEDED",
                        "Navigable parent hierarchy exceeds " + MAXIMUM_BREADCRUMB_ANCESTORS + " ancestors.");
            }
            final String title;
            try {
                title = java.util.Objects.requireNonNull(titleResolver.apply(parentPojo));
            } catch (RuntimeException ex) {
                throw breadcrumbFailure("GRAPHQL_BREADCRUMB_TITLE_FAILED", "Navigable parent title resolution failed.");
            }
            final var ancestor = new java.util.LinkedHashMap<String, String>();
            ancestor.put("logicalTypeName", parentBookmark.get().logicalTypeName());
            ancestor.put("id", parentBookmark.get().identifier());
            ancestor.put("title", title);
            final var icon = iconResolver.apply(parentBookmark.get());
            if (icon != null) {
                ancestor.put("icon", icon);
            }
            ancestors.add(Map.copyOf(ancestor));
            pojo = parentPojo;
        }
        Collections.reverse(ancestors);
        return List.copyOf(ancestors);
    }

    public String cssClass() {
        return managedObject()
                .map(managedObject -> {
                    var facet = managedObject.objSpec().lookupFacet(CssClassFacet.class).orElse(null);
                    return facet != null ? facet.cssClass(managedObject) : null;
                })
                .orElse(null);
    }

    public String layout() {
        return managedObject()
                .map(managedObject -> {
                    var facet = managedObject.objSpec().lookupFacet(LayoutPrefixFacet.class).orElse(null);
                    return facet != null ? facet.layoutPrefix(managedObject) : null;
                })
                .orElse(null);
    }

    public String grid() {
        return resource("grid");
    }

    public String icon() {
        return resource("icon");
    }

    private String breadcrumbIcon(final Bookmark ancestor) {
        if (causewayConfiguration.viewer().graphql().resources().effectiveStructuralMetadataResponseType()
                == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return null;
        }
        return resourcePath.metadata(
                ancestor,
                causewayConfiguration.viewer().graphql().metaData().fieldName(),
                "icon");
    }

    private String resource(final String resource) {
        return managedObject()
                .flatMap(Bookmarkable::getBookmark)
                .map(bookmark -> resourcePath.metadata(
                        bookmark,
                        causewayConfiguration.viewer().graphql().metaData().fieldName(),
                        resource))
                .orElse(null);
    }

    private Optional<ManagedObject> managedObject() {
        return bookmarkService.lookup(bookmark)
                .map(objectManager::adapt);
    }

    private static IllegalStateException breadcrumbFailure(final String code, final String message) {
        return new IllegalStateException(code + ": " + message);
    }
}
