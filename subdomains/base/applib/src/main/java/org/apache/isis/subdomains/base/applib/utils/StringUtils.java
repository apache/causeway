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
package org.apache.isis.subdomains.base.applib.utils;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Strings;

/**
 * @since 2.0 {@index}
 */
public final class StringUtils {

    private StringUtils() {
    }

    private static UnaryOperator<String> LOWER_CASE_THEN_CAPITALIZE = (final String input) -> {
        return input != null
                ? StringUtils.capitalize(input.toLowerCase())
                : null;
    };

    private static UnaryOperator<String> UPPER_CASE = (final String input) -> {
        return input != null
                ? StringUtils.capitalize(input.toUpperCase())
                : null;
    };


    public static String enumTitle(final String string) {
        if(string == null) {
            return null;
        }

        return _Strings.splitThenStream(string, "_")
        .map(LOWER_CASE_THEN_CAPITALIZE)
        .collect(Collectors.joining(" "));
    }

    public static String enumDeTitle(final String string) {
        if(string == null) {
            return null;
        }
        return _Strings.splitThenStream(string, " ")
        .map(UPPER_CASE)
        .collect(Collectors.joining("_"));
    }

    public static String wildcardToCaseInsensitiveRegex(final String pattern) {
        if(pattern == null) {
            return null;
        }
        return "(?i)".concat(wildcardToRegex(pattern));
    }

    public static String wildcardToRegex(final String pattern) {
        if(pattern == null) {
            return null;
        }
        return pattern.replace("*", ".*").replace("?", ".");
    }

    public static String capitalize(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }


}
