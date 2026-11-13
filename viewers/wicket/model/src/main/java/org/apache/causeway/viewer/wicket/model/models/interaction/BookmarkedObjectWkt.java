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
package org.apache.causeway.viewer.wicket.model.models.interaction;

import org.apache.wicket.model.IModel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.BookmarkedObjectHolder;

public record BookmarkedObjectWkt(
    @NonNull BookmarkedObjectHolder moHolder)
implements IModel<ManagedObject> {

    /** overwrites any current cache entry, only safe when no other views/models reference the same ManagedObject */
    public static BookmarkedObjectWkt ofAdapter(
            final @NonNull ManagedObject domainObject) {
        var bookmark = domainObject.getBookmarkElseFail();
        return new BookmarkedObjectWkt(new BookmarkedObjectHolder(bookmark, domainObject));
    }

    public static BookmarkedObjectWkt ofBookmark(final @Nullable Bookmark bookmark) {
        return new BookmarkedObjectWkt(new BookmarkedObjectHolder(bookmark, null));
    }

    public Bookmark bookmark() {
        return moHolder.bookmark();
    }

    public ManagedObject managedObject() {
        return moHolder.managedObject();
    }

    @Override
    public ManagedObject getObject() {
        return managedObject();
    }

    @Override
    public void setObject(final ManagedObject object) {
        throw _Exceptions.unsupportedOperation("BookmarkedObjectWkt is immuatable");
    }

    @Override
    public void detach() {
        moHolder.detach();
    }

}
