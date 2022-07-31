/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.isis.applib.services.bookmark.idstringifiers;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.commons.internal.base._Strings;

import lombok.NonNull;

@Component
@Priority(PriorityPrecedence.LATE)
public class IdStringifierForString extends IdStringifier.Abstract<String> {

    public IdStringifierForString() {
        super(String.class);
    }

    static final List<String> NON_SAFE_URL_CHARS = Arrays.asList("/", "\\", "?", ":", "&", "%", "+");
    static final String REGULAR_PREFIX = "s" + SEPARATOR;
    static final String BASE64_PREFIX = "base64" + SEPARATOR;

    /**
     * Not API, but publicly visible for adhoc reuse by other {@link IdStringifier} implementations.
     */
    @Override
    public String enstring(final @NonNull String id) {
        if(NON_SAFE_URL_CHARS.stream().anyMatch(id::contains)) {
            return BASE64_PREFIX + _Strings.base64UrlEncode(id);
        }
        return REGULAR_PREFIX + id;
    }

    /**
     * Not API, but publicly visible for adhoc reuse by other {@link IdStringifier} implementations.
     */
    @Override
    public String destring(
            final @NonNull String stringified,
            final Class<?> targetEntityClassIfAny) {
        if(stringified.startsWith(REGULAR_PREFIX)) {
            return stringified.substring(REGULAR_PREFIX.length());
        }
        if(stringified.startsWith(BASE64_PREFIX)) {
            return _Strings.base64UrlDecode(stringified.substring(BASE64_PREFIX.length()));
        }
        throw new IllegalArgumentException(String.format("Could not parse '%s'", stringified));
    }

}
