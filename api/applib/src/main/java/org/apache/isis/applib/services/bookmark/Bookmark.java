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
import java.util.Optional;
import java.util.StringTokenizer;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * String representation of any persistable or re-createable object managed by the framework.
 *
 * <p>
 * Analogous to the <tt>RootOid</tt>.
 * 
 * @since 1.x revised for 2.0 {@index}
 */
@Value 
@lombok.Value @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Bookmark implements Serializable {

    private static final long serialVersionUID = 3L;

    protected static final String SEPARATOR = ":";

    /**
     * Corresponds directly to the object's logical-type-name (aka. object-type).
     * @see RootOid
     */
    @NonNull  private final String logicalTypeName;
    @NonNull  private final String identifier;

    @Nullable private final String hintId;

    public static Bookmark of(String logicalTypeName, String identifier) {
        return new Bookmark(logicalTypeName, identifier, /*hintId*/ null);
    }

    /**
     * Round-trip with {@link #toString()} representation.
     */
    public static Optional<Bookmark> parse(@Nullable String str) {

        if(str==null) {
            return Optional.empty();
        }
        val tokenizer = new StringTokenizer(str, SEPARATOR);
        int tokenCount = tokenizer.countTokens();
        if(tokenCount==2) {
            return Optional.of(Bookmark.of(tokenizer.nextToken(), tokenizer.nextToken()));
        }
        if(tokenCount>2) {
            return Optional.of(Bookmark.of(tokenizer.nextToken(), tokenizer.nextToken("").substring(1)));
        }
        return Optional.empty();

        // ...
    }

    public OidDto toOidDto() {
        val oidDto = new OidDto();
        oidDto.setType(getLogicalTypeName());
        oidDto.setId(getIdentifier());
        return oidDto;
    }

    public static Bookmark from(@NonNull OidDto oidDto) {
        return Bookmark.of(oidDto.getType(), oidDto.getId());
    }
    
    /**
     * The canonical form of the {@link Bookmark}, that is 
     * &quot;{@link #getLogicalTypeName() logical-type-name}{@value #SEPARATOR}{@link #getIdentifier()}&quot;.
     *
     * <p>
     * This is parseable by the {@link #parse(String)}.
     */
    @Override
    public String toString() {
        return toStringUsingIdentifier(identifier);
    }

    public Bookmark withHintId(@NonNull String hintId) {
        return new Bookmark(this.getLogicalTypeName(), this.getIdentifier(), hintId); 
    }

    public String toStringUsingIdentifier(String id) {
        return logicalTypeName + SEPARATOR + id;
    }
    
    // -- DEPRECATIONS 
    
    /**
     * @deprecated use {@link #getLogicalTypeName()} instead
     */
    public String getObjectType() {
        return getLogicalTypeName();
    }
    

}
