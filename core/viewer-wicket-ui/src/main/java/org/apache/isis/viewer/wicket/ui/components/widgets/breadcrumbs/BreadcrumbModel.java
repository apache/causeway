/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

public class BreadcrumbModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_SIZE = 5;

    private final Map<String, Bookmark> bookmarkByOidStr = Maps.newHashMap();
    private final Map<Bookmark, String> oidStrByBookmark = Maps.newHashMap();
    private final List<Bookmark> list = _Lists.newArrayList();

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
            final RootOid unmarshal = Oid.unmarshaller().unmarshal(oidStr, RootOid.class);
            return unmarshal.enStringNoVersion();
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


    void remove(final String rootOid) {
        Bookmark existingBookmark = bookmarkByOidStr.get(rootOid);
        if(existingBookmark != null) {
            remove(rootOid, existingBookmark);
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
        return entityModel.getObjectAdapterMemento().asBookmarkIfSupported();
    }

    protected EntityModel toEntityModel(final Bookmark bookmark) {
        RootOid rootOid = Factory.ofBookmark(bookmark);
        ObjectAdapterMemento oam = ObjectAdapterMemento.ofRootOid(rootOid);
        return new EntityModel(oam);
    }

    private void remove(final String rootOid, final Bookmark bookmark) {
        bookmarkByOidStr.remove(rootOid);
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
