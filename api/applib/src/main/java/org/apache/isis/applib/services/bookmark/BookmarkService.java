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

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

/**
 * This service provides a serializable 'bookmark' for any entity, and
 * conversely to lookup an entity from a bookmark.
 *
 * @since 1.x {@index}
 */
public interface BookmarkService {

    /**
     * Optionally returns the {@link Bookmark} for the given domain object,
     * based on whether can create a bookmark for it.
     *
     * <p>
     * <b>Note</b>: Not every domain object is bookmark-able:
     * only entities, view models and services (NOT values or collections)
     * </p>
     *
     * @param domainObject - domain object (if any) to return a bookmark for
     * @return optionally a {@link Bookmark} representing given {@code domainObject}
     */
    Optional<Bookmark> bookmarkFor(@Nullable Object domainObject);

    /**
     * Optionally returns a {@link Bookmark} created from the constituent parts,
     * based on whether can create a bookmark from these.
     * <p>
     * With constituent parts a {@code type} and an {@code identifier} that uniquely
     * identifies an instance of this type.
     *
     * @return - {@link Bookmark} for provided class and identifier
     */
    Optional<Bookmark> bookmarkFor(@Nullable Class<?> type, @Nullable String identifier);

    /**
     * @see #lookup(Bookmark)
     *
     * @param bookmarkHolder - from which the {@link Bookmark} is obtained
     * @return - optionally, the corresponding domain object
     */
    Optional<Object> lookup(@Nullable BookmarkHolder bookmarkHolder);

    /**
     * Reciprocal of {@link #bookmarkFor(Object)}
     *
     * @param bookmark - representing a domain object
     * @return - optionally, the corresponding domain object
     */
    Optional<Object> lookup(@Nullable Bookmark bookmark);

    // -- SHORTCUTS

    /**
     * As {@link #lookup(Bookmark)}, but down-casting to the specified type.
     */
    default <T> Optional<T> lookup(@Nullable Bookmark bookmark, @NonNull Class<T> cls) {
        return lookup(bookmark)
                .map(t->cls.cast(t));
    }

    /**
     * As per {@link #bookmarkFor(Object)}, but requires that a non-null {@link Bookmark} is returned.
     *
     * @param domainObject - to be bookmarked
     * @return a (non-null) {@link Bookmark} for the provided domain object.
     */
    default Bookmark bookmarkForElseFail(@Nullable Object domainObject) {
        return bookmarkFor(domainObject)
                .orElseThrow(()->_Exceptions.illegalArgument(
                        "cannot create bookmark for type %s",
                        domainObject!=null
                            ? domainObject.getClass().getName()
                            : "<null>"));
    }

}
