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
package org.apache.isis.core.metamodel.object;

import java.util.Optional;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.object.ManagedObject.Specialization.BookmarkPolicy;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;

public interface Bookmarkable {

    BookmarkPolicy getBookmarkPolicy();

    default boolean isBookmarkSupported() {
        return !getBookmarkPolicy().isNoBookmark();
    }

    /**
     * Returns the object's bookmark as identified by the {@link ObjectManager}.
     * Bookmarks are immutable, hence will be memoized once fetched.
     */
    Optional<Bookmark> getBookmark();

    boolean isBookmarkMemoized();

    default void invalidateBookmark() {
        _Casts.castTo(BookmarkRefreshable.class, this)
            .ifPresent(Bookmarkable.BookmarkRefreshable::invalidateBookmark);
    }

    /**
     * Implements {@link Bookmarkable} reflecting
     * {@link org.apache.isis.core.metamodel.object.ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK}
     */
    static interface NoBookmark extends Bookmarkable {
        @Override default boolean isBookmarkSupported() { return false; }
        @Override default Optional<Bookmark> getBookmark() { return Optional.empty(); }
        @Override default boolean isBookmarkMemoized() { return false; }
    }

    static interface BookmarkRefreshable extends Bookmarkable {
        /**
         * Invalidates any memoized {@link Bookmark} for (lazy) recreation,
         * reflecting the object's current state.
         * @apiNote only makes sense in the context of mutable viewmodels
         */
        @Override
        void invalidateBookmark();

    }

}
