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

import javax.annotation.Priority;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.PriorityPrecedence;

import lombok.val;


/**
 * Hmm, not sure if this is really needed...  HAVE REMOVED FROM MODULE
 *
 * The responsibility and usage of IdStringifier is in the handling of PKs (either datastore-definde or application-defined)
 * into a string.  What neither JDO nor JPA support is the use of an arbitrary object (eg a reference to a Customer),
 * so I can't see that this would ever be called...
 */
// @Component
@Priority(PriorityPrecedence.LATE + 100) // after the rest
public class IdStringifierForEntityDEFUNCT extends IdStringifier.Abstract<Object> {

    private final BookmarkService bookmarkService;
    private final IdStringifierForBookmarkDEFUNCT idStringifierForBookmark;

    @Inject
    public IdStringifierForEntityDEFUNCT(
            final BookmarkService bookmarkService,
            final IdStringifierForBookmarkDEFUNCT idStringifierForBookmark) {
        super(Object.class);
        this.bookmarkService = bookmarkService;
        this.idStringifierForBookmark = idStringifierForBookmark;
    }

    @Override
    public boolean handles(Class<?> candidateValueClass) {
        return true;
    }

    public String enstring(final Object object) {
        if (object == null) {
            return null;
        }
        val bookmark = bookmarkService.bookmarkFor(object).orElseThrow(() -> new IllegalArgumentException(String.format("Could not create bookmark for '%s'", object)));
        return idStringifierForBookmark.enstring(bookmark);
    }

    @Override
    public Object destring(final String stringified, Class<?> targetEntityClass) {
        val bookmark = idStringifierForBookmark.destring(stringified, targetEntityClass);
        return bookmarkService.lookup(bookmark).orElseThrow(() -> new IllegalArgumentException(String.format("Could not lookup object from '%s'", bookmark)));
    }


}
