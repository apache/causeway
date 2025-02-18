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
package org.apache.causeway.applib.services.bookmark;

import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.schema.common.v2.OidDto;

/**
 * String representation of any persistable or re-creatable object managed by the framework.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@org.apache.causeway.applib.annotation.Value
@XmlJavaTypeAdapter(Bookmark.JaxbToStringAdapter.class) // for JAXB view model support
public record Bookmark(
    @NonNull String logicalTypeName, 
    @Nullable String identifier, 
    @Nullable String hintId,
    int precalculatedHashCode) implements Oid {

    // -- FACTORIES

    public static Bookmark empty(
            final @NonNull LogicalType logicalType) {
        return emptyForLogicalTypeName(logicalType.logicalName());
    }

    public static Bookmark emptyForLogicalTypeName(
            final @NonNull String logicalTypeName) {
        return new Bookmark(
                logicalTypeName,
                /*identifier*/null,
                /*hintId*/null);
    }

    public static Bookmark forLogicalTypeNameAndIdentifier(
            final @NonNull String logicalTypeName,
            final @NonNull String urlSafeIdentifier) {
        return new Bookmark(
                logicalTypeName,
                urlSafeIdentifier,
                /*hintId*/null);
    }

    public static Bookmark forLogicalTypeAndIdentifier(
            final @NonNull LogicalType logicalType,
            final @NonNull String urlSafeIdentifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier(
                logicalType.logicalName(),
                urlSafeIdentifier);
    }

    public static Bookmark forOidDto(final @NonNull OidDto oidDto) {
        return Bookmark.forLogicalTypeNameAndIdentifier(
                oidDto.getType(),
                oidDto.getId());
    }

    // -- CONSTRUCTOR

    private Bookmark(
            final String logicalTypeName,
            final String urlSafeIdentifier,
            final String hintId) {
        this(logicalTypeName, urlSafeIdentifier, hintId, Objects.hash(logicalTypeName, urlSafeIdentifier));
    }
    
    // -- WITHERS
    
    public Bookmark withHintId(final @Nullable String hintId) {
        return new Bookmark(this.logicalTypeName(), this.identifier(), hintId);
    }

    // -- PARSE

    /**
     * Round-trip with {@link #stringify()} representation.
     */
    public static Optional<Bookmark> parse(final @Nullable String str) {
        if(_Strings.isNullOrEmpty(str)) return Optional.empty();
        
        var tokenizer = new StringTokenizer(str, SEPARATOR);
        int tokenCount = tokenizer.countTokens();
        if(tokenCount==1) {
            return str.endsWith(SEPARATOR)
                    || str.startsWith(SEPARATOR)
                    ? Optional.empty() // invalid
                    : Optional.of(Bookmark.emptyForLogicalTypeName(
                            tokenizer.nextToken()));
        }
        if(tokenCount==2) {
            return Optional.of(Bookmark.forLogicalTypeNameAndIdentifier(
                    tokenizer.nextToken(),
                    tokenizer.nextToken()));
        }
        if(tokenCount>2) {
            return Optional.of(Bookmark.forLogicalTypeNameAndIdentifier(
                    tokenizer.nextToken(),
                    tokenizer.nextToken("").substring(1)));
        }
        return Optional.empty();
    }

    public static Bookmark parseElseFail(final @Nullable String input) {
        return parse(input)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot parse Bookmark '%s'", input));
    }

    /**
     * there is only one use-case, that is, if a bookmark itself needs to be encoded (eg. page params)
     */
    public static Optional<Bookmark> parseUrlEncoded(final @Nullable String urlEncodedStr) {
        return _Strings.isEmpty(urlEncodedStr)
                ? Optional.empty()
                : parse(UrlUtils.urlDecodeUtf8(urlEncodedStr));
    }

    // -- TO DTO

    public OidDto toOidDto() {
        var oidDto = new OidDto();
        oidDto.setType(logicalTypeName());
        oidDto.setId(identifier());
        return oidDto;
    }

    // -- STRINGIFY

    @Override
    public String stringify() {
        return stringify(identifier);
    }

    // -- OBJECT CONTRACT // not considering any hintId

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return equals((Bookmark) other);
    }

    public boolean equals(final Bookmark other) {
        return Objects.equals(logicalTypeName, other.logicalTypeName())
                && Objects.equals(identifier, other.identifier());
    }

    @Override
    public int hashCode() {
        return precalculatedHashCode;
    }

    @Override
    public String toString() {
        return stringify();
    }

    /**
     * Analogous to {@link #stringify()}, but replaces the {@code identifier} string with
     * the {@code hintId} if present and not empty.
     */
    public String stringifyHonoringHintIfAny() {
        return _Strings.isNotEmpty(hintId)
                ? stringify(hintId)
                : stringify(identifier);
    }

    /**
     * Whether represents {@code null}.
     */
    public boolean isEmpty() {
        return identifier==null;
    }

    // -- UTILITY

    public static class JaxbToStringAdapter extends XmlAdapter<String, Bookmark> {
        @Override
        public Bookmark unmarshal(final String literal) {
            return Bookmark.parse(literal).orElse(null);
        }

        @Override
        public String marshal(final Bookmark bookmark) {
            return bookmark != null ? bookmark.stringify() : null;
        }
    }

    // -- HELPER

    private String stringify(final String id) {
        return !isEmpty()
                ? logicalTypeName + SEPARATOR + id
                : logicalTypeName;
    }

}
