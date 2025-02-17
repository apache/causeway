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
package org.apache.causeway.commons.internal.base;

import java.util.Optional;

/**
 * package private utility for {@link _Strings}
 */
record _Strings_KeyValuePair(
        String key,
        String value
        ) implements _Strings.KeyValuePair {

    @Override
    public String toString() {
        return key + "=" + value;
    }

    /**
     * Parses a string assumed to be of the form <kbd>key[separator]value</kbd> into its parts.
     * @param keyValueLiteral
     * @param separator
     * @return a non-empty Optional, if (and only if) the {@code keyValueLiteral}
     * does contain at least one {@code separator}
     */
    public static Optional<_Strings.KeyValuePair> parse(final String keyValueLiteral, final char separator) {

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

        return Optional.of(new _Strings_KeyValuePair(aKey, aValue));
    }

}
