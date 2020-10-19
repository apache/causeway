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
package org.apache.isis.applib.client;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.base._Strings.KeyValuePair;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
    
    private static String trimFirstAndLastCharacter(String s) {
        if(s.length()<2) {
            return s;
        }
        return s.substring(1, s.length()-1);
    }
    
    private static Optional<String> extractReprType(final @NonNull Stream<String> stringStream) {
        return stringStream
        .map(String::trim)
        .filter(_Strings::isNotEmpty)
        .filter(s->s.startsWith("repr-type"))
        .map(s->_Strings.parseKeyValuePair(s, '=').orElse(null))
        .filter(Objects::nonNull)
        .map(KeyValuePair::getValue)
        .filter(_Strings::isNotEmpty)
        .findAny()
        .map(RepresentationTypeSimplifiedV2::trimFirstAndLastCharacter);
    }
    
   
    
}
