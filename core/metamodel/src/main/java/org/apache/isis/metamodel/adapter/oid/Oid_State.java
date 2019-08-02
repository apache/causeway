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
package org.apache.isis.metamodel.adapter.oid;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.common.v1.BookmarkObjectState;
import org.apache.isis.schema.common.v1.OidDto;

enum Oid_State {
    PERSISTENT,
    TRANSIENT,
    VIEWMODEL;

    public boolean isTransient() {
        return this == TRANSIENT;
    }
    public boolean isViewModel() {
        return this == VIEWMODEL;
    }
    public boolean isPersistent() {
        return this == PERSISTENT;
    }

    public static Oid_State from(final Bookmark bookmark) {
        final Bookmark.ObjectState objectState = bookmark.getObjectState();
        return from(objectState);
    }

    public static Oid_State from(final Bookmark.ObjectState objectState) {
        switch (objectState) {
        case VIEW_MODEL:
            return VIEWMODEL;
        case TRANSIENT:
            return TRANSIENT;
        case PERSISTENT:
            return PERSISTENT;
        default:
            throw _Exceptions.unmatchedCase(objectState);
        }
    }
    public Bookmark.ObjectState asBookmarkObjectState() {
        switch (this) {
        case VIEWMODEL:
            return Bookmark.ObjectState.VIEW_MODEL;
        case TRANSIENT:
            return Bookmark.ObjectState.TRANSIENT;
        case PERSISTENT:
            return Bookmark.ObjectState.PERSISTENT;
        default:
            throw _Exceptions.unmatchedCase(this);
        }
    }

    public Bookmark bookmarkOf(RootOid rootOid) {
        final String objectType = asBookmarkObjectState().getCode() + rootOid.getObjectSpecId().asString();
        final String identifier = rootOid.getIdentifier();
        return new Bookmark(objectType, identifier);
    }

    public OidDto toOidDto(RootOid rootOid) {

        final OidDto oidDto = new OidDto();

        oidDto.setType(rootOid.getObjectSpecId().asString());
        oidDto.setId(rootOid.getIdentifier());

        final Bookmark.ObjectState objectState = asBookmarkObjectState();
        final BookmarkObjectState bookmarkState = objectState.toBookmarkState();
        // persistent is assumed if not specified...
        oidDto.setObjectState(
                bookmarkState != BookmarkObjectState.PERSISTENT ? bookmarkState : null);

        return oidDto;
    }

}