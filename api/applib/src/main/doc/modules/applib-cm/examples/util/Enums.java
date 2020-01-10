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

package org.apache.isis.applib.util;

import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Strings;

public final class Enums {

    private Enums(){}

    public static String getFriendlyNameOf(Enum<?> anEnum) {
        return getFriendlyNameOf(anEnum.name());
    }

    public static String getFriendlyNameOf(String anEnumName) {
        return _Strings.splitThenStream(anEnumName, "_")
                .map(_Strings::lower)
                .map(_Strings::capitalize)
                .collect(Collectors.joining(" "));
    }


    public static String getEnumNameFromFriendly(String anEnumFriendlyName) {
        return _Strings.splitThenStream(anEnumFriendlyName, " ")
                .map(_Strings::upper)
                .collect(Collectors.joining("_"));
    }


    public static String enumToHttpHeader(final Enum<?> anEnum) {
        return enumNameToHttpHeader(anEnum.name());
    }

    public static String enumNameToHttpHeader(final String name) {
        final StringBuilder builder = new StringBuilder();
        boolean nextUpper = true;
        for (final char c : name.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
                builder.append("-");
            } else {
                builder.append(nextUpper ? c : Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return builder.toString();
    }

    public static String enumToCamelCase(final Enum<?> anEnum) {
        return enumNameToCamelCase(anEnum.name());
    }

    private static String enumNameToCamelCase(final String name) {
        final StringBuilder builder = new StringBuilder();
        boolean nextUpper = false;
        for (final char c : name.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                builder.append(nextUpper ? c : Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return builder.toString();
    }


}
