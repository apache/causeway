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

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkHolder;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.adapter.DomainObjectServices;
import org.apache.isis.core.metamodel.adapter.DomainObjectServicesAware;

public class BookmarkServiceDefault implements BookmarkService, DomainObjectServicesAware {

    private DomainObjectServices domainObjectServices;
    
    /**
     * Contributed action contributed to
     * any class that implements {@link BookmarkHolder}.
     * 
     * <p>
     * If required, applications can suppress by subclassing and annotating the
     * overridden method with <tt>@Hidden</tt>:
     * <pre>
     * @Hidden
     * public Object lookup(final BookmarkHolder bookmarkHolder) {
     *     return super.lookup(bookmarkHolder);
     * }
     * </pre>
     */
    @Override
    @NotInServiceMenu
    @NotContributed(As.ASSOCIATION)
    public Object lookup(final BookmarkHolder bookmarkHolder) {
        Bookmark bookmark = bookmarkHolder.bookmark();
        return bookmark != null? lookup(bookmark): null;
    }

    /**
     * Contributed property (named '<tt>Object</tt>'), contributed to
     * any class that implements {@link BookmarkHolder}.
     * 
     * <p>
     * If required, applications can suppress by subclassing and annotating the
     * overridden method with <tt>@Hidden</tt>:
     * <pre>
     * @Hidden
     * public Object object(final BookmarkHolder bookmarkHolder) {
     *     return super.object(bookmarkHolder);
     * }
     * </pre>
     */
    @NotInServiceMenu
    @NotContributed(As.ACTION)
    public Object object(final BookmarkHolder bookmarkHolder) {
        return lookup(bookmarkHolder);
    }
    
    @Hidden
    public Object lookup(final Bookmark bookmark) {
        if(bookmark == null) {
            return null;
        }
        return domainObjectServices.lookup(bookmark);
    }


    @Override
    @Programmatic
    public void setDomainObjectServices(final DomainObjectServices domainObjectServices) {
        this.domainObjectServices = domainObjectServices;
    }

    @Override
    @Hidden
    public Bookmark bookmarkFor(final Object domainObject) {
        return domainObjectServices.bookmarkFor(domainObject);
    }


}
