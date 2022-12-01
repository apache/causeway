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
package org.apache.causeway.viewer.restfulobjects.client;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.client.SuppressionType;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public enum RestfulClientMediaType {
    RO_XML("application", "xml", "org.restfulobjects:repr-types/action-result"),
    SIMPLE_JSON("application", "json", "org.apache.causeway/v2");
    ;

    private final String type;
    private final String subType;
    private final String urn;

    public final MediaType mediaTypeFor() {
        return mediaTypeFor(null, EnumSet.noneOf(SuppressionType.class));
    }

    public final MediaType mediaTypeFor(
            final @Nullable Class<?> dtoClass) {
        return mediaTypeFor(dtoClass, EnumSet.noneOf(SuppressionType.class));
    }

    public final MediaType mediaTypeFor(
            final @Nullable EnumSet<SuppressionType> suppressionTypes) {
        return new MediaType(type, subType, headerMap(urn, null, suppressionTypes));
    }

    public final MediaType mediaTypeFor(
            final @Nullable Class<?> dtoClass,
            final @Nullable EnumSet<SuppressionType> suppressionTypes) {
        return new MediaType(type, subType, headerMap(urn, dtoClass, suppressionTypes));
    }

    // -- HELPER

    private static Map<String, String> headerMap(
            final String urn,
            final Class<?> dtoClass,
            final EnumSet<SuppressionType> suppressionTypes) {
        val headerMap = _Maps.<String, String>newHashMap();

        headerMap.put("profile", "urn:" + urn);

        toSuppressionLiteral(suppressionTypes)
        .ifPresent(suppress->headerMap.put("suppress", suppress));

        Optional.ofNullable(dtoClass)
        .map(Class::getName)
        .ifPresent(typeLiteral->headerMap.put("x-ro-domain-type", typeLiteral));

        return headerMap;
    }

    private static Optional<String> toSuppressionLiteral(final EnumSet<SuppressionType> suppressionTypes) {
        final String suppressionSetLiteral = _NullSafe.stream(suppressionTypes)
                .map(SuppressionType::name)
                .collect(Collectors.joining(","));
        return _Strings.nonEmpty(suppressionSetLiteral);
    }

}

