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
package org.apache.causeway.applib.services.conmap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.causeway.commons.internal.base._NullSafe;

/**
 *
 * @since 1.x {@index}
 */
public interface ContentMappingService {

    /**
     * Typically for mapping from a domain object to a DTO.
     */
    Object map(
            Object object,
            final List<MediaType> acceptableMediaTypes);

    /**
     * Convenience utilities for implementations of {@link ContentMappingService}.
     */
    default boolean isSupported(
            final Class<?> clazz,
            final List<MediaType> acceptableMediaTypes) {
        final String domainType = determineDomainType(acceptableMediaTypes);
        return clazz.getName().equals(domainType);
    }

    default String determineDomainType(final List<MediaType> acceptableMediaTypes) {
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            final Map<String, String> parameters = acceptableMediaType.getParameters();
            final String domainType = parameters.get("x-ro-domain-type");
            if(domainType != null) {
                return domainType;
            }
        }
        throw new IllegalArgumentException(
                "Could not locate x-ro-domain-type parameter in any of the provided media types; got: " +
                        _NullSafe.stream(acceptableMediaTypes)
                                .filter(_NullSafe::isPresent)
                                .map(Object::toString)
                                .collect(Collectors.joining(", ")) );
    }

}
