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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.commons.internal.base._Strings;

import lombok.NonNull;

@Component
@Priority(PriorityPrecedence.LATE)
public class IdStringifierForCharacter extends IdStringifier.Abstract<Character> {

    public IdStringifierForCharacter() {
        super(Character.class, char.class);
    }

    static final List<Character> NON_SAFE_URL_CHARS = IdStringifierForString.NON_SAFE_URL_CHARS.stream().map(x -> x.charAt(0)).collect(Collectors.toList());
    static final String REGULAR_PREFIX = "c" + SEPARATOR;
    static final String BASE64_PREFIX = "cbse64" + SEPARATOR;

    /**
     * Not API, but publicly visible for adhoc reuse by other {@link IdStringifier} implementations.
     */
    @Override
    public String enstring(final @NonNull Character id) {
        if(NON_SAFE_URL_CHARS.stream().anyMatch(x -> Objects.equals(x, id))) {
            return BASE64_PREFIX + _Strings.base64UrlEncode(""+id);
        }
        return REGULAR_PREFIX + id;
    }

    /**
     * Not API, but publicly visible for adhoc reuse by other {@link IdStringifier} implementations.
     */
    @Override
    public Character destring(
            final @NonNull String stringified,
            final @NonNull Class<?> targetEntityClass) {
        if(stringified.startsWith(REGULAR_PREFIX)) {
            return stringified.substring(REGULAR_PREFIX.length()).charAt(0);
        }
        if(stringified.startsWith(BASE64_PREFIX)) {
            return _Strings.base64UrlDecode(stringified.substring(BASE64_PREFIX.length())).charAt(0);
        }
        throw new IllegalArgumentException("Could not parse '" + stringified + "'");
    }

}
