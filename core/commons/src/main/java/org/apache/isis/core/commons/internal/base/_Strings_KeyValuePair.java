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

package org.apache.isis.core.commons.internal.base;

import java.util.Optional;

import static org.apache.isis.core.commons.internal.exceptions._Exceptions.notImplemented;

/**
 *
 * package private mixin for utility class {@link _Strings}
 *
 */
final class _Strings_KeyValuePair implements _Strings.KeyValuePair {

    static _Strings.KeyValuePair of(String key, String value) {
        return new _Strings_KeyValuePair(key, value);
    }

    private final String key;
    private final String value;

    private _Strings_KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String value) {
        throw notImplemented();
    }

    /**
     * Parses a string assumed to be of the form <kbd>key[separator]value</kbd> into its parts.
     * @param keyValueLiteral
     * @param separator
     * @return a non-empty Optional, if (and only if) the {@code keyValueLiteral} 
     * does contain at least one {@code separator}
     */
    public static Optional<_Strings.KeyValuePair> parse(String keyValueLiteral, char separator) {

        if(_Strings.isNullOrEmpty(keyValueLiteral)) {
            return Optional.empty();
        }

        final int equalsIndex = keyValueLiteral.indexOf(separator);
        if (equalsIndex == -1) {
            return Optional.empty();
        }

        String aKey = keyValueLiteral.substring(0, equalsIndex);
        String aValue = equalsIndex == keyValueLiteral.length() - 1 
                ? "" 
                        : keyValueLiteral.substring(equalsIndex + 1);

        return Optional.of(of(aKey, aValue));
    }

}
