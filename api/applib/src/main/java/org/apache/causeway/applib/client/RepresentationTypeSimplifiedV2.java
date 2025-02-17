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
package org.apache.causeway.applib.client;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Strings.KeyValuePair;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @since 1.x {@index}
 */
@RequiredArgsConstructor
public enum RepresentationTypeSimplifiedV2 {

    /**
     * The media type used as content-Type header when a domain object is rendered.
     */
    OBJECT("object"),

    /**
     * The media type used as content-Type header when a parented collection is rendered.
     */
    OBJECT_COLLECTION("object-collection"),

    /**
     * The media type used as content-Type header when an object property is rendered.
     */
    OBJECT_PROPERTY("object-property"),

    /**
     * The media type used as content-Type header when a standalone collection is rendered.
     */
    LIST("list"),

    /**
     * The media type used as content-Type header when a single (nullable) value is rendered.
     * @since 2.0
     */
    VALUE("value"),

    /**
     * The media type used as content-Type header when a list of values is rendered.
     * @since 2.0
     */
    VALUES("values"),

    /**
     * The media type used as content-Type header when a void action result is rendered.
     * @since 2.0
     */
    VOID("void"),

    ;

    @Getter final String typeLiteral;

    public boolean isObject()               { return this == OBJECT; }
    public boolean isObjectCollection()     { return this == OBJECT_COLLECTION; }
    public boolean isObjectProperty()       { return this == OBJECT_PROPERTY; }
    public boolean isList()                 { return this == LIST; }
    public boolean isValue()                { return this == VALUE; }
    public boolean isValues()               { return this == VALUES; }
    public boolean isVoid()                 { return this == VOID; }

    public String getContentTypeHeaderValue(final String profileName) {
        return "application/json;"
                + "profile=\"" + profileName + "\""
                + ";repr-type=\"" + typeLiteral + "\"";
    }

    public static Optional<RepresentationTypeSimplifiedV2> parse(
            final @Nullable String typeLiteral) {
        return Stream.of(RepresentationTypeSimplifiedV2.values())
        .filter(candidate->candidate.typeLiteral.equals(typeLiteral))
        .findAny();
    }

    public static Optional<RepresentationTypeSimplifiedV2> parseContentTypeHeaderString(
            final @Nullable String contentTypeHeaderString) {
        return extractReprType(_Strings.splitThenStream(contentTypeHeaderString, ";"))
        .map(typeLiteral->parse(typeLiteral).orElse(null))
        .filter(Objects::nonNull);
    }

    // -- HELPER

    private static String trimQuotesIfAny(final String s) {
        if(s.length()<2) {
            return s;
        }
        if(s.charAt(0) == '"'
                || s.charAt(0) == '\'') {
            // just assuming we have quotes at the end as well
            return s.substring(1, s.length()-1);
        }
        return s;
    }

    private static Optional<String> extractReprType(final @NonNull Stream<String> stringStream) {

        return stringStream
                //.peek(System.err::println)//debug
        .map(String::trim)
        .filter(_Strings::isNotEmpty)
        //.map(s->s.replace("profile=\"urn:org.restfulobjects:repr-types/", "repr-type=\""))
        .filter(s->s.startsWith("repr-type"))
        .map(s->_Strings.parseKeyValuePair(s, '=').orElse(null))
        .filter(Objects::nonNull)
        .map(KeyValuePair::value)
        .filter(_Strings::isNotEmpty)
        .findAny()
        .map(RepresentationTypeSimplifiedV2::trimQuotesIfAny);
    }

}
