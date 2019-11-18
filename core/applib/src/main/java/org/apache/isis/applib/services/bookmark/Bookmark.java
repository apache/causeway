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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.common.v1.BookmarkObjectState;
import org.apache.isis.schema.common.v1.OidDto;

import lombok.Getter;
import lombok.val;

/**
 * String representation of any persistent object managed by the framework.
 *
 * <p>
 * Analogous to the <tt>RootOid</tt>.
 */
@Value
public class Bookmark implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String SEPARATOR = ":";
    
    @Getter private final String objectType;
    @Getter private final String identifier;
    @Getter private final ObjectState objectState;
    
    public enum ObjectState {
        PERSISTENT("", BookmarkObjectState.PERSISTENT),
        TRANSIENT("!", BookmarkObjectState.TRANSIENT), // same as OidMarshaller
        VIEW_MODEL("*", BookmarkObjectState.VIEW_MODEL); // same as OidMarshaller

        @Getter private final String code;
        private final BookmarkObjectState bookmarkObjectState;

        ObjectState(
                final String code,
                final BookmarkObjectState bookmarkObjectState) {
            this.code = code;
            this.bookmarkObjectState = bookmarkObjectState;
        }

        public boolean isTransient() {
            return this == TRANSIENT;
        }
        public boolean isViewModel() {
            return this == VIEW_MODEL;
        }
        public boolean isPersistent() {
            return this == PERSISTENT;
        }

        public static ObjectState from(final String objectType) {
            if(objectType.startsWith(TRANSIENT.code)) return TRANSIENT;
            if(objectType.startsWith(VIEW_MODEL.code)) return VIEW_MODEL;
            return PERSISTENT;
        }

        public static ObjectState from(final BookmarkObjectState objectState) {
            if(objectState == null) {
                // persistent is assumed if not specified
                return ObjectState.PERSISTENT;
            }
            switch (objectState) {
            case TRANSIENT:
                return ObjectState.TRANSIENT;
            case VIEW_MODEL:
                return ObjectState.VIEW_MODEL;
            case PERSISTENT:
                return ObjectState.PERSISTENT;
            default:
                throw _Exceptions.unmatchedCase(objectState);
            }
        }

        public BookmarkObjectState toBookmarkState() {
            return bookmarkObjectState;
        }
    }

    public static Bookmark create(String str) {
        return str != null? new Bookmark(str): null;
    }

    public OidDto toOidDto() {
        final OidDto oidDto = new OidDto();

        oidDto.setType(getObjectType());
        oidDto.setId(getIdentifier());

        // persistent is assumed if not specified...
        final BookmarkObjectState bookmarkState = getObjectState().toBookmarkState();
        oidDto.setObjectState(bookmarkState != BookmarkObjectState.PERSISTENT ? bookmarkState : null);

        return oidDto;
    }

    public static Bookmark from(OidDto oidDto) {
        val bookmarkObjectState = oidDto.getObjectState();
        val objectState = ObjectState.from(bookmarkObjectState);
        val objectType = coalesce(oidDto.getType(), oidDto.getObjectType());
        val objectId = coalesce(oidDto.getId(), oidDto.getObjectIdentifier());
        val bookmark = new Bookmark(objectState.getCode() + objectType, objectId);
        return bookmark;
    }

    /**
     * Round-trip with {@link #toString()} representation.
     */
    public Bookmark(final String toString) {
        this(_Strings.splitThenStream(toString, SEPARATOR).iterator());
    }

    private Bookmark(final Iterator<String> split) {
        this(split.next(), split.next());
    }

    public Bookmark(final String objectType, final String identifier) {
        this.objectState = ObjectState.from(objectType);
        this.objectType = objectState != ObjectState.PERSISTENT ? objectType.substring(1): objectType;
        this.identifier = identifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getIdentifier(), this.getObjectType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        
        val other = (Bookmark) obj;
        
        if(! Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        if(! Objects.equals(this.objectType, other.objectType)) {
            return false;
        }
        
        return true;
    }

    /**
     * The canonical form of the {@link Bookmark}, that is &quot;{@link #getObjectType() objectType}{@value #SEPARATOR}{@link #getIdentifier()}&quot;.
     *
     * <p>
     * This is parseable by the {@link #Bookmark(String) string constructor}.
     */
    @Override
    public String toString() {
        return objectState.getCode() + objectType + SEPARATOR + identifier;
    }

    // -- HELPER
    
    private static String coalesce(final String first, final String second) {
        return first != null? first: second;
    }

    

}
