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
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.causeway.applib.client.SuppressionType;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;

public enum RestfulClientMediaType {
    RO_XML{
        @Override
        public MediaType mediaTypeFor(final Class<?> dtoClass, final EnumSet<SuppressionType> suppressionTypes) {
            return new MediaType("application", "xml",
                    Map.<String, String>of(
                            "profile", "urn:org.restfulobjects:repr-types/action-result"
                                    + toSuppressionLiteral(suppressionTypes),
                            "x-ro-domain-type", dtoClass.getName()));
        }
    },
    SIMPLE_JSON {
        @Override
        public MediaType mediaTypeFor(final Class<?> dtoClass, final EnumSet<SuppressionType> suppressionTypes) {
            return new MediaType("application", "json",
                    Map.<String, String>of(
                            "profile", "urn:org.apache.causeway/v2"
                                    + toSuppressionLiteral(suppressionTypes),
                            "x-ro-domain-type", dtoClass.getName()));
        }
    }
    ;

    public final MediaType mediaTypeFor(final Class<?> dtoClass) {
        return mediaTypeFor(dtoClass, EnumSet.noneOf(SuppressionType.class));
    }

    public abstract MediaType mediaTypeFor(final Class<?> dtoClass, EnumSet<SuppressionType> suppressionTypes);

    private static String toSuppressionLiteral(final EnumSet<SuppressionType> suppressionTypes) {
        final String suppressionSetLiteral = _NullSafe.stream(suppressionTypes)
                .map(SuppressionType::name)
                .collect(Collectors.joining(","));
        if(_Strings.isNotEmpty(suppressionSetLiteral)) {
            return ";suppress=" + suppressionSetLiteral;
        }
        return "";
    }

}

