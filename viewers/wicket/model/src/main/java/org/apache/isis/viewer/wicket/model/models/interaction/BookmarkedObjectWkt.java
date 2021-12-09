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
package org.apache.isis.viewer.wicket.model.models.interaction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public final class BookmarkedObjectWkt
extends ModelAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;
    @Getter private final Bookmark bookmark;

    /**
     * Request scoped. No yet activated!
     */
    public static class ManagedObjectCache {

        public static MetaDataKey<ManagedObjectCache> KEY = new MetaDataKey<> (){
            private static final long serialVersionUID = 1L;
        };

        public static void ifPresent(final Consumer<ManagedObjectCache> consumer) {
            Optional.ofNullable(RequestCycle.get())
                    .map(requestCycle->requestCycle.getMetaData(KEY))
                    .ifPresent(consumer);
        }

        public static <T> Optional<T> map(final Function<ManagedObjectCache, T> mapper) {
            return Optional.ofNullable(RequestCycle.get())
                    .map(requestCycle->requestCycle.getMetaData(KEY))
                    .map(mapper);
        }

        private final Map<Bookmark, ManagedObject> objectsByBookmark = _Maps.newConcurrentHashMap();

        public Optional<ManagedObject> lookup(final Bookmark bookmark) {
            return Optional.ofNullable(objectsByBookmark.get(bookmark));
        }

        public ManagedObject computeIfAbsent(
                final Bookmark bookmark,
                final Function<? super Bookmark, ? extends ManagedObject> mapper) {
            return objectsByBookmark.computeIfAbsent(bookmark, mapper);
        }

        /**
         * @return the previous value associated with bookmark, or null if there was no mapping for bookmark
         */
        public ManagedObject put(
                final Bookmark bookmark,
                final ManagedObject newObject) {
            if(!newObject.getSpecification().isEntityOrViewModel()) {
                return null; // don't cache managed services
            }
            return objectsByBookmark.put(bookmark, newObject);
        }

        public void invalidate(final Bookmark bookmark) {
            objectsByBookmark.remove(bookmark);
        }
    }

    /** overwrites any current cache entry, only safe when no other views/models reference the same ManagedObject */
    public static BookmarkedObjectWkt ofAdapter(
            final @NonNull IsisAppCommonContext commonContext,
            final @Nullable ManagedObject domainObject) {
        val bookmark = commonContext
                .getObjectManager()
                .getObjectBookmarker()
                .bookmarkObject(domainObject);
        return new BookmarkedObjectWkt(commonContext, bookmark, domainObject);
    }

    public static BookmarkedObjectWkt ofBookmark(
            final @NonNull IsisAppCommonContext commonContext,
            final @Nullable Bookmark bookmark) {
        return new BookmarkedObjectWkt(commonContext, bookmark);
    }

    private BookmarkedObjectWkt(
            final @NonNull IsisAppCommonContext commonContext,
            final @NonNull Bookmark bookmark) {
        super(commonContext);
        this.bookmark = bookmark;
    }

    private BookmarkedObjectWkt(
            final @NonNull IsisAppCommonContext commonContext,
            final @NonNull Bookmark bookmark,
            final @Nullable ManagedObject domainObject) {
        super(commonContext, domainObject);
        this.bookmark = bookmark;
        ManagedObjectCache.ifPresent(cache->cache.put(bookmark, domainObject));
    }

    @Override
    public final void setObject(final ManagedObject object) {
        throw _Exceptions.unsupportedOperation("MangedObjectWkt is immuatable");
    }

    /**
     * Every request-cycle ends with a transaction commit,
     * where JDO will automatically detach entities.
     * For any inline <i>Property</i> edits a new AJAX requests gets created,
     * which results in a new request-cycle, where an EntityPage instance
     * is reused, that was already populated in the previous request-cycle.
     * However, this time, all the contained entities are detached
     * from the persistence layer and need to be re-fetched using their bookmarks.
     */
    public final ManagedObject getObjectAndRefetch() {
        //EntityUtil.assertAttachedWhenEntity()//guard
        val entityOrViewmodel = super.getObject();
        return EntityUtil.computeIfDetached(entityOrViewmodel, this::reload);
    }

    @Override
    protected final ManagedObject load() {
        return ManagedObjectCache
                .map(cache->cache.computeIfAbsent(bookmark, this::loadDirect))
                .orElseGet(()->loadDirect(bookmark));
    }

    // -- HELPER

    private final ManagedObject loadDirect(final Bookmark bookmark) {
        return getCommonContext().getMetaModelContext().loadObject(bookmark)
                .orElse(null);
    }

    /**
     * invalidate cache entry, then reload, then memoize the result
     */
    private final ManagedObject reload(final ManagedObject object) {
        ManagedObjectCache.ifPresent(cache->cache.invalidate(bookmark));
        super.setObject(load());
        return getObject();
    }


}
