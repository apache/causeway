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

import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.codec._UrlDecoderUtil;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * String representation of any persistable or re-creatable object managed by the framework.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@org.apache.isis.applib.annotation.Value(
        logicalTypeName = IsisModuleApplib.NAMESPACE + ".Bookmark")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bookmark implements Oid {

    private static final long serialVersionUID = 3L;

    @Getter(onMethod_ = {@Override}) private final String logicalTypeName;
    @Getter(onMethod_ = {@Override}) private final String identifier;
    @Getter private final @Nullable String hintId;
    private final int hashCode;

    // -- FACTORIES

    public static Bookmark forLogicalTypeNameAndIdentifier(
            final @NonNull String logicalTypeName,
            final @NonNull String identifier) {
        return new Bookmark(
                logicalTypeName,
                identifier,
                /*hintId*/null);
    }

    public static Bookmark forLogicalTypeAndIdentifier(
            final @NonNull LogicalType logicalType,
            final @NonNull String identifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier(
                logicalType.getLogicalTypeName(),
                identifier);
    }

    public static Bookmark forOidDto(final @NonNull OidDto oidDto) {
        return Bookmark.forLogicalTypeNameAndIdentifier(
                oidDto.getType(),
                oidDto.getId());
    }

    public Bookmark withHintId(final @Nullable String hintId) {
        return new Bookmark(this.getLogicalTypeName(), this.getIdentifier(), hintId);
    }

    // -- CONSTRUCTOR

    private Bookmark(
            final String logicalTypeName,
            final String identifier,
            final String hintId) {

        this.logicalTypeName = logicalTypeName;
        this.identifier = identifier;
        this.hintId = hintId;
        this.hashCode = Objects.hash(logicalTypeName, identifier);
    }

    // -- PARSE

    /**
     * Round-trip with {@link #stringify()} representation.
     */
    public static Optional<Bookmark> parse(final @Nullable String str) {

        if(str==null) {
            return Optional.empty();
        }
        val tokenizer = new StringTokenizer(str, SEPARATOR);
        int tokenCount = tokenizer.countTokens();
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
                .orElseThrow(()->_Exceptions.illegalArgument("cannot parse Bookmark %s", input));
    }

    public static Optional<Bookmark> parseUrlEncoded(@Nullable final String urlEncodedStr) {
        return _Strings.isEmpty(urlEncodedStr)
                ? Optional.empty()
                : parse(_UrlDecoderUtil.urlDecode(urlEncodedStr));
    }

    // -- TO DTO

    public OidDto toOidDto() {
        val oidDto = new OidDto();
        oidDto.setType(getLogicalTypeName());
        oidDto.setId(getIdentifier());
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
        return Objects.equals(logicalTypeName, other.getLogicalTypeName())
                && Objects.equals(identifier, other.getIdentifier());
    }

    @Override
    public int hashCode() {
        return hashCode;
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

    // -- HELPER

    private String stringify(final String id) {
        return logicalTypeName + SEPARATOR + id;
    }



}
