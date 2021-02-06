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
package org.apache.isis.applib.services.bookmark;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;

/**
 * This service provides a serializable 'bookmark' for any entity, and
 * conversely to lookup an entity from a bookmark.
 *
 * @since 1.x {@index}
 */
public interface BookmarkService {

    /**
     * Returns the {@link Bookmark} for the given domain object.
     *
     * <p>
     * <b>Note</b>: Not every domain object is bookmark-able: only entities, view models and services (NOT values or collections)
     * </p>
     *
     * @param domainObject - domain object (if any) to return a bookmark for
     * @return optionally a {@link Bookmark} representing given {@code domainObject}
     */
    Bookmark bookmarkFor(@Nullable Object domainObject);

    /**
     * As per {@link #bookmarkFor(Object)}, but requires that a non-null {@link Bookmark} is returned.
     *
     * @param domainObject - that can be bookmarked
     * @return a (non-null) {@link Bookmark} for the provided domain object.
     */
    default Bookmark bookmarkForElseThrow(Object domainObject) {

        requires(domainObject, "domainObject");
        val bookmark = bookmarkFor(domainObject);
        if(bookmark!=null) {
            return bookmark;
        }
        throw _Exceptions.illegalArgument(
                "cannot create bookmark for type %s", domainObject.getClass().getName());
    }

    /**
     * Utility method that creates a {@link Bookmark} from the constituent parts.
     *
     * @return - {@link Bookmark} for provided class and identifier
     */
    Bookmark bookmarkFor(Class<?> cls, String identifier);

    /**
     * @see #lookup(Bookmark)
     *
     * @param bookmarkHolder - from which the {@link Bookmark} is obtained
     * @return - corresponding domain object
     */
    Object lookup(BookmarkHolder bookmarkHolder);

    /**
     * Reciprocal of {@link #bookmarkFor(Object)}
     *
     * @param bookmark - representing a domain object
     * @return - the corresponding domain object
     */
    Object lookup(Bookmark bookmark);

    /**
     * As {@link #lookup(Bookmark)}, but down-casting to the specified type.
     */
    default <T> T lookup(Bookmark bookmark, Class<T> cls) {
        return cls.cast(lookup(bookmark));
    }

}
