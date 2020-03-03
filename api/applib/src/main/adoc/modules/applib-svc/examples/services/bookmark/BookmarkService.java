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

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.val;

/**
 * This service enables a serializable 'bookmark' to be created for an entity.
 *
 */
// tag::refguide[]
public interface BookmarkService {

// end::refguide[]
    /**
     * Given any {@link Bookmark} this service is able to reconstruct to originating domain object the {@link Bookmark}
     * was created for.
     * <p>
     * Note: Not every domain object is bookmark-able.
     * </p>
     * @param domainObject
     * @return optionally a {@link Bookmark} representing given {@code domainObject}
     */
// tag::refguide[]
    Bookmark bookmarkFor(@Nullable Object domainObject);

    default Bookmark bookmarkForElseThrow(Object domainObject) {
// end::refguide[]
        requires(domainObject, "domainObject");
        val bookmark = bookmarkFor(domainObject);
        if(bookmark!=null) {
            return bookmark;
        }
        throw _Exceptions.illegalArgument(
                        "cannot create bookmark for type %s", domainObject.getClass().getName());
// tag::refguide[]
        // ...
    }

    Bookmark bookmarkFor(Class<?> cls, String identifier);

    Object lookup(BookmarkHolder bookmarkHolder);

    Object lookup(Bookmark bookmark);

// end::refguide[]
    /**
     * As {@link #lookup(Bookmark)}, but down-casting to the specified type.
     */
// tag::refguide[]
    default <T> T lookup(Bookmark bookmark, Class<T> cls) {
        return cls.cast(lookup(bookmark));
    }

}
// end::refguide[]
