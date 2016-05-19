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
package org.apache.isis.core.metamodel.services.bookmarks;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkHolder;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;

/**
 * This service enables a serializable &quot;bookmark&quot; to be created for an entity.
 *
 * <p>
 * This implementation has no UI and there are no other implementations of the service API, and so it annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  Because this class is implemented in core, this means
 * that it is automatically registered and available for use; no further configuration is required.
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class BookmarkServiceInternalDefault implements BookmarkService2 {


    @Programmatic
    @Override
    public Object lookup(
            final BookmarkHolder bookmarkHolder,
            final FieldResetPolicy fieldResetPolicy) {
        Bookmark bookmark = bookmarkHolder.bookmark();
        return bookmark != null? lookup(bookmark, fieldResetPolicy): null;
    }

    @Programmatic
    @Override
    public Object lookup(final BookmarkHolder bookmarkHolder) {
        return lookup(bookmarkHolder, FieldResetPolicy.RESET);
    }


    @Programmatic
    @Override
    public Object lookup(
            final Bookmark bookmark,
            final FieldResetPolicy fieldResetPolicy) {
        if(bookmark == null) {
            return null;
        }
        try {
            return persistenceSessionServiceInternal.lookup(bookmark, fieldResetPolicy);
        } catch(ObjectNotFoundException ex) {
            return null;
        }
    }

    @Programmatic
    @Override
    public Object lookup(final Bookmark bookmark) {
        return lookup(bookmark, FieldResetPolicy.RESET);
    }


    @SuppressWarnings("unchecked")
    @Programmatic
    @Override
    public <T> T lookup(
            final Bookmark bookmark,
            final FieldResetPolicy fieldResetPolicy,
            Class<T> cls) {
        return (T) lookup(bookmark, fieldResetPolicy);
    }

    @SuppressWarnings("unchecked")
    @Programmatic
    @Override
    public <T> T lookup(final Bookmark bookmark, Class<T> cls) {
        return (T) lookup(bookmark, FieldResetPolicy.RESET, cls);
    }


    @Programmatic
    @Override
    public Bookmark bookmarkFor(final Object domainObject) {
        if(domainObject == null) {
            return null;
        }
        return persistenceSessionServiceInternal.bookmarkFor(unwrapped(domainObject));
    }

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }


    @Programmatic
    @Override
    public Bookmark bookmarkFor(Class<?> cls, String identifier) {
        return persistenceSessionServiceInternal.bookmarkFor(cls, identifier);
    }



    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

    @javax.inject.Inject
    private WrapperFactory wrapperFactory;

}
