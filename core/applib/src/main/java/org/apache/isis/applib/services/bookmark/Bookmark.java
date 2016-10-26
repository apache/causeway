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

import com.google.common.base.Splitter;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.schema.common.v1.BookmarkObjectState;
import org.apache.isis.schema.common.v1.OidDto;

/**
 * String representation of any persistent object managed by the framework.
 * 
 * <p>
 * Analogous to the <tt>RootOid</tt>.
 */
@Value
public class Bookmark implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final char SEPARATOR = ':';

    public OidDto toOidDto() {
        final OidDto oidDto = new OidDto();

        oidDto.setType(getObjectType());
        oidDto.setId(getIdentifier());

        // persistent is assumed if not specified...
        final BookmarkObjectState bookmarkState = getObjectState().toBookmarkState();
        oidDto.setObjectState(bookmarkState != BookmarkObjectState.PERSISTENT ? bookmarkState : null);

        return oidDto;
    }

    public static Bookmark from(final OidDto oidDto) {
        final BookmarkObjectState bookmarkObjectState = oidDto.getObjectState();
        final ObjectState objectState = ObjectState.from(bookmarkObjectState);
        final String objectType = coalesce(oidDto.getType(), oidDto.getObjectType());
        final String objectId = coalesce(oidDto.getId(), oidDto.getObjectIdentifier());
        final Bookmark bookmark = new Bookmark(objectState.getCode() + objectType, objectId);
        return bookmark;
    }

    private static String coalesce(final String first, final String second) {
        return first != null? first: second;
    }


    public enum ObjectState {
        PERSISTENT("", BookmarkObjectState.PERSISTENT),
        TRANSIENT("!", BookmarkObjectState.TRANSIENT), // same as OidMarshaller
        VIEW_MODEL("*", BookmarkObjectState.VIEW_MODEL); // same as OidMarshaller

        private final String code;
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

        public String getCode() {
            return code;
        }

        public static ObjectState from(final String objectType) {
            if(objectType.startsWith(TRANSIENT.code)) return TRANSIENT;
            if(objectType.startsWith(VIEW_MODEL.code)) return VIEW_MODEL;
            return PERSISTENT;
        }

        public static ObjectState from(final BookmarkObjectState objectState) {
            if(objectState == null) {
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
                // persistent is assumed if not specified
                return ObjectState.PERSISTENT;
            }
        }

        public BookmarkObjectState toBookmarkState() {
            return bookmarkObjectState;
        }
    }

    private final String objectType;
    private final String identifier;
    private final ObjectState state;


    /**
     * Round-trip with {@link #toString()} representation.
     */
    public Bookmark(final String toString) {
        this(Splitter.on(SEPARATOR).split(toString).iterator());
    }

    private Bookmark(final Iterator<String> split) {
        this(split.next(), split.next());
    }

    public Bookmark(final String objectType, final String identifier) {
        this.state = ObjectState.from(objectType);
        this.objectType = state != ObjectState.PERSISTENT ? objectType.substring(1): objectType;
        this.identifier = identifier;
    }

    public ObjectState getObjectState() {
        return state;
    }

    public String getObjectType() {
        return objectType;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Bookmark other = (Bookmark) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (objectType == null) {
            if (other.objectType != null)
                return false;
        } else if (!objectType.equals(other.objectType))
            return false;
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
        return state.getCode() + objectType + SEPARATOR + identifier;
    }


    public static class AsStringType {

        private AsStringType() {}

        public static class Meta {

            /**
             * Is based on the defacto limit of a request URL in web browsers such as IE8
             */
            public static final int MAX_LEN = 2000;

            private Meta() {}

        }

    }

}
