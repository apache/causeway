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
package org.apache.causeway.viewer.restfulobjects.applib.client;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

import org.apache.causeway.applib.client.SuppressionType;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CausewayMediaTypes {

    public final MediaType CAUSEWAY_XML = profile(MediaType.APPLICATION_XML, "urn:org.restfulobjects:repr-types/action-result");
    public final MediaType CAUSEWAY_JSON_V2 = profile(MediaType.APPLICATION_JSON, "urn:org.apache.causeway/v2");
    public final MediaType CAUSEWAY_JSON_V2_LIGHT = suppress(CAUSEWAY_JSON_V2, SuppressionType.all());

    public MediaType appendParameter(MediaType input, String name, String value) {
        if(!StringUtils.hasLength(name)) return input;
        value = _Strings.nullToEmpty(value);

        var parameters = input.getParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            var map = new LinkedCaseInsensitiveMap<String>(parameters.size(), Locale.ROOT);
            map.put(name, value);
            parameters.forEach(map::put);
            return new MediaType(input, map);
        }
        return new MediaType(input, Map.of(name, value));
    }

    public MediaType profile(MediaType input, String urn) {
        if(!StringUtils.hasLength(urn)) return input;

        urn = _Strings.prefix(urn, "\"");
        urn = _Strings.suffix(urn, "\"");

        return appendParameter(input, "profile", urn);
    }

    public MediaType suppress(MediaType input, EnumSet<SuppressionType> suppressionTypes) {
        return SuppressionType.toLiteral(suppressionTypes)
            .map(value->appendParameter(input, "suppress", value))
            .orElse(input);
    }

    public MediaType domainType(MediaType input, Class<?> domainType) {
        return Optional.ofNullable(domainType)
            .map(Class::getName)
            .map(name->appendParameter(input, "x-ro-domain-type", name))
            .orElse(input);
    }

}
