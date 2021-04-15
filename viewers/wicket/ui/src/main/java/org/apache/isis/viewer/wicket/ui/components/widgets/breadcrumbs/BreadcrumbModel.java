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
package org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

import lombok.val;

public class BreadcrumbModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_SIZE = 5;

    private final Map<String, Bookmark> bookmarkByOidStr = _Maps.newHashMap();
    private final Map<Bookmark, String> oidStrByBookmark = _Maps.newHashMap();
    private final List<Bookmark> list = _Lists.newArrayList();
    
    private final transient IsisAppCommonContext commonContext;
    
    public BreadcrumbModel(IsisAppCommonContext commonContext) {
        super();
        this.commonContext = commonContext;
    }

    public List<EntityModel> getList() {
        List<EntityModel> entityModels = _Lists.newArrayList();
        for (Bookmark bookmark : list) {
            EntityModel entityModel = toEntityModel(bookmark);
            entityModels.add(entityModel);
        }
        return Collections.unmodifiableList(entityModels);
    }

    /**
     * May be null if called for a view model or for home page.
     */
    private String mostRecentlyVisitedOidStr;

    public void visitedHomePage() {
        mostRecentlyVisitedOidStr = null;
    }

    public EntityModel getMostRecentlyVisited() {
        return mostRecentlyVisitedOidStr != null ? lookup(mostRecentlyVisitedOidStr) : null;
    }

    public void visited(final EntityModel entityModel) {

        // ignore view models
        if(entityModel.getTypeOfSpecification().isViewModel()) {
            mostRecentlyVisitedOidStr = null;
            return;
        }

        final String oidStr = oidStrFrom(entityModel);
        mostRecentlyVisitedOidStr = oidStr;

        remove(oidStr);
        addToStart(oidStr, entityModel);

        trimTo(MAX_SIZE);
    }

    private String oidStrFrom(final EntityModel entityModel) {
        final PageParameters pageParameters = entityModel.getPageParametersWithoutUiHints();
        return oidStrFrom(pageParameters);
    }

    private String oidStrFrom(final PageParameters pageParameters) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        if(oidStr == null) {
            return null;
        }
        try {
            final Oid unmarshal = Oid.parse(oidStr);
            return unmarshal.enString();
        } catch(Exception ex) {
            return null;
        }
    }

    private void addToStart(final String oidStr, final EntityModel entityModel) {
        Bookmark bookmark = toBookmark(entityModel);
        bookmarkByOidStr.put(oidStr, bookmark);
        oidStrByBookmark.put(bookmark, oidStr);
        list.add(0, bookmark);
    }

    private void trimTo(final int size) {
        if(list.size() <= size) {
            return;
        }
        final List<Bookmark> bookmarksToRemove = list.subList(size, list.size());
        for (final Bookmark bookmark : bookmarksToRemove) {
            final String oidStr = oidStrByBookmark.get(bookmark);
            remove(oidStr, bookmark);
        }
    }

    public EntityModel lookup(final String oidStr) {
        if(oidStr == null) {
            return null;
        }
        final Bookmark bookmark = bookmarkByOidStr.get(oidStr);
        if(bookmark == null) {
            return null;
        }
        return toEntityModel(bookmark);
    }

    public void detach() {
        // previously list held EntityModels rather than Bookmarks
        // this code is now redundant, I think.

        // for (EntityModel entityModel : list) {
        //     entityModel.detach();
        // }
    }


    void remove(final String oid) {
        Bookmark existingBookmark = bookmarkByOidStr.get(oid);
        if(existingBookmark != null) {
            remove(oid, existingBookmark);
        }
    }

    public void remove(final EntityModel entityModel) {
        Bookmark bookmark = toBookmark(entityModel);
        final String oidStr = oidStrByBookmark.get(bookmark);
        if(oidStr != null) {
            remove(oidStr, bookmark);
        }
    }

    protected Bookmark toBookmark(final EntityModel entityModel) {
        return entityModel.asBookmarkIfSupported();
    }

    protected EntityModel toEntityModel(final Bookmark bookmark) {
        val oid = Oid.forBookmark(bookmark);
        val objectAdapterMemento = commonContext.mementoFor(oid);
        return EntityModel.ofMemento(commonContext, objectAdapterMemento);
    }

    private void remove(final String oid, final Bookmark bookmark) {
        bookmarkByOidStr.remove(oid);
        oidStrByBookmark.remove(bookmark);
        list.remove(bookmark);
    }

    public synchronized void clear() {
        bookmarkByOidStr.clear();
        oidStrByBookmark.clear();
        list.clear();
        mostRecentlyVisitedOidStr = null;
    }
}
